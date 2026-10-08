package com.tallerwebi.dominio;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

import com.tallerwebi.dominio.excepcion.TorneoExistente;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ServicioTorneoTest {

  private ServicioTorneo servicioTorneo;
  private RepositorioTorneo repositorioTorneoMock;

  @BeforeEach
  public void init() {
    this.repositorioTorneoMock = mock(RepositorioTorneo.class);
    this.servicioTorneo = new ServicioTorneoImpl(this.repositorioTorneoMock);
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
}
