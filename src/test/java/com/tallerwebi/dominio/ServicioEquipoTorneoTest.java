package com.tallerwebi.dominio;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

import com.tallerwebi.dominio.excepcion.EquipoExistente;
import com.tallerwebi.dominio.excepcion.EquipoYaInscripto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ServicioEquipoTorneoTest {

  private ServicioEquipoTorneo servicioEquipoTorneo;

  private RepositorioEquipoTorneo repositorioEquipoTorneoMock;

  private ServicioEquipo servicioEquipoMock;

  @BeforeEach
  public void init() {
    this.repositorioEquipoTorneoMock = mock(RepositorioEquipoTorneo.class);
    this.servicioEquipoMock = mock(ServicioEquipo.class);
    this.servicioEquipoTorneo =
      new ServicioEquipoTorneoImpl(this.repositorioEquipoTorneoMock, this.servicioEquipoMock);
  }

  @Test
  public void deberiaCrearEquipoEInscribirloEnTorneo() throws EquipoExistente {
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
  public void deberiaLanzarExcepcionSiElEquipoYaExiste() throws EquipoExistente {
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

    when(this.repositorioEquipoTorneoMock
            .existeEquipoEnTorneo(5L, 10L))
            .thenReturn(false);

    // ejecucion
    this.servicioEquipoTorneo.inscribirEquipoExistente(equipo, torneo);

    // verificacion
    verify(this.repositorioEquipoTorneoMock, times(1))
            .existeEquipoEnTorneo(5L, 10L);

    verify(this.repositorioEquipoTorneoMock, times(1))
            .guardar(any(EquipoTorneo.class));
  }

  @Test
  public void deberiaLanzarExcepcionSiElEquipoYaEstaInscripto()
          throws EquipoYaInscripto {

    //preparacion
    Equipo equipo = new Equipo();
    equipo.setId(5L);

    Torneo torneo = new Torneo();
    torneo.setId(10L);

    when(this.repositorioEquipoTorneoMock
            .existeEquipoEnTorneo(5L, 10L))
            .thenReturn(true);

    //ejecucion y verificacion
    assertThrows(
            EquipoYaInscripto.class,
            () -> this.servicioEquipoTorneo.inscribirEquipoExistente(
                    equipo,
                    torneo
            )
    );

    verify(this.repositorioEquipoTorneoMock, times(1))
            .existeEquipoEnTorneo(5L, 10L);

    verify(this.repositorioEquipoTorneoMock, times(0))
            .guardar(any(EquipoTorneo.class));
  }




}
