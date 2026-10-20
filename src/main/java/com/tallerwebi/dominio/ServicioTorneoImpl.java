package com.tallerwebi.dominio;

import com.tallerwebi.dominio.excepcion.FechaLimiteEditarTorneo;
import com.tallerwebi.dominio.excepcion.TorneoExistente;
import com.tallerwebi.infraestructura.RepositorioEquipoImpl;
import jakarta.transaction.Transactional;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service("ServicioTorneo")
@Transactional
public class ServicioTorneoImpl implements ServicioTorneo {

  /*Injectar servicio, no repositorio*/

  private final RepositorioEquipoImpl repositorioEquipo;
  private RepositorioTorneo repositorioTorneo;
  private RepositorioJornada repositorioJornada;

  private List<Equipo> equiposFechaLibreIda = new ArrayList<>();

  @Autowired
  public ServicioTorneoImpl(
    RepositorioTorneo repositorioTorneo,
    RepositorioEquipoImpl repositorioEquipo,
    RepositorioJornada repositorioJornada
  ) {
    this.repositorioTorneo = repositorioTorneo;
    this.repositorioEquipo = repositorioEquipo;
    this.repositorioJornada = repositorioJornada;
  }

  @Override
  public Torneo consultarTorneoPorId(Long id) {
    return repositorioTorneo.buscarTorneoPorId(id);
  }

  @Override
  public Torneo consultarTorneo(String nombre) {
    return repositorioTorneo.buscarTorneo(nombre);
  }

  @Override
  public void registrarTorneo(Torneo torneo) throws TorneoExistente, Exception {
    Torneo torneoEncontrado = repositorioTorneo.buscarTorneo(torneo.getNombre());

    if (torneoEncontrado != null) {
      throw new TorneoExistente();
    }

    repositorioTorneo.guardar(torneo);
  }

  @Override
  public List<Torneo> obtenerTorneos() {
    return repositorioTorneo.listar();
  }

  @Override
  public boolean consultarAunEnFechaParaEditar(Long idTorneo) {
    Torneo torneoActual = repositorioTorneo.buscarTorneoPorId(idTorneo);
    if (torneoActual.permiteModificaciones() == false) {
      return false;
    }
    return true;
  }

  @Override
  public List<Jornada> obtenerJornadasPorTorneo(Long torneoId) {
    return repositorioJornada.obtenerJornadasPorTorneo(torneoId);
  }

  @Override
  public void crearTorneoConFixture(Torneo torneo, List<Long> idsEquipos)
    throws TorneoExistente, Exception {
    validarDatosFixture(torneo, idsEquipos);

    List<Equipo> equipos = buscarEquipos(idsEquipos);
    agregarFechaLibreSiCorresponde(equipos);

    List<List<Equipo[]>> partidosDeIda = crearJornadasDeIda(torneo, equipos);

    if (Boolean.TRUE.equals(torneo.getIdaYVuelta())) {
      crearJornadasDeVuelta(torneo, partidosDeIda);
    }
  }

  @Override
  public long contarFechasFinalizadas(Long idTorneo) {
    Torneo torneo = consultarTorneoPorId(idTorneo);

    if (torneo == null || torneo.getJornadas() == null) {
      return 0;
    }

    return torneo
      .getJornadas()
      .stream()
      .filter(jornada -> "Finalizada".equalsIgnoreCase(jornada.getEstado()))
      .count();
  }

  private void validarDatosFixture(Torneo torneo, List<Long> idsEquipos) {
    if (idsEquipos == null || idsEquipos.size() < 2) {
      throw new IllegalArgumentException("Debe tener al menos dos equipos");
    }

    if (
      torneo.getCantidadDeEquipos() == null ||
      !torneo.getCantidadDeEquipos().equals(idsEquipos.size())
    ) {
      throw new IllegalArgumentException(
        "La cantidad de equipos no coincide con " + "la configurada del torneo"
      );
    }
  }

  private List<Equipo> buscarEquipos(List<Long> idsEquipos) {
    List<Equipo> equipos = new ArrayList<>();

    for (Long idEquipo : idsEquipos) {
      Equipo equipo = repositorioEquipo.buscarPorId(idEquipo);

      if (equipo == null) {
        throw new IllegalArgumentException("No existe un equipo con ese ID");
      }

      equipos.add(equipo);
    }

    return equipos;
  }

  private void agregarFechaLibreSiCorresponde(List<Equipo> equipos) {
    if (equipos.size() % 2 != 0) {
      equipos.add(null);
    }
  }

  private List<List<Equipo[]>> crearJornadasDeIda(Torneo torneo, List<Equipo> equipos) {
    int cantidadJornadas = equipos.size() - 1;
    int partidosPorJornada = equipos.size() / 2;

    List<List<Equipo[]>> partidosDeIda = new ArrayList<>();

    for (int numero = 1; numero <= cantidadJornadas; numero++) {
      Jornada jornada = new Jornada();
      jornada.setNumero(numero);
      jornada.setTorneo(torneo);

      List<Equipo[]> enfrentamientos = crearPartidosDeJornada(equipos, jornada, partidosPorJornada);

      equiposFechaLibreIda.add(jornada.getEquipoFechaLibre());

      partidosDeIda.add(enfrentamientos);

      torneo.agregarJornada(jornada); //se agrega esta linea para agregar la jornada al torneo

      repositorioJornada.guardar(jornada);

      rotarEquipos(equipos);
    }

    return partidosDeIda;
  }

  private List<Equipo[]> crearPartidosDeJornada(
    List<Equipo> equipos,
    Jornada jornada,
    int partidosPorJornada
  ) {
    List<Equipo[]> enfrentamientos = new ArrayList<>();

    for (int i = 0; i < partidosPorJornada; i++) {
      Equipo local = equipos.get(i);
      Equipo visitante = equipos.get(equipos.size() - 1 - i);

      if (local == null) {
        jornada.setEquipoFechaLibre(visitante);
      } else if (visitante == null) {
        jornada.setEquipoFechaLibre(local);
      } else {
        Partido partido = new Partido();
        partido.setEquipoLocal(local);
        partido.setEquipoVisitante(visitante);

        jornada.agregarPartido(partido);
        enfrentamientos.add(new Equipo[] { local, visitante });
      }
    }

    return enfrentamientos;
  }

  private void rotarEquipos(List<Equipo> equipos) {
    Equipo ultimoEquipo = equipos.remove(equipos.size() - 1);
    equipos.add(1, ultimoEquipo);
  }

  private void crearJornadasDeVuelta(Torneo torneo, List<List<Equipo[]>> partidosDeIda) {
    int cantidadJornadasIda = partidosDeIda.size();

    for (int i = 0; i < cantidadJornadasIda; i++) {
      Jornada jornada = new Jornada();
      jornada.setNumero(cantidadJornadasIda + i + 1);
      jornada.setTorneo(torneo);

      jornada.setEquipoFechaLibre(equiposFechaLibreIda.get(i));

      for (Equipo[] enfrentamiento : partidosDeIda.get(i)) {
        Partido partido = new Partido();
        partido.setEquipoLocal(enfrentamiento[1]);
        partido.setEquipoVisitante(enfrentamiento[0]);
        jornada.agregarPartido(partido);
      }

      torneo.agregarJornada(jornada);
      repositorioJornada.guardar(jornada);
    }
  }
}
