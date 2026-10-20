package com.tallerwebi.dominio;

import com.tallerwebi.dominio.excepcion.JugadorNoExiste;
import java.util.List;

public interface ServicioEstadisticaJugador {
  void iniciarEstadisticas(Jugador Jugador) throws JugadorNoExiste;
  List<EstadisticaJugador> obtenerEstadisticas();
  void eliminarEstadisticas(Long idJugador);
}
