package com.tallerwebi.dominio;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ServicioJugadorTest {

  private ServicioJugador servicioJugador;
  private RepositorioJugador repositorioJugadorMock;
  private RepositorioEquipo repositorioEquipoMock;

  @BeforeEach
  public void init() {
    this.repositorioJugadorMock = mock(RepositorioJugador.class);
    this.repositorioEquipoMock = mock(RepositorioEquipo.class);
    this.servicioJugador =
      new ServicioJugadorImpl(this.repositorioJugadorMock, this.repositorioEquipoMock);
  }

  @Test
  public void deberiaGuardarElJugadorYAsociarloAlEquipo() {
    // preparacion
    Equipo equipo = new Equipo();
    equipo.setJugadores(new ArrayList<>());
    when(this.repositorioEquipoMock.buscarPorId(1L)).thenReturn(equipo);

    Jugador jugador = new Jugador();
    jugador.setNombre("Lionel");

    // ejecucion
    this.servicioJugador.agregarJugador(1L, jugador);

    // verificacion
    verify(this.repositorioJugadorMock).guardar(jugador);
    assertThat(equipo.getJugadores(), hasItem(jugador));
    verify(this.repositorioEquipoMock).guardar(equipo);
  }

  @Test
  public void deberiaInicializarLaListaDeJugadoresCuandoEsNula() {
    // preparacion
    Equipo equipo = new Equipo();
    when(this.repositorioEquipoMock.buscarPorId(2L)).thenReturn(equipo);

    // ejecucion
    this.servicioJugador.agregarJugador(2L, new Jugador());

    // verificacion
    assertThat(equipo.getJugadores(), hasSize(1));
  }

  @Test
  public void noDeberiaFallarCuandoElEquipoNoExiste() {
    // preparacion
    when(this.repositorioEquipoMock.buscarPorId(99L)).thenReturn(null);

    // ejecucion
    this.servicioJugador.agregarJugador(99L, new Jugador());

    // verificacion
    verify(this.repositorioJugadorMock).guardar(any(Jugador.class));
    verify(this.repositorioEquipoMock, never()).guardar(any(Equipo.class));
  }

  @Test
  public void deberiaDelegarElListadoDeJugadoresEnElRepositorio() {
    // preparacion
    List<Jugador> jugadores = new ArrayList<>();
    when(this.repositorioJugadorMock.listarPorEquipo(5L)).thenReturn(jugadores);

    // ejecucion
    List<Jugador> resultado = this.servicioJugador.obtenerJugadores(5L);

    // verificacion
    assertThat(resultado, is(jugadores));
    verify(this.repositorioJugadorMock).listarPorEquipo(5L);
  }

  @Test
  public void deberiaEliminarElJugadorYQuitarloDelEquipo() {
    // preparacion
    Equipo equipo = new Equipo();
    Jugador jugador = new Jugador();
    jugador.setId(7L);
    equipo.setJugadores(new ArrayList<>(List.of(jugador)));
    when(this.repositorioEquipoMock.buscarPorId(1L)).thenReturn(equipo);

    // ejecucion
    this.servicioJugador.eliminarJugador(1L, 7L);

    // verificacion
    assertThat(equipo.getJugadores(), hasSize(0));
    verify(this.repositorioEquipoMock).guardar(equipo);
    verify(this.repositorioJugadorMock).eliminar(7L);
  }

  @Test
  public void deberiaHabilitarElJugadorYActualizarlo() {
    // preparacion
    Jugador jugador = new Jugador();
    jugador.setId(7L);
    when(this.repositorioJugadorMock.obtenerJugador(7L)).thenReturn(jugador);

    // ejecucion
    this.servicioJugador.habilitarJugador(7L, true);

    // verificacion
    assertThat(jugador.getHabilitado(), is(true));
    verify(this.repositorioJugadorMock).actualizar(jugador);
  }

  @Test
  public void noDeberiaHabilitarSiElJugadorNoExiste() {
    // preparacion
    when(this.repositorioJugadorMock.obtenerJugador(99L)).thenReturn(null);

    // ejecucion
    this.servicioJugador.habilitarJugador(99L, true);

    // verificacion
    verify(this.repositorioJugadorMock, never()).actualizar(any(Jugador.class));
  }

  @Test
  public void deberiaConsultarJugadorPorId() {
    //preparacion
    Jugador jugador = new Jugador();
    jugador.setId(10L);

    when(repositorioJugadorMock.obtenerJugador(10L)).thenReturn(jugador);

    //ejecucion
    Jugador resultado = servicioJugador.consultarJugador(10L);

    //verificacion
    assertThat(resultado, is(jugador));
    verify(repositorioJugadorMock).obtenerJugador(10L);
  }

  @Test
  public void deberiaRetornarNullSiElJugadorConsultadoNoExiste() {
    when(repositorioJugadorMock.obtenerJugador(99L)).thenReturn(null);

    Jugador resultado = servicioJugador.consultarJugador(99L);

    assertThat(resultado, is((Jugador) null));
    verify(repositorioJugadorMock).obtenerJugador(99L);
  }

  @Test
  public void deberiaSancionarJugadorYActualizarlo() {
    //preparacion
    Jugador jugador = new Jugador();
    jugador.setId(10L);

    when(repositorioJugadorMock.obtenerJugador(10L)).thenReturn(jugador);

    //ejecucion
    servicioJugador.sancionarJugador(10L, 3);

    //verificacion
    assertThat(jugador.getFechasSuspensionPendientes(), is(3));
    assertThat(jugador.getEstadoSancion(), is("SANCIONADO"));
    verify(repositorioJugadorMock).actualizar(jugador);
  }

  @Test
  public void deberiaDescontarUnaFechaDeSuspension() {
    //preparacion
    Jugador jugador = new Jugador();
    jugador.setFechasSuspensionPendientes(3);
    jugador.setEstadoSancion("SANCIONADO");

    when(repositorioJugadorMock.obtenerJugador(10L)).thenReturn(jugador);

    //ejecucion
    servicioJugador.cumplirFechaSuspension(10L);

    //verificacion
    assertThat(jugador.getFechasSuspensionPendientes(), is(2));
    assertThat(jugador.getEstadoSancion(), is("SANCIONADO"));
    verify(repositorioJugadorMock).actualizar(jugador);
  }

  @Test
  public void deberiaQuitarLaSancionAlCumplirLaUltimaFecha() {
    //preparacion
    Jugador jugador = new Jugador();
    jugador.setFechasSuspensionPendientes(1);
    jugador.setEstadoSancion("SANCIONADO");

    when(repositorioJugadorMock.obtenerJugador(10L)).thenReturn(jugador);

    //ejecucion
    servicioJugador.cumplirFechaSuspension(10L);

    //verificacion
    assertThat(jugador.getFechasSuspensionPendientes(), is(0));
    assertThat(jugador.getEstadoSancion(), is("SIN_SANCION"));
    verify(repositorioJugadorMock).actualizar(jugador);
  }

  @Test
  public void deberiaAgregarFechasALaSuspensionExistente() {
    Jugador jugador = new Jugador();
    jugador.setFechasSuspensionPendientes(2);

    when(repositorioJugadorMock.obtenerJugador(10L)).thenReturn(jugador);

    servicioJugador.agregarFechasSuspension(10L, 3);

    assertThat(jugador.getFechasSuspensionPendientes(), is(5));
    assertThat(jugador.getEstadoSancion(), is("SANCIONADO"));
    verify(repositorioJugadorMock).actualizar(jugador);
  }

  @Test
  public void deberiaAgregarFechasSiLasPendientesSonNulas() {
    Jugador jugador = new Jugador();
    jugador.setFechasSuspensionPendientes(null);

    when(repositorioJugadorMock.obtenerJugador(10L)).thenReturn(jugador);

    servicioJugador.agregarFechasSuspension(10L, 2);

    assertThat(jugador.getFechasSuspensionPendientes(), is(2));
    assertThat(jugador.getEstadoSancion(), is("SANCIONADO"));
    verify(repositorioJugadorMock).actualizar(jugador);
  }

  @Test
  public void deberiaSumarFechasALaSuspensionExistente() {
    // Preparación
    Jugador jugador = new Jugador();
    jugador.setFechasSuspensionPendientes(2);

    when(repositorioJugadorMock.obtenerJugador(10L)).thenReturn(jugador);

    // Ejecución
    servicioJugador.sancionarJugador(10L, 3);

    // Verificación
    assertThat(jugador.getFechasSuspensionPendientes(), is(5));
    assertThat(jugador.getEstadoSancion(), is("SANCIONADO"));
    verify(repositorioJugadorMock).actualizar(jugador);
  }
}
