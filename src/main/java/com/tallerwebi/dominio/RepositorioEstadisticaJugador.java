package com.tallerwebi.dominio;

import java.util.List;

public interface RepositorioEstadisticaJugador {
  void guardar(EstadisticaJugador estadisticaJugador);
  List<EstadisticaJugador> obtenerEstadisticas();
}
