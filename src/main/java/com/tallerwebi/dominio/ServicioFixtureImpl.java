package com.tallerwebi.dominio;

import com.tallerwebi.dominio.excepcion.FixtureInvalidoException;
import jakarta.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service("ServicioFixture")
@Transactional
public class ServicioFixtureImpl implements ServicioFixture {

  private static final int EQUIPOS_MINIMOS = 2;

  private RepositorioEquipoTorneo repositorioEquipoTorneo;
  private RepositorioFixture repositorioFixture;

  @Autowired
  public ServicioFixtureImpl(
    RepositorioEquipoTorneo repositorioEquipoTorneo,
    RepositorioFixture repositorioFixture
  ) {
    this.repositorioEquipoTorneo = repositorioEquipoTorneo;
    this.repositorioFixture = repositorioFixture;
  }

  @Override
  public List<Fecha> generarFixture(Long idTorneo) throws FixtureInvalidoException {
    List<EquipoTorneo> inscriptos = repositorioEquipoTorneo.buscarPorTorneo(idTorneo);
    List<Equipo> equipos = equiposInscriptos(inscriptos);
    if (equipos.size() < EQUIPOS_MINIMOS) {
      throw new FixtureInvalidoException(
        "El torneo " +
        idTorneo +
        " necesita al menos 2 equipos inscriptos para generar el fixture, hay " +
        equipos.size()
      );
    }
    Torneo torneo = inscriptos.get(0).getTorneo();
    return persistirFixture(circulosDeIda(equipos), torneo);
  }

  @Override
  public boolean hayFixture(Long idTorneo) {
    return !repositorioFixture.buscarFechasPorTorneo(idTorneo).isEmpty();
  }

  @Override
  public List<Fecha> obtenerFechas(Long idTorneo) {
    return repositorioFixture.buscarFechasPorTorneo(idTorneo);
  }

  private List<Equipo> equiposInscriptos(List<EquipoTorneo> inscriptos) {
    List<Equipo> equipos = new ArrayList<>();
    for (EquipoTorneo fila : inscriptos) {
      // el null que agrega circulosDeIda es el DESCANSO, no debe confundirse con uno real
      if (fila.getEquipo() != null) {
        equipos.add(fila.getEquipo());
      }
    }
    return equipos;
  }

  private List<List<Equipo>> circulosDeIda(List<Equipo> equipos) {
    List<Equipo> circulo = new ArrayList<>(equipos);
    if (circulo.size() % 2 != 0) {
      // con cantidad impar de equipos la posición vacía hace de equipo libre
      circulo.add(null);
    }
    List<List<Equipo>> circulos = new ArrayList<>();
    List<Equipo> rotacion = circulo;
    circulos.add(rotacion);
    for (int vueltas = 1; vueltas < circulo.size() - 1; vueltas++) {
      rotacion = rotar(rotacion);
      circulos.add(rotacion);
    }
    return circulos;
  }

  private List<Equipo> rotar(List<Equipo> circulo) {
    List<Equipo> rotado = new ArrayList<>(circulo.size());
    rotado.add(circulo.get(0));
    rotado.add(circulo.get(circulo.size() - 1));
    for (int posicion = 1; posicion < circulo.size() - 1; posicion++) {
      rotado.add(circulo.get(posicion));
    }
    return rotado;
  }

  private List<Partido> encuentros(List<Equipo> circulo, boolean invertido) {
    List<Partido> partidos = new ArrayList<>();
    int posiciones = circulo.size();
    for (int posicion = 0; posicion < posiciones / 2; posicion++) {
      Equipo primero = circulo.get(posicion);
      Equipo segundo = circulo.get(posiciones - 1 - posicion);
      if (primero == null || segundo == null) {
        continue;
      }
      partidos.add(crearPartido(primero, segundo, invertido));
    }
    return partidos;
  }

  private Partido crearPartido(Equipo primero, Equipo segundo, boolean invertido) {
    Equipo local = invertido ? segundo : primero;
    Equipo visitante = invertido ? primero : segundo;
    Partido partido = new Partido();
    partido.setEquipoLocal(local);
    partido.setEquipoVisitante(visitante);
    partido.setCodigoLocal(local.getCodigo());
    partido.setCodigoVisitante(visitante.getCodigo());
    return partido;
  }

  private List<Fecha> persistirFixture(List<List<Equipo>> circulos, Torneo torneo) {
    List<Fecha> fechas = new ArrayList<>();
    boolean idaYVuelta = torneo != null && Boolean.TRUE.equals(torneo.getIdaYVuelta());
    agregarVuelta(fechas, circulos, torneo, false);
    if (idaYVuelta) {
      agregarVuelta(fechas, circulos, torneo, true);
    }
    return fechas;
  }

  private void agregarVuelta(
    List<Fecha> fechas,
    List<List<Equipo>> circulos,
    Torneo torneo,
    boolean invertido
  ) {
    for (List<Equipo> circulo : circulos) {
      fechas.add(persistirFecha(fechas.size() + 1, torneo, encuentros(circulo, invertido)));
    }
  }

  private Fecha persistirFecha(int numero, Torneo torneo, List<Partido> partidos) {
    Fecha fecha = new Fecha();
    fecha.setNumero(numero);
    fecha.setTorneo(torneo);
    repositorioFixture.guardarFecha(fecha);
    for (Partido partido : partidos) {
      partido.setFechaId(fecha.getId());
      repositorioFixture.guardarPartido(partido);
    }
    fecha.setPartidos(partidos);
    return fecha;
  }
}
