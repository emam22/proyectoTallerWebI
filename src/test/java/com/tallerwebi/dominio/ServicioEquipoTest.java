package com.tallerwebi.dominio;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

import com.tallerwebi.dominio.excepcion.EquipoExistente;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
public class ServicioEquipoTest {

  private ServicioEquipo servicioEquipo;

  private RepositorioEquipo repositorioEquipoMock;

  @BeforeEach
  public void init() {
    this.repositorioEquipoMock = mock(RepositorioEquipo.class);
    this.servicioEquipo = new ServicioEquipoImpl(this.repositorioEquipoMock);
  }

  @Test
  public void consultarEquipoDeberiaLlamarAlRepositorio() {
    // preparacion
    String nombre = "Boca";
    Equipo equipoEsperado = new Equipo();
    when(this.repositorioEquipoMock.buscarEquipo(nombre)).thenReturn(equipoEsperado);

    // ejecucion
    Equipo equipoObtenido = this.servicioEquipo.consultarEquipo(nombre);

    // validacion
    assertThat(equipoObtenido, equalTo(equipoEsperado));
    verify(this.repositorioEquipoMock, times(1)).buscarEquipo(nombre);
  }

  @Test
  public void registrarEquipoSiExisteDeberiaLanzarExcepcion() {
    // preparacion
    Equipo equipo = new Equipo();
    when(this.repositorioEquipoMock.buscarEquipo(equipo.getNombre())).thenReturn(new Equipo());

    // ejecucion y validacion
    assertThrows(EquipoExistente.class, () -> this.servicioEquipo.registrarEquipo(equipo));
    verify(this.repositorioEquipoMock, times(0)).guardar(equipo);
  }

  @Test
  public void consultarEquipoPorIdSiNoExisteDeberiaRetornarNull() {
    // preparacion
    Long id = 99L;

    when(this.repositorioEquipoMock.buscarPorId(id)).thenReturn(null);

    // ejecucion
    Equipo equipoObtenido = this.servicioEquipo.consultarEquipoPorId(id);

    // validacion
    assertThat(equipoObtenido, equalTo(null));

    verify(this.repositorioEquipoMock, times(1)).buscarPorId(id);
  }


  @Test
  public void obtenerEquiposDeberiaRetornarLosEquipos() {

    // preparacion
    Equipo equipo1 = new Equipo();
    equipo1.setNombre("Boca");

    Equipo equipo2 = new Equipo();
    equipo2.setNombre("River");

    List<Equipo> equiposEsperados = Arrays.asList(equipo1, equipo2);

    when(repositorioEquipoMock.listar())
            .thenReturn(equiposEsperados);

    // ejecucion
    List<Equipo> equiposObtenidos = servicioEquipo.obtenerEquipos();

    // verificacion
    assertThat(equiposObtenidos, is(equiposEsperados));
    verify(repositorioEquipoMock, times(1)).listar();
  }

  @Test
  public void obtenerEquiposSiNoHayEquiposDeberiaRetornarListaVacia() {

    // preparacion
    when(repositorioEquipoMock.listar())
            .thenReturn(Arrays.asList());

    // ejecucion
    List<Equipo> equiposObtenidos = servicioEquipo.obtenerEquipos();

    // verificacion
    assertThat(equiposObtenidos, is(empty()));

    verify(repositorioEquipoMock, times(1)).listar();
  }

}
