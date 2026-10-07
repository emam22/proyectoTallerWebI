package com.tallerwebi.dominio;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.tallerwebi.dominio.excepcion.FixtureInvalidoException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ServicioFixtureTest {

  private RepositorioEquipoTorneo repositorioEquipoTorneoMock;
  private RepositorioFixture repositorioFixtureMock;
  private ServicioFixture servicio;
  private long proximoIdFecha;

  @BeforeEach
  public void init() {
    repositorioEquipoTorneoMock = mock(RepositorioEquipoTorneo.class);
    repositorioFixtureMock = mock(RepositorioFixture.class);
    proximoIdFecha = 1L;
    doAnswer(invocacion -> {
        Fecha fecha = invocacion.getArgument(0);
        fecha.setId(proximoIdFecha++);
        return null;
      })
      .when(repositorioFixtureMock)
      .guardarFecha(any(Fecha.class));
    servicio = new ServicioFixtureImpl(repositorioEquipoTorneoMock, repositorioFixtureMock);
  }

  @Test
  public void conCuatroEquiposSinIdaYVueltaGeneraTresFechasYSeisPartidos()
    throws FixtureInvalidoException {
    // preparacion
    inscribir(
      1L,
      false,
      List.of(equipo("A", "EQ01"), equipo("B", "EQ02"), equipo("C", "EQ03"), equipo("D", "EQ04"))
    );

    // ejecucion
    List<Fecha> fechas = servicio.generarFixture(1L);

    // validacion
    assertThat(fechas, hasSize(3));
    assertThat(totalDePartidos(fechas), equalTo(6));
    for (int indice = 0; indice < fechas.size(); indice++) {
      assertThat(fechas.get(indice).getNumero(), equalTo(indice + 1));
      assertThat(fechas.get(indice).getPartidos(), hasSize(2));
    }
    for (Fecha fecha : fechas) {
      for (Partido partido : fecha.getPartidos()) {
        assertThat(partido.getFechaId(), equalTo(fecha.getId()));
        assertThat(partido.getDisputado(), is(false));
        assertThat(partido.getCodigoLocal(), equalTo(partido.getEquipoLocal().getCodigo()));
        assertThat(partido.getCodigoVisitante(), equalTo(partido.getEquipoVisitante().getCodigo()));
      }
    }
    verify(repositorioFixtureMock, times(3)).guardarFecha(any(Fecha.class));
    verify(repositorioFixtureMock, times(6)).guardarPartido(any(Partido.class));
  }

  @Test
  public void conCuatroEquiposConIdaYVueltaGeneraSeisFechasYDocePartidos()
    throws FixtureInvalidoException {
    // preparacion
    inscribir(
      1L,
      true,
      List.of(equipo("A", "EQ01"), equipo("B", "EQ02"), equipo("C", "EQ03"), equipo("D", "EQ04"))
    );

    // ejecucion
    List<Fecha> fechas = servicio.generarFixture(1L);

    // validacion
    assertThat(fechas, hasSize(6));
    assertThat(totalDePartidos(fechas), equalTo(12));
    assertThat(encuentrosDe(fechas.get(0)), contains("A-D", "B-C"));
    assertThat(encuentrosDe(fechas.get(3)), contains("D-A", "C-B"));
    assertThat(fechas.get(5).getNumero(), equalTo(6));
  }

  @Test
  public void conCincoEquiposCadaParSeEnfrentaExactamenteUnaVez() throws FixtureInvalidoException {
    // preparacion
    inscribir(
      1L,
      false,
      List.of(
        equipo("A", "EQ01"),
        equipo("B", "EQ02"),
        equipo("C", "EQ03"),
        equipo("D", "EQ04"),
        equipo("E", "EQ05")
      )
    );

    // ejecucion
    List<Fecha> fechas = servicio.generarFixture(1L);

    // validacion
    assertThat(fechas, hasSize(5));
    assertThat(totalDePartidos(fechas), equalTo(10));
    Set<String> parejas = new HashSet<>();
    for (Fecha fecha : fechas) {
      assertThat(fecha.getPartidos(), hasSize(2));
      for (Partido partido : fecha.getPartidos()) {
        parejas.add(claveDePareja(partido));
      }
    }
    assertThat(parejas, equalTo(todosLosPares("A", "B", "C", "D", "E")));
  }

  @Test
  public void conCincoEquiposElFixtureReproduceLaTablaDelDiseno() throws FixtureInvalidoException {
    // preparacion
    List<Equipo> equipos = List.of(
      equipo("A", "EQ01"),
      equipo("B", "EQ02"),
      equipo("C", "EQ03"),
      equipo("D", "EQ04"),
      equipo("E", "EQ05")
    );
    inscribir(1L, false, equipos);

    // ejecucion
    List<Fecha> fechas = servicio.generarFixture(1L);

    // validacion
    assertThat(fechas, hasSize(5));
    assertThat(encuentrosDe(fechas.get(0)), contains("B-E", "C-D"));
    assertThat(encuentrosDe(fechas.get(1)), contains("A-E", "B-C"));
    assertThat(encuentrosDe(fechas.get(2)), contains("A-D", "E-C"));
    assertThat(encuentrosDe(fechas.get(3)), contains("A-C", "D-B"));
    assertThat(encuentrosDe(fechas.get(4)), contains("A-B", "D-E"));
    assertThat(descansantes(fechas, equipos), contains("A", "D", "B", "E", "C"));
  }

  @Test
  public void conUnSoloEquipoLanzaFixtureInvalido() {
    // preparacion
    inscribir(1L, false, List.of(equipo("A", "EQ01")));

    // ejecucion y validacion
    FixtureInvalidoException excepcion = assertThrows(
      FixtureInvalidoException.class,
      () -> servicio.generarFixture(1L)
    );
    assertThat(excepcion.getMessage(), containsString("al menos 2 equipos"));
    verify(repositorioFixtureMock, times(0)).guardarFecha(any(Fecha.class));
  }

  @Test
  public void sinEquiposInscriptosLanzaFixtureInvalido() {
    // preparacion
    inscribir(1L, false, List.of());

    // ejecucion y validacion
    assertThrows(FixtureInvalidoException.class, () -> servicio.generarFixture(1L));
  }

  @Test
  public void hayFixtureEsFalsoCuandoTodaviaNoHayFechas() {
    when(repositorioFixtureMock.buscarFechasPorTorneo(1L)).thenReturn(List.of());
    assertThat(servicio.hayFixture(1L), is(false));
  }

  @Test
  public void hayFixtureEsVerdaderoCuandoYaExistenFechas() {
    when(repositorioFixtureMock.buscarFechasPorTorneo(1L)).thenReturn(List.of(new Fecha()));
    assertThat(servicio.hayFixture(1L), is(true));
  }

  @Test
  public void obtenerFechasDevuelveLasFechasQueTraeElRepositorio() {
    List<Fecha> esperadas = List.of(new Fecha(), new Fecha());
    when(repositorioFixtureMock.buscarFechasPorTorneo(1L)).thenReturn(esperadas);
    assertThat(servicio.obtenerFechas(1L), equalTo(esperadas));
  }

  private Equipo equipo(String nombre, String codigo) {
    Equipo equipo = new Equipo();
    equipo.setNombre(nombre);
    equipo.setCodigo(codigo);
    return equipo;
  }

  private void inscribir(Long idTorneo, boolean idaYVuelta, List<Equipo> equipos) {
    Torneo torneo = new Torneo();
    torneo.setId(idTorneo);
    torneo.setIdaYVuelta(idaYVuelta);
    List<EquipoTorneo> filas = new ArrayList<>();
    for (Equipo equipo : equipos) {
      EquipoTorneo fila = new EquipoTorneo();
      fila.setEquipo(equipo);
      fila.setTorneo(torneo);
      filas.add(fila);
    }
    when(repositorioEquipoTorneoMock.buscarPorTorneo(idTorneo)).thenReturn(filas);
  }

  private int totalDePartidos(List<Fecha> fechas) {
    int total = 0;
    for (Fecha fecha : fechas) {
      total += fecha.getPartidos().size();
    }
    return total;
  }

  private List<String> encuentrosDe(Fecha fecha) {
    List<String> encuentros = new ArrayList<>();
    for (Partido partido : fecha.getPartidos()) {
      encuentros.add(
        partido.getEquipoLocal().getNombre() + "-" + partido.getEquipoVisitante().getNombre()
      );
    }
    return encuentros;
  }

  private List<String> descansantes(List<Fecha> fechas, List<Equipo> equipos) {
    List<String> descansos = new ArrayList<>();
    for (Fecha fecha : fechas) {
      Set<String> jugando = new HashSet<>();
      for (Partido partido : fecha.getPartidos()) {
        jugando.add(partido.getEquipoLocal().getNombre());
        jugando.add(partido.getEquipoVisitante().getNombre());
      }
      for (Equipo equipo : equipos) {
        if (!jugando.contains(equipo.getNombre())) {
          descansos.add(equipo.getNombre());
        }
      }
    }
    return descansos;
  }

  private String claveDePareja(Partido partido) {
    return clave(partido.getEquipoLocal().getNombre(), partido.getEquipoVisitante().getNombre());
  }

  private String clave(String nombreA, String nombreB) {
    return nombreA.compareTo(nombreB) < 0 ? nombreA + "|" + nombreB : nombreB + "|" + nombreA;
  }

  private Set<String> todosLosPares(String... nombres) {
    Set<String> pares = new HashSet<>();
    for (int primero = 0; primero < nombres.length; primero++) {
      for (int segundo = primero + 1; segundo < nombres.length; segundo++) {
        pares.add(clave(nombres[primero], nombres[segundo]));
      }
    }
    return pares;
  }
}
