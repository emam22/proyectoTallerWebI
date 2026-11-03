package com.tallerwebi.dominio;

import java.util.List;

public interface RepositorioJugadorEquipoTorneo {
  void guardar(JugadorEquipoTorneo inscripcion);
  JugadorEquipoTorneo buscarPorJugadorYTorneo(Long idJugador, Long idTorneo);
  List<JugadorEquipoTorneo> listarPorEquipoYTorneo(Long idEquipo, Long idTorneo);
}
