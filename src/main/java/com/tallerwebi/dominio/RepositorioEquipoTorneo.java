package com.tallerwebi.dominio;

import java.util.List;

public interface RepositorioEquipoTorneo {
  void guardar(EquipoTorneo equipoTorneo);
  int contarEquiposPorTorneo(Long idTorneo);
  boolean existeEquipoEnTorneo(Long idEquipo, Long idTorneo);
  List<EquipoTorneo> obtenerTablaPosiciones(Long idTorneo);
}
