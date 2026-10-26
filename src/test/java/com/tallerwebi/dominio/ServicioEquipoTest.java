package com.tallerwebi.dominio;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

import com.tallerwebi.dominio.excepcion.EquipoExistente;
import com.tallerwebi.dominio.excepcion.SiglasExistentes;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

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

    when(repositorioEquipoMock.listar()).thenReturn(equiposEsperados);

    // ejecucion
    List<Equipo> equiposObtenidos = servicioEquipo.obtenerEquipos();

    // verificacion
    assertThat(equiposObtenidos, is(equiposEsperados));
    verify(repositorioEquipoMock, times(1)).listar();
  }

  @Test
  public void obtenerEquiposSiNoHayEquiposDeberiaRetornarListaVacia() {
    // preparacion
    when(repositorioEquipoMock.listar()).thenReturn(Arrays.asList());

    // ejecucion
    List<Equipo> equiposObtenidos = servicioEquipo.obtenerEquipos();

    // verificacion
    assertThat(equiposObtenidos, is(empty()));

    verify(repositorioEquipoMock, times(1)).listar();
  }

  @Test
  void deberiaActualizarElEquipoCorrectamente() throws EquipoExistente, SiglasExistentes {
    // preparacion
    Equipo equipoAActualizar = new Equipo();
    equipoAActualizar.setId(1L);
    equipoAActualizar.setNombre("Equipo Modificado");

    // ejecucion
    servicioEquipo.actualizarEquipo(equipoAActualizar);

    // verificacion
    verify(repositorioEquipoMock, times(1)).actualizar(equipoAActualizar);
  }

  @Test
  public void deberiaLanzarExcepcionAlActualizarEquipoConNombreExistente() {
    // Preparación
    Equipo equipoAActualizar = new Equipo();
    equipoAActualizar.setId(1L);
    equipoAActualizar.setNombre("Boca Juniors");
    equipoAActualizar.setSiglas("BOC");

    Equipo otroEquipo = new Equipo();
    otroEquipo.setId(2L);
    otroEquipo.setNombre("Boca Juniors");
    otroEquipo.setSiglas("BJ");

    when(repositorioEquipoMock.buscarEquipo("Boca Juniors")).thenReturn(otroEquipo);

    // Ejecución y validación
    assertThrows(EquipoExistente.class, () -> servicioEquipo.actualizarEquipo(equipoAActualizar));

    verify(repositorioEquipoMock, never()).actualizar(any());
  }

  @Test
  public void deberiaLanzarExcepcionAlActualizarEquipoConSiglasExistentes() {
    // Preparación
    Equipo equipoAActualizar = new Equipo();
    equipoAActualizar.setId(1L);
    equipoAActualizar.setNombre("River Plate");
    equipoAActualizar.setSiglas("BOC");

    Equipo otroEquipo = new Equipo();
    otroEquipo.setId(2L);
    otroEquipo.setNombre("Otro Equipo");
    otroEquipo.setSiglas("BOC");

    when(repositorioEquipoMock.buscarEquipo("River Plate")).thenReturn(null);

    when(repositorioEquipoMock.buscarEquipoPorSiglas("BOC")).thenReturn(otroEquipo);

    // Ejecución y validación
    assertThrows(SiglasExistentes.class, () -> servicioEquipo.actualizarEquipo(equipoAActualizar));

    verify(repositorioEquipoMock, never()).actualizar(any());
  }

  @Test
  void deberiaLanzarSiglasExistentesCuandoLasSiglasYaEstanRegistradas()
    throws EquipoExistente, SiglasExistentes {
    // Preparación
    Equipo equipoARegistrar = new Equipo();
    equipoARegistrar.setNombre("Boca Juniors");
    equipoARegistrar.setSiglas("BOC");

    Equipo equipoExistente = new Equipo();
    equipoExistente.setNombre("Barcelona");
    equipoExistente.setSiglas("BOC");

    when(repositorioEquipoMock.buscarEquipo("Boca Juniors")).thenReturn(null);
    when(repositorioEquipoMock.buscarEquipoPorSiglas("BOC")).thenReturn(equipoExistente);

    // Ejecución y verificación
    assertThrows(
      SiglasExistentes.class,
      () -> {
        servicioEquipo.registrarEquipo(equipoARegistrar);
      }
    );

    verify(repositorioEquipoMock, times(1)).buscarEquipo("Boca Juniors");
    verify(repositorioEquipoMock, times(1)).buscarEquipoPorSiglas("BOC");
    verify(repositorioEquipoMock, never()).guardar(equipoARegistrar);
  }
}
