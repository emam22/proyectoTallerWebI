package com.tallerwebi.dominio;

import java.util.List;

public interface RepositorioFixture {
  List<Fecha> buscarFechasPorTorneo(Long idTorneo);
  void guardarFecha(Fecha fecha);
  void guardarPartido(Partido partido);
}
