package com.tallerwebi.dominio;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ServicioSimulacionTorneoTest {

  private ServicioSimulacionTorneo servicioSimulacion;
  private ServicioTorneo servicioTorneoMock;
  private RepositorioJornada repositorioJornadaMock;
  private RepositorioJugadorEquipoTorneo repositorioJugadorEquipoTorneoMock;
  private RepositorioEstadisticaJugador repositorioEstadisticaJugadorMock;
  private RepositorioEventoPartido repositorioEventoPartidoMock;

  @BeforeEach
  public void init() {
    servicioTorneoMock = mock(ServicioTorneo.class);
    repositorioJornadaMock = mock(RepositorioJornada.class);
    repositorioJugadorEquipoTorneoMock = mock(RepositorioJugadorEquipoTorneo.class);
    repositorioEstadisticaJugadorMock = mock(RepositorioEstadisticaJugador.class);
    repositorioEventoPartidoMock = mock(RepositorioEventoPartido.class);

    servicioSimulacion =
      new ServicioSimulacionTorneoImpl(
        servicioTorneoMock,
        repositorioJornadaMock,
        repositorioJugadorEquipoTorneoMock,
        repositorioEstadisticaJugadorMock,
        repositorioEventoPartidoMock
      );
  }

  // Métodos auxiliares para preparar los datos de las pruebas.

  private Jugador crearJugador(Long id, String puesto) {
    Jugador jugador = new Jugador();
    jugador.setId(id);
    jugador.setNombre("Jugador" + id);
    jugador.setPuesto(puesto);
    jugador.setHabilitado(true);
    jugador.setFechasSuspensionPendientes(0);
    return jugador;
  }

  private JugadorEquipoTorneo crearInscripcion(Jugador jugador) {
    JugadorEquipoTorneo inscripcion = new JugadorEquipoTorneo();
    inscripcion.setJugador(jugador);
    inscripcion.setFechasSuspensionPendientes(0);
    return inscripcion;
  }

  private EstadisticaJugador crearEstadistica(Jugador jugador) {
    EstadisticaJugador estadistica = new EstadisticaJugador();
    estadistica.setJugador(jugador);
    estadistica.setPartidosJugados(0);
    estadistica.setGoles(0);
    estadistica.setAsistencias(0);
    estadistica.setParticipacionesGol(0);
    estadistica.setAmarillas(0);
    estadistica.setRojas(0);
    return estadistica;
  }

  private Partido crearPartido(Equipo local, Equipo visitante) {
    Partido partido = new Partido();
    partido.setEquipoLocal(local);
    partido.setEquipoVisitante(visitante);
    partido.setDisputado(false);
    return partido;
  }

  private Jornada crearJornadaConPartido(Partido partido) {
    Jornada jornada = new Jornada();
    jornada.setEstado("Pendiente");
    jornada.getPartidos().add(partido);
    partido.setJornada(jornada);
    return jornada;
  }

  // Tests del método avanzarJornada.

  @Test
  public void avanzarJornadaDeberiaSimularElPartidoYFinalizarLaJornada() {
    // Preparación
    Long idTorneo = 1L;

    Equipo equipoLocal = new Equipo();
    equipoLocal.setId(10L);

    Equipo equipoVisitante = new Equipo();
    equipoVisitante.setId(20L);

    Partido partido = crearPartido(equipoLocal, equipoVisitante);
    Jornada jornada = crearJornadaConPartido(partido);

    Jugador jugadorLocal = crearJugador(100L, "DELANTERO");
    Jugador jugadorVisitante = crearJugador(200L, "DEFENSOR");

    JugadorEquipoTorneo inscripcionLocal = crearInscripcion(jugadorLocal);
    JugadorEquipoTorneo inscripcionVisitante = crearInscripcion(jugadorVisitante);

    EstadisticaJugador estadisticaLocal = crearEstadistica(jugadorLocal);
    EstadisticaJugador estadisticaVisitante = crearEstadistica(jugadorVisitante);

    when(servicioTorneoMock.obtenerJornadasPorTorneo(idTorneo)).thenReturn(Arrays.asList(jornada));

    when(repositorioJugadorEquipoTorneoMock.listarPorEquipoYTorneo(10L, idTorneo))
      .thenReturn(Arrays.asList(inscripcionLocal));

    when(repositorioJugadorEquipoTorneoMock.listarPorEquipoYTorneo(20L, idTorneo))
      .thenReturn(Arrays.asList(inscripcionVisitante));

    when(repositorioEstadisticaJugadorMock.buscarEstadisticasPorIdJugador(100L))
      .thenReturn(estadisticaLocal);

    when(repositorioEstadisticaJugadorMock.buscarEstadisticasPorIdJugador(200L))
      .thenReturn(estadisticaVisitante);

    // Ejecución
    Jornada jornadaObtenida = servicioSimulacion.avanzarJornada(idTorneo);

    // Validación
    assertNotNull(jornadaObtenida);
    assertThat(jornadaObtenida, equalTo(jornada));
    assertThat(jornada.getEstado(), equalTo("Finalizada"));

    verify(repositorioJornadaMock, times(1)).actualizar(jornada);
  }

  @Test
  public void avanzarJornadaSiTodasEstanFinalizadasDeberiaRetornarNull() {
    // Preparación
    Long idTorneo = 1L;

    Jornada jornada = new Jornada();
    jornada.setEstado("Finalizada");

    when(servicioTorneoMock.obtenerJornadasPorTorneo(idTorneo)).thenReturn(Arrays.asList(jornada));

    // Ejecución
    Jornada jornadaObtenida = servicioSimulacion.avanzarJornada(idTorneo);

    // Validación
    assertNull(jornadaObtenida);

    verify(repositorioJornadaMock, never()).actualizar(any(Jornada.class));
    verifyNoInteractions(repositorioJugadorEquipoTorneoMock);
  }

  @Test
  public void avanzarJornadaNoDeberiaVolverASimularUnPartidoDisputado() {
    // Preparación
    Long idTorneo = 1L;

    Partido partido = new Partido();
    partido.setDisputado(true);
    partido.setGolesLocal(2);
    partido.setGolesVisitante(1);

    Jornada jornada = crearJornadaConPartido(partido);

    when(servicioTorneoMock.obtenerJornadasPorTorneo(idTorneo)).thenReturn(Arrays.asList(jornada));

    // Ejecución
    Jornada jornadaObtenida = servicioSimulacion.avanzarJornada(idTorneo);

    // Validación
    assertNotNull(jornadaObtenida);
    assertThat(jornada.getEstado(), equalTo("Finalizada"));

    assertThat(partido.getGolesLocal(), equalTo(2));
    assertThat(partido.getGolesVisitante(), equalTo(1));

    verifyNoInteractions(repositorioJugadorEquipoTorneoMock);
    verify(repositorioJornadaMock).actualizar(jornada);
  }

  @Test
  public void avanzarJornadaDeberiaElegirLaPrimeraJornadaPendiente() {
    // Preparación
    Long idTorneo = 1L;

    Jornada jornadaFinalizada = new Jornada();
    jornadaFinalizada.setEstado("Finalizada");

    Jornada jornadaPendiente = new Jornada();
    jornadaPendiente.setEstado("Pendiente");

    when(servicioTorneoMock.obtenerJornadasPorTorneo(idTorneo))
      .thenReturn(Arrays.asList(jornadaFinalizada, jornadaPendiente));

    // Ejecución
    Jornada jornadaObtenida = servicioSimulacion.avanzarJornada(idTorneo);

    // Validación
    assertThat(jornadaObtenida, equalTo(jornadaPendiente));
    assertThat(jornadaPendiente.getEstado(), equalTo("Finalizada"));

    verify(repositorioJornadaMock).actualizar(jornadaPendiente);
  }

  @Test
  public void avanzarJornadaDeberiaLanzarExcepcionSiUnEquipoNoTieneJugadoresHabilitados() {
    // Preparación
    Long idTorneo = 1L;

    Equipo equipoLocal = new Equipo();
    equipoLocal.setId(10L);

    Equipo equipoVisitante = new Equipo();
    equipoVisitante.setId(20L);

    Jugador jugadorDeshabilitado = crearJugador(100L, "DELANTERO");
    jugadorDeshabilitado.setHabilitado(false);

    Partido partido = crearPartido(equipoLocal, equipoVisitante);
    Jornada jornada = crearJornadaConPartido(partido);

    when(servicioTorneoMock.obtenerJornadasPorTorneo(idTorneo)).thenReturn(Arrays.asList(jornada));

    when(repositorioJugadorEquipoTorneoMock.listarPorEquipoYTorneo(10L, idTorneo))
      .thenReturn(Arrays.asList(crearInscripcion(jugadorDeshabilitado)));

    // Ejecución y validación
    assertThrows(IllegalStateException.class, () -> servicioSimulacion.avanzarJornada(idTorneo));

    verify(repositorioJornadaMock, never()).actualizar(any(Jornada.class));
  }

  @Test
  public void avanzarJornadaDeberiaLanzarExcepcionSiLosJugadoresEstanSuspendidos() {
    // Preparación
    Long idTorneo = 1L;

    Equipo equipoLocal = new Equipo();
    equipoLocal.setId(10L);

    Equipo equipoVisitante = new Equipo();
    equipoVisitante.setId(20L);

    Jugador jugador = crearJugador(100L, "DELANTERO");

    JugadorEquipoTorneo inscripcion = crearInscripcion(jugador);
    inscripcion.setFechasSuspensionPendientes(2);

    Partido partido = crearPartido(equipoLocal, equipoVisitante);
    Jornada jornada = crearJornadaConPartido(partido);

    when(servicioTorneoMock.obtenerJornadasPorTorneo(idTorneo)).thenReturn(Arrays.asList(jornada));

    when(repositorioJugadorEquipoTorneoMock.listarPorEquipoYTorneo(10L, idTorneo))
      .thenReturn(Arrays.asList(inscripcion));

    // Ejecución y validación
    assertThrows(IllegalStateException.class, () -> servicioSimulacion.avanzarJornada(idTorneo));

    verify(repositorioJornadaMock, never()).actualizar(any(Jornada.class));
  }
}
