package com.tallerwebi.dominio;

import jakarta.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service("servicioJugador")
@Transactional
public class ServicioJugadorImpl implements ServicioJugador {

  private final RepositorioJugador repositorioJugador;
  private final RepositorioEquipo repositorioEquipo;

  @Autowired
  public ServicioJugadorImpl(
    RepositorioJugador repositorioJugador,
    RepositorioEquipo repositorioEquipo
  ) {
    this.repositorioJugador = repositorioJugador;
    this.repositorioEquipo = repositorioEquipo;
  }

  @Override
  public List<Jugador> obtenerJugadores(Long idEquipo) {
    return repositorioJugador.listarPorEquipo(idEquipo);
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

    jugador.setFechasSuspensionPendientes(fechas);
    jugador.setEstadoSancion("SANCIONADO");
    //  jugador.setHabilitado(false);

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
      jugador.setEstadoSancion("SANCIONADO");
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
    jugador.setEstadoSancion("SANCIONADO");

    repositorioJugador.actualizar(jugador);
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
