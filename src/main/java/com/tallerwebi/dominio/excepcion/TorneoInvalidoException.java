package com.tallerwebi.dominio.excepcion;

public class TorneoInvalidoException extends Exception {

  private static final long serialVersionUID = 1L;

  public TorneoInvalidoException(String mensaje) {
    super(mensaje);
  }
}
