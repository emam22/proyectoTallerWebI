package com.tallerwebi.presentacion;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.mercadopago.resources.payment.Payment;
import com.tallerwebi.dominio.ServicioLogin;
import com.tallerwebi.dominio.ServicioMercadoPago;
import com.tallerwebi.dominio.Usuario;
import jakarta.servlet.http.HttpSession;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.servlet.ModelAndView;

public class ControladorPagoTest {

  private ServicioMercadoPago servicioMercadoPagoMock;
  private ServicioLogin servicioLoginMock;
  private HttpSession sessionMock;
  private ControladorPago controladorPago;

  @BeforeEach
  public void init() {
    servicioMercadoPagoMock = mock(ServicioMercadoPago.class);
    servicioLoginMock = mock(ServicioLogin.class);
    sessionMock = mock(HttpSession.class);
    controladorPago = new ControladorPago(servicioMercadoPagoMock, servicioLoginMock);
  }

  @Test
  public void cuandoElPagoEsAprobadoDebeCrearAdminIniciarSesionYRedirigir() throws Exception {
    ProcesoPagoDTO dto = new ProcesoPagoDTO();
    dto.setEmail("admin@unlam.edu.ar");
    dto.setPassword("Admin123!");

    Payment paymentMock = mock(Payment.class);
    when(paymentMock.getStatus()).thenReturn("approved");
    when(paymentMock.getId()).thenReturn(987654321L);

    when(servicioLoginMock.validarFortaleza(dto.getPassword())).thenReturn("FUERTE");
    when(servicioMercadoPagoMock.crearPago(dto)).thenReturn(paymentMock);

    ResponseEntity<?> response = controladorPago.procesarPago(dto, sessionMock);

    assertThat(response.getStatusCode(), equalTo(HttpStatus.OK));

    @SuppressWarnings("unchecked")
    Map<String, Object> body = (Map<String, Object>) response.getBody();
    assertThat(body, notNullValue());
    assertThat(body.get("status"), equalTo("approved"));
    assertThat(body.get("redirect"), equalTo("/spring/lobbyAdmin"));

    verify(servicioLoginMock).registrar(any(Usuario.class));
    verify(sessionMock).setAttribute("ROL", "admin");
  }

  @Test
  public void cuandoLaContraseniaEsDebilDebeRetornarBadRequestYSinCobrarEnMercadoPago()
    throws Exception {
    ProcesoPagoDTO dto = new ProcesoPagoDTO();
    dto.setEmail("admin@unlam.edu.ar");
    dto.setPassword("1234");

    when(servicioLoginMock.validarFortaleza(dto.getPassword())).thenReturn("DEBIL");

    ResponseEntity<?> response = controladorPago.procesarPago(dto, sessionMock);

    assertThat(response.getStatusCode(), equalTo(HttpStatus.BAD_REQUEST));

    @SuppressWarnings("unchecked")
    Map<String, Object> body = (Map<String, Object>) response.getBody();
    assertThat(body, notNullValue());
    assertThat(body.get("status"), equalTo("error"));

    verify(servicioMercadoPagoMock, never()).crearPago(any());
    verify(servicioLoginMock, never()).registrar(any());
  }

  @Test
  public void cuandoLaContraseniaEsInvalidaDebeRetornarBadRequest() throws Exception {
    ProcesoPagoDTO dto = new ProcesoPagoDTO();
    dto.setEmail("admin@unlam.edu.ar");
    dto.setPassword("   ");

    ResponseEntity<?> response = controladorPago.procesarPago(dto, sessionMock);

    assertThat(response.getStatusCode(), equalTo(HttpStatus.BAD_REQUEST));
    verify(servicioMercadoPagoMock, never()).crearPago(any());
  }

  @Test
  public void cuandoElPagoEsRechazadoPorMercadoPagoNoDebeCrearAdmin() throws Exception {
    ProcesoPagoDTO dto = new ProcesoPagoDTO();
    dto.setEmail("admin@unlam.edu.ar");
    dto.setPassword("Admin123!");

    Payment paymentMock = mock(Payment.class);
    when(paymentMock.getStatus()).thenReturn("rejected");
    when(paymentMock.getStatusDetail()).thenReturn("cc_rejected_insufficient_amount");

    when(servicioLoginMock.validarFortaleza(dto.getPassword())).thenReturn("FUERTE");
    when(servicioMercadoPagoMock.crearPago(dto)).thenReturn(paymentMock);

    ResponseEntity<?> response = controladorPago.procesarPago(dto, sessionMock);

    assertThat(response.getStatusCode(), equalTo(HttpStatus.OK));

    @SuppressWarnings("unchecked")
    Map<String, Object> body = (Map<String, Object>) response.getBody();
    assertThat(body, notNullValue());
    assertThat(body.get("status"), equalTo("rejected"));

    verify(servicioLoginMock, never()).registrar(any());
    verify(sessionMock, never()).setAttribute(any(), any());
  }

  @Test
  public void cuandoOcurreUnErrorEnMercadoPagoDebeRetornarInternalServerError() throws Exception {
    ProcesoPagoDTO dto = new ProcesoPagoDTO();
    dto.setEmail("admin@unlam.edu.ar");
    dto.setPassword("Admin123!");

    when(servicioLoginMock.validarFortaleza(dto.getPassword())).thenReturn("FUERTE");
    when(servicioMercadoPagoMock.crearPago(dto))
      .thenThrow(new RuntimeException("Fallo en API Mercado Pago"));

    ResponseEntity<?> response = controladorPago.procesarPago(dto, sessionMock);

    assertThat(response.getStatusCode(), equalTo(HttpStatus.INTERNAL_SERVER_ERROR));

    @SuppressWarnings("unchecked")
    Map<String, Object> body = (Map<String, Object>) response.getBody();
    assertThat(body, notNullValue());
    assertThat(body.get("status"), equalTo("error"));
  }

  @Test
  public void irAPagarInscripcionDeberiaRetornarVistaPagoConMonto() {
    ModelAndView mav = controladorPago.irAPagarInscripcion();

    assertThat(mav.getViewName(), equalTo("pagoInscripcion"));
    assertThat(mav.getModel().get("monto"), equalTo(5000.00));
  }

  @Test
  public void cuandoLaContraseniaEsNullDebeRetornarBadRequest() throws Exception {
    ProcesoPagoDTO dto = new ProcesoPagoDTO();
    dto.setEmail("admin@unlam.edu.ar");
    dto.setPassword(null);

    ResponseEntity<?> response = controladorPago.procesarPago(dto, sessionMock);

    assertThat(response.getStatusCode(), equalTo(HttpStatus.BAD_REQUEST));
    verify(servicioMercadoPagoMock, never()).crearPago(any());
  }
}
