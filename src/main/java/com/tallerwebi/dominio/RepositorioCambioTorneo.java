package com.tallerwebi.dominio;

import java.util.List;

public interface RepositorioCambioTorneo {
  void guardar(CambioTorneo cambio);

  List<CambioTorneo> buscarPorTorneo(Long idTorneo);
}
