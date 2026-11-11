package com.tallerwebi.dominio;

import java.util.List;

public interface RepositorioEstadisticaJugador {
  void guardar(EstadisticaJugador estadisticaJugador);
  List<EstadisticaJugador> obtenerEstadisticas();
  void eliminarFilaEstadistica(Long idJugador);
  EstadisticaJugador buscarEstadisticasPorIdJugador(Long idJugador);
  void actualizarEstadisticasJugador(EstadisticaJugador estadisticas);
  List<EstadisticaJugador> obtenerEstadisticasPorTorneo(Long idTorneo);
  EstadisticaJugador buscarEstadisticasPorJugadorYTorneo(Long idJugador, Long idTorneo);
}
