package com.tallerwebi.dominio;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

import com.tallerwebi.dominio.excepcion.EquipoExistente;
import com.tallerwebi.dominio.excepcion.EquipoYaInscripto;
import com.tallerwebi.dominio.excepcion.SiglasExistentes;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ServicioEquipoTorneoTest {

  private ServicioEquipoTorneo servicioEquipoTorneo;

  private RepositorioEquipoTorneo repositorioEquipoTorneoMock;

  private ServicioEquipo servicioEquipoMock;
  private RepositorioJornada JornadaTorneoMock;

  @BeforeEach
  public void init() {
    this.repositorioEquipoTorneoMock = mock(RepositorioEquipoTorneo.class);
    this.servicioEquipoMock = mock(ServicioEquipo.class);
    this.JornadaTorneoMock = mock(RepositorioJornada.class);
    this.servicioEquipoTorneo =
      new ServicioEquipoTorneoImpl(
        this.repositorioEquipoTorneoMock,
        this.servicioEquipoMock,
        this.JornadaTorneoMock
      );
  }

  @Test
  public void deberiaCrearEquipoEInscribirloEnTorneo() throws EquipoExistente, SiglasExistentes {
    // preparacion
    Equipo equipo = new Equipo();
    equipo.setNombre("Los Pumas");

    Torneo torneo = new Torneo();

    when(this.servicioEquipoMock.consultarEquipo("Los Pumas")).thenReturn(null);

    // ejecucion
    this.servicioEquipoTorneo.crearEquipoEInscribirlo(equipo, torneo);

    // verificacion
    verify(this.servicioEquipoMock).consultarEquipo("Los Pumas");

    verify(this.servicioEquipoMock).registrarEquipo(equipo);

    verify(this.repositorioEquipoTorneoMock).guardar(any(EquipoTorneo.class));
  }

  @Test
  public void deberiaLanzarExcepcionSiElEquipoYaExiste() throws EquipoExistente, SiglasExistentes {
    // preparacion
    Equipo equipo = new Equipo();
    equipo.setNombre("Los Pumas");

    Torneo torneo = new Torneo();

    when(this.servicioEquipoMock.consultarEquipo("Los Pumas")).thenReturn(equipo);

    // ejecucion y verificacion
    assertThrows(
      EquipoExistente.class,
      () -> this.servicioEquipoTorneo.crearEquipoEInscribirlo(equipo, torneo)
    );

    verify(this.servicioEquipoMock, times(0)).registrarEquipo(equipo);

    verify(this.repositorioEquipoTorneoMock, times(0)).guardar(any(EquipoTorneo.class));
  }

  @Test
  public void deberiaContarEquiposPorTorneo() {
    // preparacion
    Long idTorneo = 5L;

    when(this.repositorioEquipoTorneoMock.contarEquiposPorTorneo(idTorneo)).thenReturn(8);

    // ejecucion
    int cantidad = this.servicioEquipoTorneo.contarEquiposPorTorneo(idTorneo);

    // verificacion
    assertThat(cantidad, is(8));

    verify(this.repositorioEquipoTorneoMock, times(1)).contarEquiposPorTorneo(idTorneo);
  }

  @Test
  public void deberiaInscribirEquipoExistente() throws EquipoYaInscripto {
    // preparacion
    Equipo equipo = new Equipo();
    equipo.setId(5L);

    Torneo torneo = new Torneo();
    torneo.setId(10L);

    when(this.repositorioEquipoTorneoMock.existeEquipoEnTorneo(5L, 10L)).thenReturn(false);

    // ejecucion
    this.servicioEquipoTorneo.inscribirEquipoExistente(equipo, torneo);

    // verificacion
    verify(this.repositorioEquipoTorneoMock, times(1)).existeEquipoEnTorneo(5L, 10L);

    verify(this.repositorioEquipoTorneoMock, times(1)).guardar(any(EquipoTorneo.class));
  }

  @Test
  public void deberiaLanzarExcepcionSiElEquipoYaEstaInscripto() throws EquipoYaInscripto {
    //preparacion
    Equipo equipo = new Equipo();
    equipo.setId(5L);

    Torneo torneo = new Torneo();
    torneo.setId(10L);

    when(this.repositorioEquipoTorneoMock.existeEquipoEnTorneo(5L, 10L)).thenReturn(true);

    //ejecucion y verificacion
    assertThrows(
      EquipoYaInscripto.class,
      () -> this.servicioEquipoTorneo.inscribirEquipoExistente(equipo, torneo)
    );

    verify(this.repositorioEquipoTorneoMock, times(1)).existeEquipoEnTorneo(5L, 10L);

    verify(this.repositorioEquipoTorneoMock, times(0)).guardar(any(EquipoTorneo.class));
  }

  @Test
  public void obtenerEstadisticasDeberiaDevolverLaTablaDePosiciones() {
    // preparacion
    Long idTorneo = 1L;

    List<EquipoTorneo> estadisticasEsperadas = new ArrayList<>();

    EquipoTorneo et1 = new EquipoTorneo();
    EquipoTorneo et2 = new EquipoTorneo();

    estadisticasEsperadas.add(et1);
    estadisticasEsperadas.add(et2);

    when(repositorioEquipoTorneoMock.obtenerTablaPosiciones(idTorneo))
      .thenReturn(estadisticasEsperadas);

    // ejecucion
    List<EquipoTorneo> resultado = servicioEquipoTorneo.obtenerEstadisticas(idTorneo);

    // verificacion
    assertThat(resultado, is(estadisticasEsperadas));

    verify(repositorioEquipoTorneoMock).obtenerTablaPosiciones(idTorneo);
  }

  @Test
  public void obtenerEstadisticasDevuelveLaTablaDePosiciones() {
    // preparacion
    Long idTorneo = 1L;

    List<EquipoTorneo> estadisticasEsperadas = new ArrayList<>();

    EquipoTorneo et1 = new EquipoTorneo();
    EquipoTorneo et2 = new EquipoTorneo();

    estadisticasEsperadas.add(et1);
    estadisticasEsperadas.add(et2);

    when(repositorioEquipoTorneoMock.obtenerTablaPosiciones(idTorneo))
      .thenReturn(estadisticasEsperadas);

    // ejecucion
    List<EquipoTorneo> resultado = servicioEquipoTorneo.obtenerEstadisticas(idTorneo);

    // verificacion
    assertThat(resultado, is(estadisticasEsperadas));

    verify(repositorioEquipoTorneoMock).obtenerTablaPosiciones(idTorneo);
  }

  @Test
  public void deberiaDesvincularEquipoCuandoExiste() {
    // preparacion
    Long idEquipo = 3L;
    Long idTorneo = 8L;

    EquipoTorneo equipoTorneo = new EquipoTorneo();

    when(repositorioEquipoTorneoMock.buscarEquipoTorneoPorId(idEquipo, idTorneo))
      .thenReturn(equipoTorneo);

    // ejecucion
    servicioEquipoTorneo.desvincularEquipo(idEquipo, idTorneo);

    // validacion
    verify(repositorioEquipoTorneoMock, times(1)).eliminar(equipoTorneo);
  }

  @Test
  public void noDeberiaEliminarEquipoTorneoCuandoNoExiste() {
    // preparacion
    Long idEquipo = 3L;
    Long idTorneo = 8L;

    when(repositorioEquipoTorneoMock.buscarEquipoTorneoPorId(idEquipo, idTorneo)).thenReturn(null);

    // ejecucion
    servicioEquipoTorneo.desvincularEquipo(idEquipo, idTorneo);

    // validacion
    verify(repositorioEquipoTorneoMock, times(0)).eliminar(any(EquipoTorneo.class));
  }

  @Test
  public void deberiaConsultarEquipoTorneoPorId() {
    // preparacion
    Long idEquipo = 3L;
    Long idTorneo = 8L;

    EquipoTorneo equipoTorneo = new EquipoTorneo();

    when(repositorioEquipoTorneoMock.buscarEquipoTorneoPorId(idEquipo, idTorneo))
      .thenReturn(equipoTorneo);

    // ejecucion
    EquipoTorneo equipoTorneoObtenido = servicioEquipoTorneo.consultarEquipoTorneoPorId(
      idEquipo,
      idTorneo
    );

    // validacion
    assertThat(equipoTorneoObtenido, is(equalTo(equipoTorneo)));

    verify(repositorioEquipoTorneoMock, times(1)).buscarEquipoTorneoPorId(idEquipo, idTorneo);
  }

  @Test
  public void deberiaLanzarSiglasExistentesAlCrearEquipoEInscribirlo()
    throws EquipoExistente, SiglasExistentes {
    // Preparación
    Equipo equipo = new Equipo();
    equipo.setNombre("Boca Juniors");
    equipo.setSiglas("BOC");

    Torneo torneo = new Torneo();

    when(servicioEquipoMock.consultarEquipo("Boca Juniors")).thenReturn(null);

    doThrow(new SiglasExistentes()).when(servicioEquipoMock).registrarEquipo(equipo);

    // Ejecución y validación
    assertThrows(
      SiglasExistentes.class,
      () -> {
        servicioEquipoTorneo.crearEquipoEInscribirlo(equipo, torneo);
      }
    );

    verify(servicioEquipoMock, times(1)).consultarEquipo("Boca Juniors");
    verify(servicioEquipoMock, times(1)).registrarEquipo(equipo);
    verify(repositorioEquipoTorneoMock, never()).guardar(any(EquipoTorneo.class));
  }
}
