package com.tallerwebi.dominio;

import com.tallerwebi.dominio.excepcion.EquipoExistente;
import jakarta.transaction.Transactional;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service("servicioEquipoTorneo")
@Transactional
public class ServicioEquipoTorneoImpl implements ServicioEquipoTorneo {

  private ServicioEquipo servicioEquipo;
  private RepositorioEquipoTorneo repositorioEquipoTorneo;

  @Autowired
  public ServicioEquipoTorneoImpl(
    RepositorioEquipoTorneo repositorioEquipoTorneo,
    ServicioEquipo servicioEquipo
  ) {
    this.repositorioEquipoTorneo = repositorioEquipoTorneo;
    this.servicioEquipo = servicioEquipo;
  }

  @Override
  public void crearEquipoEInscribirlo(Equipo equipo, Torneo torneo) throws EquipoExistente {
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
  public List<EquipoTorneo> obtenerEstadisticas(Long idTorneo) {
    List<EquipoTorneo> estadisticas = repositorioEquipoTorneo.obtenerTablaPosiciones(idTorneo);
    return estadisticas;
  }
}
