package com.tallerwebi.dominio;

import com.mercadopago.resources.payment.Payment;
import com.tallerwebi.presentacion.ProcesoPagoDTO;

@FunctionalInterface
public interface ServicioMercadoPago {
  Payment crearPago(ProcesoPagoDTO dto) throws Exception;
}
