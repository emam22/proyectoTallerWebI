package com.tallerwebi.dominio;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.times;

import com.tallerwebi.dominio.excepcion.TorneoExistente;
import com.tallerwebi.dominio.excepcion.TorneoInvalidoException;
import com.tallerwebi.dominio.excepcion.UsuarioExistente;
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
  public void registrarTorneoInvalidoDeberiaLanzarErrorSinGuardar() {
    // preparacion
    Torneo torneo = torneoValido("TorneoInvalido");
    torneo.setCantidadDeEquipos(1);

    // ejecucion y validacion
    assertThrows(TorneoInvalidoException.class, () -> this.servicioTorneo.registrarTorneo(torneo));
    verify(this.repositorioTorneoMock, times(0)).guardar(torneo);
  }

  @Test
  public void consultarTorneoPorIdDeberiaLlamarAlRepositorio() {
    //preparacion
    Long id = 5L;
    Torneo torneoEsperado = new Torneo();
    torneoEsperado.setId(5L);

    when(this.repositorioTorneoMock.buscarTorneoPorId(id)).thenReturn(torneoEsperado);

    //ejecucion
    Torneo torneoObtenido = this.servicioTorneo.consultarTorneoPorId(id);

    //validacion
    assertThat(torneoObtenido, equalTo(torneoEsperado));

    verify(this.repositorioTorneoMock, times(1)).buscarTorneoPorId(id);
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
}
