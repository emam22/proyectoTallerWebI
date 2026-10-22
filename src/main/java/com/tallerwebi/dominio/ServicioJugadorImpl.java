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
  public void habilitarJugador(Long idJugador, boolean habilitado) {
    Jugador jugador = repositorioJugador.obtenerJugador(idJugador);

    if (jugador != null) {
      jugador.setHabilitado(habilitado);
      repositorioJugador.actualizar(jugador);
    }
  }
}
