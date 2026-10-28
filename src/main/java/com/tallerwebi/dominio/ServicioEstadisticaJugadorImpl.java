package com.tallerwebi.dominio;

import com.tallerwebi.dominio.excepcion.JugadorNoExiste;
import jakarta.transaction.Transactional;
import java.util.List;
import org.springframework.stereotype.Service;

@Service("ServicioEstadisticasJugador")
@Transactional
public class ServicioEstadisticaJugadorImpl implements ServicioEstadisticaJugador {

  private final ServicioJugador servicioJugador;
  private RepositorioEstadisticaJugador repoEstadisticas;
  ServicioTorneo servicioTorneo;

  public ServicioEstadisticaJugadorImpl(
    ServicioJugador servicioJugador,
    RepositorioEstadisticaJugador repoEstadisticas,
    ServicioTorneo servicioTorneo
  ) {
    this.servicioJugador = servicioJugador;
    this.repoEstadisticas = repoEstadisticas;
    this.servicioTorneo = servicioTorneo;
  }

  @Override
  public void iniciarEstadisticas(Jugador jugador) throws JugadorNoExiste {
    Jugador jugadorCreado = servicioJugador.consultarJugador(jugador.getId());
    if (jugadorCreado == null) {
      throw new JugadorNoExiste();
    }

    EstadisticaJugador estadisticas = new EstadisticaJugador();

    estadisticas.setJugador(jugador);
    estadisticas.setGoles(0);
    estadisticas.setAsistencias(0);
    estadisticas.setParticipacionesGol(0);
    estadisticas.setAmarillas(0);
    estadisticas.setRojas(0);

    repoEstadisticas.guardar(estadisticas);
  }

  @Override
  public List<EstadisticaJugador> obtenerEstadisticas() {
    List<EstadisticaJugador> estadisticas = repoEstadisticas.obtenerEstadisticas();
    return estadisticas;
  }

  @Override
  public void eliminarEstadisticas(Long idJugador) {
    repoEstadisticas.eliminarFilaEstadistica(idJugador);
  }

  @Override
  public void registrarTarjetaAmarilla(Long idJugador, Long idTorneo) {
    Torneo torneo = servicioTorneo.consultarTorneoPorId(idTorneo);
    EstadisticaJugador estadisticas = obtenerEstadisticasValidadas(idJugador, idTorneo);

    Integer limite = torneo.getCantidadAmaSusp();

    if (limite == null || limite <= 0) {
      throw new IllegalArgumentException(
        "El torneo no tiene configurado un límite válido de amarillas."
      );
    }

    int amarillasAntes = valorOZero(estadisticas.getAmarillas());
    int amarillasDespues = amarillasAntes + 1;

    estadisticas.setAmarillas(amarillasDespues);
    repoEstadisticas.actualizarEstadisticasJugador(estadisticas);

    if (amarillasDespues % limite == 0) {
      servicioJugador.agregarFechasSuspension(idJugador, 1);
    }
  }

  //este metodo devuelve 0 aunque sea null
  private int valorOZero(Integer valor) {
    return valor == null ? 0 : valor;
  }

  @Override
  public void registrarTarjetaRoja(Long idJugador, Long idTorneo) {
    EstadisticaJugador estadisticas = obtenerEstadisticasValidadas(idJugador, idTorneo);

    int rojas = valorOZero(estadisticas.getRojas());

    estadisticas.setRojas(rojas + 1);

    repoEstadisticas.actualizarEstadisticasJugador(estadisticas);

    servicioJugador.agregarFechasSuspension(idJugador, 1);
  }

  private EstadisticaJugador obtenerEstadisticasValidadas(Long idJugador, Long idTorneo) {
    Jugador jugador = servicioJugador.consultarJugador(idJugador);
    Torneo torneo = servicioTorneo.consultarTorneoPorId(idTorneo);

    EstadisticaJugador estadisticas = repoEstadisticas.buscarEstadisticasPorIdJugador(idJugador);

    if (jugador == null || torneo == null || estadisticas == null) {
      throw new IllegalArgumentException(
        "No se encontró el jugador, el torneo o sus estadísticas."
      );
    }

    return estadisticas;
  }

  @Override
  public void sumarPartidoJugado(Long idJugador) {
    EstadisticaJugador estadisticas = repoEstadisticas.buscarEstadisticasPorIdJugador(idJugador);
    if (estadisticas != null) {
      estadisticas.setPartidosJugados(estadisticas.getPartidosJugados() + 1);
      repoEstadisticas.actualizarEstadisticasJugador(estadisticas);
    }
  }

  @Override
  public void registrarGol(Long idJugador) {
    EstadisticaJugador estadisticas = repoEstadisticas.buscarEstadisticasPorIdJugador(idJugador);
    if (estadisticas != null) {
      int goles = estadisticas.getGoles() + 1;
      estadisticas.setGoles(goles);
      // Participación de gol = goles + asistencias
      estadisticas.setParticipacionesGol(goles + estadisticas.getAsistencias());
      repoEstadisticas.actualizarEstadisticasJugador(estadisticas);
    }
  }

  @Override
  public void registrarAsistencia(long idJugador) {
    EstadisticaJugador estadisticas = repoEstadisticas.buscarEstadisticasPorIdJugador(idJugador);
    if (estadisticas != null) {
      int asistencias = estadisticas.getAsistencias() + 1;
      estadisticas.setAsistencias(asistencias);
      // Participación de gol = goles + asistencias
      estadisticas.setParticipacionesGol(estadisticas.getGoles() + asistencias);
      repoEstadisticas.actualizarEstadisticasJugador(estadisticas);
    }
  }
}
