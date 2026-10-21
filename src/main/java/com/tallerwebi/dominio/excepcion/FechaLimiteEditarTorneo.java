package com.tallerwebi.dominio.excepcion;

public class FechaLimiteEditarTorneo extends Exception {

  private static final long serialVersionUID = 1L;

  public FechaLimiteEditarTorneo() {
    super();
  }

  public FechaLimiteEditarTorneo(String mensaje) {
    super(mensaje);
  }
}
