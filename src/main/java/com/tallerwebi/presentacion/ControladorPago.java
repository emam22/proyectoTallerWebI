package com.tallerwebi.presentacion;

import com.mercadopago.exceptions.MPApiException;
import com.mercadopago.exceptions.MPException;
import com.mercadopago.resources.payment.Payment;
import com.tallerwebi.dominio.ServicioLogin;
import com.tallerwebi.dominio.ServicioMercadoPago;
import com.tallerwebi.dominio.Usuario;
import jakarta.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class ControladorPago {

  private static final Logger LOGGER = LoggerFactory.getLogger(ControladorPago.class);
  private static final String ESTADO_APROBADO = "approved";
  private static final String ROL_ADMIN = "admin";
  private static final String FORTALEZA_DEBIL = "DEBIL";
  private static final String FORTALEZA_INVALIDA = "INVALIDA";
  private static final String CLAVE_STATUS = "status";
  private static final String CLAVE_MENSAJE = "mensaje";
  private static final String ESTADO_ERROR = "error";

  private final ServicioMercadoPago mpService;
  private final ServicioLogin loginService;

  @Autowired
  public ControladorPago(ServicioMercadoPago mpService, ServicioLogin loginService) {
    this.mpService = mpService;
    this.loginService = loginService;
  }

  @GetMapping("/pagar-inscripcion")
  public ModelAndView irAPagarInscripcion() {
    ModelAndView modelAndView = new ModelAndView("pagoInscripcion");
    modelAndView.addObject("monto", 5000.00);
    return modelAndView;
  }

  @GetMapping("/api/validar-password")
  @ResponseBody
  public ResponseEntity<?> validarPasswordPrevia(@RequestParam("password") String password) {
    Map<String, Object> resp = new HashMap<>();
    String error = obtenerErrorPassword(password);
    if (error != null) {
      resp.put("valido", false);
      resp.put(CLAVE_MENSAJE, error);
      return ResponseEntity.ok(resp);
    }
    resp.put("valido", true);
    return ResponseEntity.ok(resp);
  }

  @PostMapping("/api/pagos/procesar")
  @ResponseBody
  public ResponseEntity<?> procesarPago(@RequestBody ProcesoPagoDTO dto, HttpSession session) {
    Map<String, Object> respuesta = new HashMap<>();

    String errorValidacion = obtenerErrorPassword(dto.getPassword());
    if (errorValidacion != null) {
      respuesta.put(CLAVE_STATUS, ESTADO_ERROR);
      respuesta.put(CLAVE_MENSAJE, errorValidacion);
      return ResponseEntity.badRequest().body(respuesta);
    }

    try {
      Payment pago = mpService.crearPago(dto);
      return armarRespuestaExito(pago, dto, session);
    } catch (MPApiException apiException) {
      return manejarErrorApi(apiException);
    } catch (MPException mpException) {
      return manejarErrorSdk(mpException);
    } catch (Exception e) {
      return manejarErrorGeneral(e);
    }
  }

  private ResponseEntity<?> armarRespuestaExito(
    Payment pago,
    ProcesoPagoDTO dto,
    HttpSession session
  ) throws Exception {
    Map<String, Object> respuesta = new HashMap<>();

    if (ESTADO_APROBADO.equalsIgnoreCase(pago.getStatus())) {
      registrarAdminSiAplica(dto, session);
      respuesta.put(CLAVE_STATUS, ESTADO_APROBADO);
      respuesta.put("id", pago.getId());
      respuesta.put("redirect", "/spring/lobbyAdmin");
      return ResponseEntity.ok(respuesta);
    }

    respuesta.put(CLAVE_STATUS, pago.getStatus());
    respuesta.put("status_detail", pago.getStatusDetail());
    return ResponseEntity.ok(respuesta);
  }

  private ResponseEntity<?> manejarErrorApi(MPApiException apiException) {
    String contenidoError = apiException.getMessage();
    if (
      apiException.getApiResponse() != null && apiException.getApiResponse().getContent() != null
    ) {
      contenidoError = apiException.getApiResponse().getContent();
    }

    if (LOGGER.isErrorEnabled()) {
      LOGGER.error(
        "Error API Mercado Pago (HTTP {}): {}",
        apiException.getStatusCode(),
        contenidoError
      );
    }

    Map<String, Object> respuesta = new HashMap<>();
    respuesta.put(CLAVE_STATUS, ESTADO_ERROR);
    respuesta.put(CLAVE_MENSAJE, contenidoError);
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(respuesta);
  }

  private ResponseEntity<?> manejarErrorSdk(MPException mpException) {
    if (LOGGER.isErrorEnabled()) {
      LOGGER.error("Error SDK Mercado Pago: ", mpException);
    }
    Map<String, Object> respuesta = new HashMap<>();
    respuesta.put(CLAVE_STATUS, ESTADO_ERROR);
    respuesta.put(CLAVE_MENSAJE, mpException.getMessage());
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(respuesta);
  }

  private ResponseEntity<?> manejarErrorGeneral(Exception excepcion) {
    if (LOGGER.isErrorEnabled()) {
      LOGGER.error("Error inesperado al procesar el pago: ", excepcion);
    }
    Map<String, Object> respuesta = new HashMap<>();
    respuesta.put(CLAVE_STATUS, ESTADO_ERROR);
    respuesta.put(CLAVE_MENSAJE, excepcion.getMessage());
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(respuesta);
  }

  private String obtenerErrorPassword(String password) {
    if (password == null || password.trim().isEmpty()) {
      return "La contraseña no puede estar vacía.";
    }
    String fortaleza = loginService.validarFortaleza(password);
    if (FORTALEZA_DEBIL.equalsIgnoreCase(fortaleza)) {
      return "La contraseña es demasiado débil. Requiere al menos 8 caracteres, 1 número y 1 símbolo especial.";
    }
    if (FORTALEZA_INVALIDA.equalsIgnoreCase(fortaleza)) {
      return "La contraseña no puede estar vacía.";
    }
    return null;
  }

  private void registrarAdminSiAplica(ProcesoPagoDTO dto, HttpSession session) throws Exception {
    if (dto.getPassword() == null || dto.getPassword().trim().isEmpty()) {
      return;
    }
    Usuario nuevoAdmin = new Usuario();
    nuevoAdmin.setEmail(dto.getEmail());
    nuevoAdmin.setPassword(dto.getPassword());
    nuevoAdmin.setRol(ROL_ADMIN);

    loginService.registrar(nuevoAdmin);

    session.setAttribute("ROL", nuevoAdmin.getRol());
    session.setAttribute("usuario", nuevoAdmin);
  }
}
