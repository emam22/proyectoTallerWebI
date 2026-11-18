package com.tallerwebi.presentacion;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
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

  private static final String CLAVE_EMAIL = "REGISTRO_ADMIN_EMAIL";
  private static final String CLAVE_PASSWORD = "REGISTRO_ADMIN_PASSWORD";
  private static final String EMAIL = "admin@unlam.edu.ar";
  private static final String PASSWORD = "Admin123!";

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

  private void simularCredencialesEnSesion() {
    when(sessionMock.getAttribute(CLAVE_EMAIL)).thenReturn(EMAIL);
    when(sessionMock.getAttribute(CLAVE_PASSWORD)).thenReturn(PASSWORD);
  }

  private ProcesoPagoDTO crearDtoDePago() {
    ProcesoPagoDTO dto = new ProcesoPagoDTO();
    dto.setToken("tok_123");
    return dto;
  }

  @Test
  public void cuandoElPagoEsAprobadoDebeCrearAdminIniciarSesionYRedirigir() throws Exception {
    simularCredencialesEnSesion();
    ProcesoPagoDTO dto = crearDtoDePago();

    Payment paymentMock = mock(Payment.class);
    when(paymentMock.getStatus()).thenReturn("approved");
    when(paymentMock.getId()).thenReturn(987654321L);

    when(servicioLoginMock.validarFortaleza(PASSWORD)).thenReturn("FUERTE");
    when(servicioMercadoPagoMock.crearPago(dto)).thenReturn(paymentMock);

    ResponseEntity<?> response = controladorPago.procesarPago(dto, sessionMock);

    assertThat(response.getStatusCode(), equalTo(HttpStatus.OK));

    @SuppressWarnings("unchecked")
    Map<String, Object> body = (Map<String, Object>) response.getBody();
    assertThat(body, notNullValue());
    assertThat(body.get("status"), equalTo("approved"));
    assertThat(body.get("redirect"), equalTo("/spring/login"));

    verify(servicioLoginMock).registrar(any(Usuario.class));
    verify(sessionMock).setAttribute("ROL", "admin");
    verify(sessionMock).setAttribute(eq("USUARIO"), any(Usuario.class));
  }

  @Test
  public void cuandoLaContraseniaEsDebilDebeRetornarBadRequestYSinCobrarEnMercadoPago()
    throws Exception {
    simularCredencialesEnSesion();
    when(sessionMock.getAttribute(CLAVE_PASSWORD)).thenReturn("1234");
    ProcesoPagoDTO dto = crearDtoDePago();

    when(servicioLoginMock.validarFortaleza("1234")).thenReturn("DEBIL");

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
    simularCredencialesEnSesion();
    when(sessionMock.getAttribute(CLAVE_PASSWORD)).thenReturn("   ");
    ProcesoPagoDTO dto = crearDtoDePago();

    ResponseEntity<?> response = controladorPago.procesarPago(dto, sessionMock);

    assertThat(response.getStatusCode(), equalTo(HttpStatus.BAD_REQUEST));
    verify(servicioMercadoPagoMock, never()).crearPago(any());
  }

  @Test
  public void cuandoElPagoEsRechazadoPorMercadoPagoNoDebeCrearAdmin() throws Exception {
    simularCredencialesEnSesion();
    ProcesoPagoDTO dto = crearDtoDePago();

    Payment paymentMock = mock(Payment.class);
    when(paymentMock.getStatus()).thenReturn("rejected");
    when(paymentMock.getStatusDetail()).thenReturn("cc_rejected_insufficient_amount");

    when(servicioLoginMock.validarFortaleza(PASSWORD)).thenReturn("FUERTE");
    when(servicioMercadoPagoMock.crearPago(dto)).thenReturn(paymentMock);

    ResponseEntity<?> response = controladorPago.procesarPago(dto, sessionMock);

    assertThat(response.getStatusCode(), equalTo(HttpStatus.OK));

    @SuppressWarnings("unchecked")
    Map<String, Object> body = (Map<String, Object>) response.getBody();
    assertThat(body, notNullValue());
    assertThat(body.get("status"), equalTo("rejected"));

    verify(servicioLoginMock, never()).registrar(any());
    verify(sessionMock, never()).setAttribute(eq("ROL"), any());
  }

  @Test
  public void cuandoOcurreUnErrorEnMercadoPagoDebeRetornarInternalServerError() throws Exception {
    simularCredencialesEnSesion();
    ProcesoPagoDTO dto = crearDtoDePago();

    when(servicioLoginMock.validarFortaleza(PASSWORD)).thenReturn("FUERTE");
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
  public void procesarPagoSinCredencialesEnSesionNoDebeCobrarNiRegistrar() throws Exception {
    ProcesoPagoDTO dto = crearDtoDePago();

    ResponseEntity<?> response = controladorPago.procesarPago(dto, sessionMock);

    assertThat(response.getStatusCode(), equalTo(HttpStatus.BAD_REQUEST));

    @SuppressWarnings("unchecked")
    Map<String, Object> body = (Map<String, Object>) response.getBody();
    assertThat(body, notNullValue());
    assertThat(String.valueOf(body.get("mensaje")), containsString("No hay datos de registro"));

    verify(servicioMercadoPagoMock, never()).crearPago(any());
    verify(servicioLoginMock, never()).registrar(any());
  }

  @Test
  public void procesarPagoDebeLimpiarLasCredencialesDeLaSesion() throws Exception {
    simularCredencialesEnSesion();
    ProcesoPagoDTO dto = crearDtoDePago();
    Payment paymentMock = mock(Payment.class);
    when(paymentMock.getStatus()).thenReturn("approved");
    when(servicioLoginMock.validarFortaleza(PASSWORD)).thenReturn("FUERTE");
    when(servicioMercadoPagoMock.crearPago(dto)).thenReturn(paymentMock);

    controladorPago.procesarPago(dto, sessionMock);

    verify(sessionMock).removeAttribute(CLAVE_EMAIL);
    verify(sessionMock).removeAttribute(CLAVE_PASSWORD);
  }

  @Test
  public void validarPasswordPreviaConPasswordDebilDebeResponderValidoFalse() {
    RegistroAdminDTO dto = new RegistroAdminDTO();
    dto.setPassword("1234");
    when(servicioLoginMock.validarFortaleza("1234")).thenReturn("DEBIL");

    ResponseEntity<?> response = controladorPago.validarPasswordPrevia(dto);

    @SuppressWarnings("unchecked")
    Map<String, Object> body = (Map<String, Object>) response.getBody();
    assertThat(body.get("valido"), equalTo(false));
    assertThat(body.get("mensaje"), notNullValue());
  }

  @Test
  public void validarPasswordPreviaConPasswordFuerteDebeResponderValidoTrue() {
    RegistroAdminDTO dto = new RegistroAdminDTO();
    dto.setPassword(PASSWORD);
    when(servicioLoginMock.validarFortaleza(PASSWORD)).thenReturn("FUERTE");

    ResponseEntity<?> response = controladorPago.validarPasswordPrevia(dto);

    @SuppressWarnings("unchecked")
    Map<String, Object> body = (Map<String, Object>) response.getBody();
    assertThat(body.get("valido"), equalTo(true));
  }

  @Test
  public void prepararPagoDebeGuardarCredencialesEnLaSesion() {
    RegistroAdminDTO dto = new RegistroAdminDTO();
    dto.setEmail("  admin@unlam.edu.ar  ");
    dto.setPassword(PASSWORD);
    when(servicioLoginMock.validarFortaleza(PASSWORD)).thenReturn("FUERTE");

    ResponseEntity<?> response = controladorPago.prepararPago(dto, sessionMock);

    assertThat(response.getStatusCode(), equalTo(HttpStatus.OK));
    verify(sessionMock).setAttribute(CLAVE_EMAIL, EMAIL);
    verify(sessionMock).setAttribute(CLAVE_PASSWORD, PASSWORD);
  }

  @Test
  public void prepararPagoConPasswordDebilNoDebeGuardarCredenciales() {
    RegistroAdminDTO dto = new RegistroAdminDTO();
    dto.setEmail(EMAIL);
    dto.setPassword("1234");
    when(servicioLoginMock.validarFortaleza("1234")).thenReturn("DEBIL");

    ResponseEntity<?> response = controladorPago.prepararPago(dto, sessionMock);

    assertThat(response.getStatusCode(), equalTo(HttpStatus.BAD_REQUEST));
    verify(sessionMock, never()).setAttribute(eq(CLAVE_EMAIL), any());
    verify(sessionMock, never()).setAttribute(eq(CLAVE_PASSWORD), any());
  }

  @Test
  public void prepararPagoSinEmailNoDebeGuardarCredenciales() {
    RegistroAdminDTO dto = new RegistroAdminDTO();
    dto.setEmail("");
    dto.setPassword(PASSWORD);

    ResponseEntity<?> response = controladorPago.prepararPago(dto, sessionMock);

    assertThat(response.getStatusCode(), equalTo(HttpStatus.BAD_REQUEST));
    assertThat(((Map<?, ?>) response.getBody()).get("mensaje"), notNullValue());
    verify(sessionMock, never()).setAttribute(eq(CLAVE_PASSWORD), any());
  }
}
