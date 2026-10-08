package com.tallerwebi.dominio;

public interface RepositorioEquipoTorneo {
  void guardar(EquipoTorneo equipoTorneo);
  int contarEquiposPorTorneo(Long idTorneo);
  boolean existeEquipoEnTorneo(Long idEquipo, Long idTorneo);
}
