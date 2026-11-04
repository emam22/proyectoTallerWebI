package com.tallerwebi.presentacion;

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
import org.springframework.web.bind.annotation.*;
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
  public ControladorPago(ServicioMercadoPago mpservice, ServicioLogin loginService) {
    this.mpService = mpservice;
    this.loginService = loginService;
  }

  @GetMapping("/pagar-inscripcion")
  public ModelAndView irAPagarInscripcion() {
    ModelAndView modelAndView = new ModelAndView("pagoInscripcion");
    modelAndView.addObject("monto", 5000.00);
    return modelAndView;
  }

  @PostMapping("/api/pagos/procesar")
  @ResponseBody
  public ResponseEntity<?> procesarPago(@RequestBody ProcesoPagoDTO dto, HttpSession session) {
    Map<String, Object> respuesta = new HashMap<>();

    try {
      // 1. Validar contraseña antes de invocar pasarela
      String errorValidacion = obtenerErrorPassword(dto.getPassword());
      if (errorValidacion != null) {
        respuesta.put(CLAVE_STATUS, ESTADO_ERROR);
        respuesta.put(CLAVE_MENSAJE, errorValidacion);
        return ResponseEntity.badRequest().body(respuesta);
      }

      // 2. Transacción Mercado Pago
      Payment pago = mpService.crearPago(dto);

      // 3. Resultado de pago
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
    } catch (Exception e) {
      LOGGER.error("Error al procesar el pago o registrar el admin", e);
      respuesta.put(CLAVE_STATUS, ESTADO_ERROR);
      respuesta.put(CLAVE_MENSAJE, e.getMessage());
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(respuesta);
    }
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
