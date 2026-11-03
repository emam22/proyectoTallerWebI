package com.tallerwebi.dominio;

import jakarta.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service("servicioJugador")
@Transactional
public class ServicioJugadorImpl implements ServicioJugador {

  private static final String ESTADO_SANCIONADO = "SANCIONADO";

  private final RepositorioJugador repositorioJugador;
  private final RepositorioEquipo repositorioEquipo;
  private final RepositorioTorneo repositorioTorneo;
  private final RepositorioJugadorEquipoTorneo repositorioJugadorEquipoTorneo;

  @Autowired
  public ServicioJugadorImpl(
    RepositorioJugador repositorioJugador,
    RepositorioEquipo repositorioEquipo,
    RepositorioTorneo repositorioTorneo,
    RepositorioJugadorEquipoTorneo repositorioJugadorEquipoTorneo
  ) {
    this.repositorioJugador = repositorioJugador;
    this.repositorioEquipo = repositorioEquipo;
    this.repositorioTorneo = repositorioTorneo;
    this.repositorioJugadorEquipoTorneo = repositorioJugadorEquipoTorneo;
  }

  @Override
  public List<Jugador> obtenerJugadores(Long idEquipo) {
    return repositorioJugador.listarPorEquipo(idEquipo);
  }

  @Override
  public List<String> obtenerPosicionesPresentes(Long idEquipo) {
    return repositorioJugador.obtenerPosicionesPresentes(idEquipo);
  }

  @Override
  public void agregarJugador(Long idEquipo, Jugador jugador) {
    repositorioJugador.guardar(jugador);

    Equipo equipo = repositorioEquipo.buscarPorId(idEquipo);
    if (equipo != null) {
      if (equipo.getJugadores() == null) {
        equipo.setJugadores(new ArrayList<>());
      }
      equipo.getJugadores().add(jugador);
      repositorioEquipo.guardar(equipo);
    }
  }

  @Override
  public void eliminarJugador(Long idEquipo, Long idJugador) {
    Equipo equipo = repositorioEquipo.buscarPorId(idEquipo);
    if (equipo != null && equipo.getJugadores() != null) {
      equipo.getJugadores().removeIf(jugador -> idJugador.equals(jugador.getId()));
      repositorioEquipo.guardar(equipo);
    }
    repositorioJugador.eliminar(idJugador);
  }

  @Override
  public Jugador consultarJugador(long idJugador) {
    return repositorioJugador.obtenerJugador(idJugador);
  }

  @Override
  public void sancionarJugador(Long idJugador, Integer fechas) {
    Jugador jugador = repositorioJugador.obtenerJugador(idJugador);

    if (jugador == null) {
      return;
    }

    if (fechas == null || fechas <= 0) {
      throw new IllegalArgumentException("La cantidad de fechas debe ser mayor a cero.");
    }

    Integer fechasPendientes = jugador.getFechasSuspensionPendientes();

    if (fechasPendientes == null) {
      fechasPendientes = 0;
    }

    jugador.setFechasSuspensionPendientes(fechasPendientes + fechas);
    jugador.setEstadoSancion(ESTADO_SANCIONADO);

    // jugador.setHabilitado(false);

    repositorioJugador.actualizar(jugador);
  }

  @Override
  public void cumplirFechaSuspension(Long idJugador) {
    Jugador jugador = repositorioJugador.obtenerJugador(idJugador);

    if (jugador == null) {
      return;
    }

    Integer pendientes = jugador.getFechasSuspensionPendientes();

    if (pendientes == null || pendientes <= 0) {
      return;
    }

    pendientes--;
    jugador.setFechasSuspensionPendientes(pendientes);

    if (pendientes == 0) {
      jugador.setEstadoSancion("SIN_SANCION");
    } else {
      jugador.setEstadoSancion(ESTADO_SANCIONADO);
    }

    repositorioJugador.actualizar(jugador);
  }

  @Override
  public void agregarFechasSuspension(Long idJugador, Integer fechas) {
    Jugador jugador = repositorioJugador.obtenerJugador(idJugador);

    if (jugador == null || fechas == null || fechas <= 0) {
      return;
    }

    int pendientes = jugador.getFechasSuspensionPendientes() == null
      ? 0
      : jugador.getFechasSuspensionPendientes();

    jugador.setFechasSuspensionPendientes(pendientes + fechas);
    jugador.setEstadoSancion(ESTADO_SANCIONADO);

    repositorioJugador.actualizar(jugador);
  }

  @Override
  public void inscribirJugadorEnTorneo(Long idJugador, Long idEquipo, Long idTorneo) {
    if (idJugador == null || idEquipo == null || idTorneo == null) {
      return;
    }

    if (repositorioJugadorEquipoTorneo.buscarPorJugadorYTorneo(idJugador, idTorneo) != null) {
      return;
    }

    Jugador jugador = repositorioJugador.obtenerJugador(idJugador);
    Equipo equipo = repositorioEquipo.buscarPorId(idEquipo);
    Torneo torneo = repositorioTorneo.buscarTorneoPorId(idTorneo);

    if (jugador == null || equipo == null || torneo == null) {
      return;
    }

    JugadorEquipoTorneo inscripcion = new JugadorEquipoTorneo();
    inscripcion.setJugador(jugador);
    inscripcion.setEquipo(equipo);
    inscripcion.setTorneo(torneo);

    repositorioJugadorEquipoTorneo.guardar(inscripcion);
  }

  @Override
  public void sancionarJugadorEnTorneo(Long idJugador, Long idTorneo, Integer fechas) {
    if (fechas == null || fechas <= 0) {
      throw new IllegalArgumentException("La cantidad de fechas debe ser mayor a cero.");
    }

    JugadorEquipoTorneo inscripcion = repositorioJugadorEquipoTorneo.buscarPorJugadorYTorneo(
      idJugador,
      idTorneo
    );

    if (inscripcion == null) {
      return;
    }

    Integer pendientes = inscripcion.getFechasSuspensionPendientes() == null
      ? 0
      : inscripcion.getFechasSuspensionPendientes();

    inscripcion.setFechasSuspensionPendientes(pendientes + fechas);
    inscripcion.setEstadoSancion(ESTADO_SANCIONADO);
    repositorioJugadorEquipoTorneo.guardar(inscripcion);
  }

  @Override
  public void agregarFechasSuspensionEnTorneo(Long idJugador, Long idTorneo, Integer fechas) {
    if (fechas == null || fechas <= 0) {
      return;
    }

    sancionarJugadorEnTorneo(idJugador, idTorneo, fechas);
  }

  @Override
  public void cumplirFechaSuspensionEnTorneo(Long idJugador, Long idTorneo) {
    JugadorEquipoTorneo inscripcion = repositorioJugadorEquipoTorneo.buscarPorJugadorYTorneo(
      idJugador,
      idTorneo
    );

    if (inscripcion == null) {
      return;
    }

    Integer pendientes = inscripcion.getFechasSuspensionPendientes();

    if (pendientes == null || pendientes <= 0) {
      return;
    }

    pendientes--;
    inscripcion.setFechasSuspensionPendientes(pendientes);
    inscripcion.setEstadoSancion(pendientes == 0 ? "SIN_SANCION" : ESTADO_SANCIONADO);
    repositorioJugadorEquipoTorneo.guardar(inscripcion);
  }

  @Override
  public List<JugadorEquipoTorneo> listarInscripcionesPorEquipoYTorneo(
    Long idEquipo,
    Long idTorneo
  ) {
    return repositorioJugadorEquipoTorneo.listarPorEquipoYTorneo(idEquipo, idTorneo);
  }

  @Override
  public void habilitarJugador(Long idJugador, boolean habilitado) {
    Jugador jugador = repositorioJugador.obtenerJugador(idJugador);

    if (jugador != null) {
      jugador.setHabilitado(habilitado);
      repositorioJugador.actualizar(jugador);
    }
  }
}
