package com.tallerwebi.dominio;

import java.util.List;

public interface ServicioJugador {
  List<Jugador> obtenerJugadores(Long idEquipo);
  List<String> obtenerPosicionesPresentes(Long idEquipo);
  void agregarJugador(Long idEquipo, Jugador jugador);
  void eliminarJugador(Long idEquipo, Long idJugador);
  void habilitarJugador(Long idJugador, boolean habilitado);
  Jugador consultarJugador(long idJugador);
  void sancionarJugador(Long idJugador, Integer fechas);
  void cumplirFechaSuspension(Long idJugador);
  void agregarFechasSuspension(Long idJugador, Integer fechas);
  void inscribirJugadorEnTorneo(Long idJugador, Long idEquipo, Long idTorneo);
  void sancionarJugadorEnTorneo(Long idJugador, Long idTorneo, Integer fechas);
  void agregarFechasSuspensionEnTorneo(Long idJugador, Long idTorneo, Integer fechas);
  void cumplirFechaSuspensionEnTorneo(Long idJugador, Long idTorneo);
  List<JugadorEquipoTorneo> listarInscripcionesPorEquipoYTorneo(Long idEquipo, Long idTorneo);
}
