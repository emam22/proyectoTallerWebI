package com.tallerwebi.dominio;

import com.tallerwebi.presentacion.ResultadoPartidoDTO;

@SuppressWarnings("PMD.ImplicitFunctionalInterface")
public interface ServicioPartido {
  void registrarResultado(ResultadoPartidoDTO resultado);
}
