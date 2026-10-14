package com.tallerwebi.dominio;

import java.util.List;

public interface ServicioJugador {
  List<Jugador> obtenerJugadores(Long idEquipo);
  void agregarJugador(Long idEquipo, Jugador jugador);
}
