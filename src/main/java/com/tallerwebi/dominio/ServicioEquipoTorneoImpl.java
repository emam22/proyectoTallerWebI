package com.tallerwebi.dominio;

import com.tallerwebi.dominio.excepcion.EquipoExistente;
import com.tallerwebi.dominio.excepcion.EquipoYaInscripto;
import com.tallerwebi.dominio.excepcion.SiglasExistentes;
import jakarta.transaction.Transactional;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service("servicioEquipoTorneo")
@Transactional
public class ServicioEquipoTorneoImpl implements ServicioEquipoTorneo {

  private ServicioEquipo servicioEquipo;
  private RepositorioEquipoTorneo repositorioEquipoTorneo;
  private RepositorioJornada repositorioJornada;

  @Autowired
  public ServicioEquipoTorneoImpl(
    RepositorioEquipoTorneo repositorioEquipoTorneo,
    ServicioEquipo servicioEquipo,
    RepositorioJornada repositorioJornada
  ) {
    this.repositorioEquipoTorneo = repositorioEquipoTorneo;
    this.servicioEquipo = servicioEquipo;
    this.repositorioJornada = repositorioJornada;
  }

  @Override
  public void crearEquipoEInscribirlo(Equipo equipo, Torneo torneo)
    throws EquipoExistente, SiglasExistentes {
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

    this.repositorioEquipoTorneo.guardar(equipoTorneo);
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
  public void actualizarTablaPosiciones(Long idTorneo, Long idEquipoLocal, Long idEquipoVisitante, int golesLocal, int golesVisitante) {
    EquipoTorneo estadisticasLocal = consultarEquipoTorneoPorId(idEquipoLocal, idTorneo);
    EquipoTorneo estadisticasVisitante = consultarEquipoTorneoPorId(idEquipoVisitante, idTorneo);

    if(estadisticasLocal == null || estadisticasVisitante == null) {
      return;
    }

    estadisticasLocal.setPartidosJugados(estadisticasLocal.getPartidosJugados() + 1);
    estadisticasVisitante.setPartidosJugados(estadisticasVisitante.getPartidosJugados() + 1);

    estadisticasLocal.setGolesAFavor(estadisticasLocal.getGolesAFavor() + golesLocal);
    estadisticasLocal.setGolesEnContra(estadisticasLocal.getGolesEnContra() + golesVisitante);

    estadisticasVisitante.setGolesAFavor(estadisticasVisitante.getGolesAFavor() + golesVisitante);
    estadisticasVisitante.setGolesEnContra(estadisticasVisitante.getGolesEnContra() + golesLocal);

    //puntos
    if(golesLocal > golesVisitante) {
     estadisticasLocal.setPuntos(estadisticasLocal.getPuntos() + 3);
     estadisticasLocal.setPartidosGanados(estadisticasLocal.getPartidosGanados() + 1);
     estadisticasVisitante.setPartidoPerdidos(estadisticasVisitante.getPartidoPerdidos() + 1);
    }else if(golesVisitante > golesLocal) {
      estadisticasVisitante.setPuntos(estadisticasVisitante.getPuntos() + 3);
      estadisticasVisitante.setPartidosGanados(estadisticasVisitante.getPartidosGanados() + 1);
      estadisticasLocal.setPartidoPerdidos(estadisticasLocal.getPartidoPerdidos() + 1);
    }else if(golesLocal == golesVisitante) {
      estadisticasLocal.setPuntos(estadisticasLocal.getPuntos() + 1);
      estadisticasLocal.setPartidosEmpatados(estadisticasLocal.getPartidosEmpatados() + 1);

      estadisticasVisitante.setPuntos(estadisticasVisitante.getPuntos() + 1);
      estadisticasVisitante.setPartidosEmpatados(estadisticasVisitante.getPartidosEmpatados() + 1);
    }
  }
}