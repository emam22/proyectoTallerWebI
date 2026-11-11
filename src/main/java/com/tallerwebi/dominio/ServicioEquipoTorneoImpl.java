package com.tallerwebi.dominio;

import com.tallerwebi.dominio.excepcion.EquipoExistente;
import com.tallerwebi.dominio.excepcion.EquipoYaInscripto;
import com.tallerwebi.dominio.excepcion.PlantelIncompletoException;
import com.tallerwebi.dominio.excepcion.SiglaExistente;
import com.tallerwebi.dominio.excepcion.SiglaInvalida;
import jakarta.transaction.Transactional;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service("servicioEquipoTorneo")
@Transactional
public class ServicioEquipoTorneoImpl implements ServicioEquipoTorneo {

  private static final List<String> POSICIONES_PLANTEL = List.of(
    "Arquero",
    "Defensor",
    "Mediocampista",
    "Delantero"
  );

  private ServicioEquipo servicioEquipo;
  private ServicioJugador servicioJugador;
  private RepositorioEquipoTorneo repositorioEquipoTorneo;
  private RepositorioJornada repositorioJornada;

  @Autowired
  public ServicioEquipoTorneoImpl(
    RepositorioEquipoTorneo repositorioEquipoTorneo,
    ServicioEquipo servicioEquipo,
    ServicioJugador servicioJugador,
    RepositorioJornada repositorioJornada
  ) {
    this.repositorioEquipoTorneo = repositorioEquipoTorneo;
    this.servicioEquipo = servicioEquipo;
    this.servicioJugador = servicioJugador;
    this.repositorioJornada = repositorioJornada;
  }

  @Override
  public void crearEquipoEInscribirlo(Equipo equipo, Torneo torneo)
    throws EquipoExistente, SiglaExistente, SiglaInvalida {
    Equipo equipoExistente = servicioEquipo.consultarEquipo(equipo.getNombre());

    if (equipoExistente != null) {
      throw new EquipoExistente();
    }

    servicioEquipo.registrarEquipo(equipo);
    equipoExistente = equipo;

    EquipoTorneo equipoTorneo = new EquipoTorneo();

    equipoTorneo.setEquipo(equipoExistente);
    equipoTorneo.setTorneo(torneo);

    equipoTorneo.setPuntos(0);
    equipoTorneo.setPartidosJugados(0);
    equipoTorneo.setPartidosGanados(0);
    equipoTorneo.setPartidoPerdidos(0);
    equipoTorneo.setPartidosEmpatados(0);
    equipoTorneo.setGolesAFavor(0);
    equipoTorneo.setGolesEnContra(0);

    repositorioEquipoTorneo.guardar(equipoTorneo);
  }

  @Override
  public int contarEquiposPorTorneo(Long idTorneo) {
    return repositorioEquipoTorneo.contarEquiposPorTorneo(idTorneo);
  }

  /*@Override
  public void inscribirEquipoExistente(Equipo equipo, Torneo torneo) throws EquipoYaInscripto {
    boolean yaInscripto =
      this.repositorioEquipoTorneo.existeEquipoEnTorneo(equipo.getId(), torneo.getId());

    if (yaInscripto) {
      throw new EquipoYaInscripto();
    }

    EquipoTorneo equipoTorneo = new EquipoTorneo();

    equipoTorneo.setEquipo(equipo);
    equipoTorneo.setTorneo(torneo);

    equipoTorneo.setPuntos(0);
    equipoTorneo.setPartidosJugados(0);
    equipoTorneo.setPartidosGanados(0);
    equipoTorneo.setPartidoPerdidos(0);
    equipoTorneo.setPartidosEmpatados(0);
    equipoTorneo.setGolesAFavor(0);
    equipoTorneo.setGolesEnContra(0);

    this.repositorioEquipoTorneo.guardar(equipoTorneo);
  }*/

  @Override
  public void inscribirEquipoExistente(Equipo equipo, Torneo torneo) throws EquipoYaInscripto {
    boolean yaInscripto =
      this.repositorioEquipoTorneo.existeEquipoEnTorneo(equipo.getId(), torneo.getId());

    if (yaInscripto) {
      throw new EquipoYaInscripto();
    }

    EquipoTorneo equipoTorneo = new EquipoTorneo();

    equipoTorneo.setEquipo(equipo);
    equipoTorneo.setTorneo(torneo);

    equipoTorneo.setPuntos(0);
    equipoTorneo.setPartidosJugados(0);
    equipoTorneo.setPartidosGanados(0);
    equipoTorneo.setPartidoPerdidos(0);
    equipoTorneo.setPartidosEmpatados(0);
    equipoTorneo.setGolesAFavor(0);
    equipoTorneo.setGolesEnContra(0);

    repositorioEquipoTorneo.guardar(equipoTorneo);

    List<Jugador> jugadores = servicioJugador.obtenerJugadores(equipo.getId());

    for (Jugador jugador : jugadores) {
      servicioJugador.inscribirJugadorEnTorneo(jugador.getId(), equipo.getId(), torneo.getId());
    }
  }

  @Override
  public List<EquipoTorneo> obtenerEstadisticas(Long idTorneo) {
    List<EquipoTorneo> estadisticas = repositorioEquipoTorneo.obtenerTablaPosiciones(idTorneo);
    return estadisticas;
  }

  @Override
  public List<EquipoTorneo> obtenerEquiposPorTorneo(Long idTorneo) {
    return repositorioEquipoTorneo.obtenerEquiposPorTorneo(idTorneo);
  }

  @Override
  public void desvincularEquipo(Long idEquipo, Long idTorneo) {
    EquipoTorneo equipoTorneo = consultarEquipoTorneoPorId(idEquipo, idTorneo);

    if (equipoTorneo == null) {
      return;
    }

    repositorioJornada.eliminarFixturePorTorneo(idTorneo);
    repositorioEquipoTorneo.eliminar(equipoTorneo);
  }

  @Override
  public EquipoTorneo consultarEquipoTorneoPorId(Long idEquipo, Long idTorneo) {
    return repositorioEquipoTorneo.buscarEquipoTorneoPorId(idEquipo, idTorneo);
  }

  @Override
  public void confirmarPlantel(Long idEquipo) throws PlantelIncompletoException {
    Set<String> posicionesPresentes = new HashSet<>(
      servicioJugador.obtenerPosicionesPresentes(idEquipo)
    );
    List<String> posicionesFaltantes = new ArrayList<>();

    for (String posicion : POSICIONES_PLANTEL) {
      if (!posicionesPresentes.contains(posicion.toLowerCase(java.util.Locale.ROOT))) {
        posicionesFaltantes.add(posicion);
      }
    }

    if (!posicionesFaltantes.isEmpty()) {
      throw new PlantelIncompletoException(posicionesFaltantes);
    }
  }

  @Override
  public void actualizarTablaPosiciones(
    Long idTorneo,
    Long idEquipoLocal,
    Long idEquipoVisitante,
    int golesLocal,
    int golesVisitante
  ) {
    EquipoTorneo estadisticasLocal = consultarEquipoTorneoPorId(idEquipoLocal, idTorneo);
    EquipoTorneo estadisticasVisitante = consultarEquipoTorneoPorId(idEquipoVisitante, idTorneo);

    if (estadisticasLocal == null || estadisticasVisitante == null) {
      return;
    }

    estadisticasLocal.registrarPartido(golesLocal, golesVisitante);
    estadisticasVisitante.registrarPartido(golesVisitante, golesLocal);
  }
}
