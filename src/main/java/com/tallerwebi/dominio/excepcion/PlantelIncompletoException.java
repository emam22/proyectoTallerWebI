package com.tallerwebi.dominio.excepcion;

import java.util.List;

public class PlantelIncompletoException extends Exception {

  private static final long serialVersionUID = 1L;

  private final List<String> posicionesFaltantes;

  public PlantelIncompletoException(List<String> posicionesFaltantes) {
    this.posicionesFaltantes = posicionesFaltantes;
  }

  public List<String> getPosicionesFaltantes() {
    return posicionesFaltantes;
  }
}
