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
}
