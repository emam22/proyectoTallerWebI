package com.tallerwebi.dominio;

import java.util.List;

public interface RepositorioJugador {
  void guardar(Jugador jugador);
  void eliminar(Long idJugador);
  List<Jugador> listarPorEquipo(Long idEquipo);
}
