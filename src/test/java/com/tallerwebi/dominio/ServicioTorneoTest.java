package com.tallerwebi.dominio;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.any;

import com.tallerwebi.dominio.excepcion.TorneoExistente;
import com.tallerwebi.infraestructura.RepositorioEquipoImpl;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ServicioTorneoTest {

  private ServicioTorneo servicioTorneo;
  private RepositorioTorneo repositorioTorneoMock;
  private RepositorioEquipoImpl repositorioEquipoMock;
  private RepositorioJornada repositorioJornadaMock;

  @BeforeEach
  public void init() {
    this.repositorioTorneoMock = mock(RepositorioTorneo.class);
    // this.servicioTorneo = new ServicioTorneoImpl(this.repositorioTorneoMock);
    this.repositorioEquipoMock = mock(RepositorioEquipoImpl.class);
    this.repositorioJornadaMock = mock(RepositorioJornada.class);

    this.servicioTorneo =
      new ServicioTorneoImpl(
        this.repositorioTorneoMock,
        this.repositorioEquipoMock,
        this.repositorioJornadaMock
      );
  }

  @Test
  public void consultarTorneoDeberiaLlamarAlRepositorio() {
    // preparacion
    String nombre = "MegaFutbol";
    Torneo TorneoEsperado = new Torneo();
    when(this.repositorioTorneoMock.buscarTorneo(nombre)).thenReturn(TorneoEsperado);

    // ejecucion
    Torneo TorneoObtenido = this.servicioTorneo.consultarTorneo(nombre);

    // validacion
    assertThat(TorneoObtenido, equalTo(TorneoEsperado));
    verify(this.repositorioTorneoMock, times(1)).buscarTorneo(nombre);
  }

  private Torneo torneoValido(String nombre) {
    Torneo torneo = new Torneo();
    torneo.setNombre(nombre);
    torneo.setCantidadDeEquipos(4);
    torneo.setCantidadMinJugadores(10);
    torneo.setCantidadMaxJugadores(15);
    torneo.setCantidadAmaSusp(3);
    return torneo;
  }

  @Test
  public void registrarTorneoSiNoExisteDeberiaGuardarlo() throws TorneoExistente, Exception {
    // preparacion
    Torneo torneo = torneoValido("nuevoTorneo");
    when(this.repositorioTorneoMock.buscarTorneo(torneo.getNombre())).thenReturn(null);

    // ejecucion
    this.servicioTorneo.registrarTorneo(torneo);

    // validacion
    verify(this.repositorioTorneoMock, times(1)).guardar(torneo);
  }

  @Test
  public void registrarTorneoSiExisteDeberiaLanzarExcepcion() {
    // preparacion
    Torneo torneo = torneoValido("MegaFutbol");
    when(this.repositorioTorneoMock.buscarTorneo(torneo.getNombre())).thenReturn(new Torneo());

    // ejecucion y validacion
    assertThrows(TorneoExistente.class, () -> this.servicioTorneo.registrarTorneo(torneo));
    verify(this.repositorioTorneoMock, times(0)).guardar(torneo);
  }

  @Test
  public void consultarTorneoPorIdSiNoExisteDeberiaRetornarNull() {
    // preparacion
    Long id = 99L;

    when(this.repositorioTorneoMock.buscarTorneoPorId(id)).thenReturn(null);

    // ejecucion
    Torneo torneoObtenido = this.servicioTorneo.consultarTorneoPorId(id);

    // validacion
    assertThat(torneoObtenido, equalTo(null));

    verify(this.repositorioTorneoMock, times(1)).buscarTorneoPorId(id);
  }

  @Test
  public void obtenerTorneosDeberiaRetornarLosTorneos() {
    // preparacion
    Torneo torneo1 = new Torneo();
    torneo1.setNombre("Campito");

    Torneo torneo2 = new Torneo();
    torneo2.setNombre("MegaFutbol");

    List<Torneo> torneosEsperados = Arrays.asList(torneo1, torneo2);

    when(repositorioTorneoMock.listar()).thenReturn(torneosEsperados);

    // ejecucion
    List<Torneo> torneosObtenidos = servicioTorneo.obtenerTorneos();

    // verificacion
    assertThat(torneosObtenidos, is(torneosObtenidos));
    verify(repositorioTorneoMock, times(1)).listar();
  }

  @Test
  public void obtenerTorneosSiNoHayTorneosDeberiaRetornarListaVacia() {
    // preparacion
    when(repositorioTorneoMock.listar()).thenReturn(Arrays.asList());

    // ejecucion
    List<Torneo> torneosObtenidos = servicioTorneo.obtenerTorneos();

    // verificacion
    assertThat(torneosObtenidos, is(empty()));

    verify(repositorioTorneoMock, times(1)).listar();
  }

  //tests jornada y fixture

  @Test
  public void crearTorneoConDosEquiposDeberiaCrearUnaJornada() throws Exception {
    //given
    Torneo torneo = new Torneo();
    torneo.setCantidadDeEquipos(2);

    givenCreacionEquipo(1L, "Barcelona");
    givenCreacionEquipo(2L, "Real Madrid");

    List<Long> idsEquipos = Arrays.asList(1L, 2L);

    //when
    servicioTorneo.crearTorneoConFixture(torneo, idsEquipos);
    //then

    verify(repositorioJornadaMock, times(1)).guardar(any(Jornada.class));
  }

  private void givenCreacionEquipo(Long id, String nombre) {
    Equipo equipo = new Equipo();
    equipo.setId(id);
    equipo.setNombre(nombre);

    when(repositorioEquipoMock.buscarPorId(id)).thenReturn(equipo);
  }

  @Test
  public void crearTorneoConDosEquiposSeDeberiaCrearJornadaYAsociarlaAlTorneo() throws Exception {
    //given
    Torneo torneo = new Torneo();
    torneo.setCantidadDeEquipos(2);

    givenCreacionEquipo(1L, "Barcelona");
    givenCreacionEquipo(2L, "Real Madrid");

    List<Long> idsEquipos = Arrays.asList(1L, 2L);

    //when
    servicioTorneo.crearTorneoConFixture(torneo, idsEquipos);

    //then
    verify(repositorioJornadaMock).guardar(argThat(jornada -> jornada.getTorneo() == torneo));
  }

  @Test
  public void alCrearTorneoSeDeberiaCrearLaJornadaNumeroUno() throws Exception {
    //given
    Torneo torneo = new Torneo();
    torneo.setCantidadDeEquipos(2);

    givenCreacionEquipo(1L, "Barcelona");
    givenCreacionEquipo(2L, "Real Madrid");

    List<Long> idsEquipos = Arrays.asList(1L, 2L);

    //when
    servicioTorneo.crearTorneoConFixture(torneo, idsEquipos);

    //then
    verify(repositorioJornadaMock)
      .guardar(argThat(jornada -> Integer.valueOf(1).equals(jornada.getNumero())));
  }

  @Test
  public void alCrearTorneoLaJornadaDeberiaEstarEnPendiente() throws Exception {
    Torneo torneo = new Torneo();
    torneo.setCantidadDeEquipos(2);

    givenCreacionEquipo(1L, "Barcelona");
    givenCreacionEquipo(2L, "Real Madrid");

    List<Long> idsEquipos = Arrays.asList(1L, 2L);

    Jornada jornadaEsperada = new Jornada();
    jornadaEsperada.setNumero(1);
    jornadaEsperada.setEstado("Pendiente");
    jornadaEsperada.setTorneo(torneo);

    // when
    servicioTorneo.crearTorneoConFixture(torneo, idsEquipos);

    // then
    assertThat(jornadaEsperada.getEstado(), equalTo("Pendiente"));
  }

  @Test
  public void alCrearTorneoConDosEquiposSeDebeCrearUnPartido() throws Exception {
    Torneo torneo = new Torneo();
    torneo.setCantidadDeEquipos(2);

    givenCreacionEquipo(1L, "Barcelona");
    givenCreacionEquipo(2L, "Real Madrid");

    List<Long> idsEquipos = Arrays.asList(1L, 2L);

    //when
    servicioTorneo.crearTorneoConFixture(torneo, idsEquipos);

    //then
    assertNotNull(torneo.getPartidosEnTorneo(), "la lista de partidos no debe ser null");
    assertEquals(1, torneo.getPartidosEnTorneo().size(), "Tendria que haber 1 partido");
  }

  @Test
  public void alCrearTorneoSeDebeAsignarLosEquiposAlPartido() throws Exception {
    Torneo torneo = new Torneo();
    torneo.setCantidadDeEquipos(2);

    givenCreacionEquipo(1L, "Barcelona");
    givenCreacionEquipo(2L, "Real Madrid");

    List<Long> idsEquipos = Arrays.asList(1L, 2L);

    servicioTorneo.crearTorneoConFixture(torneo, idsEquipos);

    Partido partido = torneo.getPartidosEnTorneo().get(0);

    assertEquals("Barcelona", partido.getEquipoLocal().getNombre());
    assertEquals("Real Madrid", partido.getEquipoVisitante().getNombre());
  }

  @Test
  public void alCrearTorneoSeDebeAgregarElPartidoAlTorneo() throws Exception {
    Torneo torneo = new Torneo();
    torneo.setCantidadDeEquipos(2);

    givenCreacionEquipo(1L, "Barcelona");
    givenCreacionEquipo(2L, "Real Madrid");

    List<Long> idsEquipos = Arrays.asList(1L, 2L);

    servicioTorneo.crearTorneoConFixture(torneo, idsEquipos);

    assertEquals(1, torneo.getPartidosEnTorneo().size());
    verify(repositorioJornadaMock, times(1)).guardar(any(Jornada.class));
  }

  @Test
  public void alCrearTorneoConCuatroEquiposSeDebenCrearSeisPartidos() throws Exception {
    Torneo torneo = new Torneo();
    torneo.setCantidadDeEquipos(4);

    givenCreacionEquipo(1L, "Barcelona");
    givenCreacionEquipo(2L, "Real Madrid");
    givenCreacionEquipo(3L, "Manchester City");
    givenCreacionEquipo(4L, "Manchester United");

    List<Long> idsEquipos = Arrays.asList(1L, 2L, 3L, 4L);
    servicioTorneo.crearTorneoConFixture(torneo, idsEquipos);

    assertEquals(6, torneo.getPartidosEnTorneo().size());
  }

  @Test
  public void alCrearTorneoConCuatrosEquiposSeDebenCrearTresJornadas() throws Exception {
    Torneo torneo = torneoValido("Nuevo Torneo");

    givenCreacionEquipo(1L, "Barcelona");
    givenCreacionEquipo(2L, "Real Madrid");
    givenCreacionEquipo(3L, "Manchester City");
    givenCreacionEquipo(4L, "Manchester United");

    List<Long> idsEquipos = Arrays.asList(1L, 2L, 3L, 4L);
    servicioTorneo.crearTorneoConFixture(torneo, idsEquipos);

    assertThat(torneo.getJornadas(), hasSize(3));
    assertThat(torneo.getPartidosEnTorneo(), hasSize(6));
  }

  @Test
  public void alCrearTorneoConCuatroEquiposCadaJornadaDebeTenerDosPartidos() throws Exception {
    Torneo torneo = torneoValido("Nuevo Torneo");
    torneo.setCantidadDeEquipos(4);
    torneo.setIdaYVuelta(false);

    givenCreacionEquipo(1L, "Barcelona");
    givenCreacionEquipo(2L, "Real Madrid");
    givenCreacionEquipo(3L, "Manchester City");
    givenCreacionEquipo(4L, "Manchester United");

    List<Long> idsEquipos = Arrays.asList(1L, 2L, 3L, 4L);
    servicioTorneo.crearTorneoConFixture(torneo, idsEquipos);

    verify(repositorioJornadaMock, times(3)).guardar(any(Jornada.class));

    assertThat(torneo.getJornadas().get(0).getPartidos(), hasSize(2));
    assertThat(torneo.getJornadas().get(1).getPartidos(), hasSize(2));
    assertThat(torneo.getJornadas().get(2).getPartidos(), hasSize(2));
  }

  @Test
  public void alCrearTorneoConCuatroEquiposNoSeDebenRepetirEnfrentamientosCuandoNoHayIdaYVuelta()
    throws Exception {
    Torneo torneo = torneoValido("Nuevo Torneo");
    torneo.setCantidadDeEquipos(4);
    torneo.setIdaYVuelta(false);

    givenCreacionEquipo(1L, "Barcelona");
    givenCreacionEquipo(2L, "Real Madrid");
    givenCreacionEquipo(3L, "Manchester City");
    givenCreacionEquipo(4L, "Manchester United");

    List<Long> idsEquipos = Arrays.asList(1L, 2L, 3L, 4L);
    servicioTorneo.crearTorneoConFixture(torneo, idsEquipos);

    assertEquals(6, torneo.contarEnfrentamientosUnicos());
  }

  @Test
  public void alCrearTorneoConCuatroEquiposValidarQueSeJuegueIdaYVuelta() throws Exception {
    Torneo torneo = new Torneo();
    torneo.setCantidadDeEquipos(4);
    torneo.setIdaYVuelta(true);

    givenCreacionEquipo(1L, "Barcelona");
    givenCreacionEquipo(2L, "Real Madrid");
    givenCreacionEquipo(3L, "Manchester City");
    givenCreacionEquipo(4L, "Manchester United");

    List<Long> idsEquipos = Arrays.asList(1L, 2L, 3L, 4L);

    servicioTorneo.crearTorneoConFixture(torneo, idsEquipos);

    assertThat(torneo.getJornadas(), hasSize(6));
    assertThat(torneo.getPartidosEnTorneo(), hasSize(12));
  }
}
