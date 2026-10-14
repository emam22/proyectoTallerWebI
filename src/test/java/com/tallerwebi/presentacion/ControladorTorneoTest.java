package com.tallerwebi.presentacion;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.text.IsEqualIgnoringCase.equalToIgnoringCase;
import static org.hamcrest.text.IsEqualIgnoringCase.equalToIgnoringCase;
import static org.mockito.Mockito.*;

import com.tallerwebi.dominio.*;
import com.tallerwebi.dominio.excepcion.TorneoExistente;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap;

public class ControladorTorneoTest {

  private ControladorTorneo controladorTorneo;
  private Torneo torneoMock;
  private ServicioTorneo servicioTorneoMock;
  private ServicioEquipoTorneo servicioEquipoTorneoMock;
  private ServicioEquipo servicioEquipoMock;

  @BeforeEach
  public void init() {
    torneoMock = mock(Torneo.class);
    servicioTorneoMock = mock(ServicioTorneo.class);
    servicioEquipoTorneoMock = mock(ServicioEquipoTorneo.class);
    servicioEquipoMock = mock(ServicioEquipo.class);
    controladorTorneo =
      new ControladorTorneo(servicioTorneoMock, servicioEquipoTorneoMock, servicioEquipoMock);
  }

  @Test
  public void alCrearTorneoSeDebeCrearTorneoYRedirigirACreacionDeEquipo() throws Exception {
    // preparación
    when(torneoMock.getId()).thenReturn(5L);

    RedirectAttributes redirectAttributes = new RedirectAttributesModelMap();

    // ejecución
    ModelAndView mav = controladorTorneo.crearTorneo(torneoMock, redirectAttributes);

    // validación
    assertThat(mav.getViewName(), equalToIgnoringCase("redirect:/creacion-equipo?idTorneo=5"));

    verify(servicioTorneoMock, times(1)).registrarTorneo(torneoMock);
  }

  @Test
  public void siElTorneoYaExisteSeDebeVolverALFormulario() throws Exception {
    //preparacion
    doThrow(TorneoExistente.class).when(servicioTorneoMock).registrarTorneo(torneoMock);

    RedirectAttributes redirectAttributes = new RedirectAttributesModelMap();

    //ejecucion
    ModelAndView mav = controladorTorneo.crearTorneo(torneoMock, redirectAttributes);

    //validacion
    assertThat(mav.getViewName(), equalToIgnoringCase("formulario-torneo"));
    assertThat(
      mav.getModel().get("error").toString(),
      equalToIgnoringCase("Ya existe un torneo con este nombre")
    );
  }

  @Test
  public void errorEnRegistrarTorneoDeberiaVolverAFormularioTorneoYMostrarError() throws Exception {
    // preparacion
    doThrow(RuntimeException.class).when(servicioTorneoMock).registrarTorneo(torneoMock);

    RedirectAttributes redirectAttributes = new RedirectAttributesModelMap();

    // ejecucion
    ModelAndView modelAndView = controladorTorneo.crearTorneo(torneoMock, redirectAttributes);

    // validacion
    assertThat(modelAndView.getViewName(), equalToIgnoringCase("formulario-torneo"));
    assertThat(
      modelAndView.getModel().get("error").toString(),
      equalToIgnoringCase("Error al crear el torneo")
    );
  }

  @Test
  public void crearEquipoDebePasarMinYMaxDelTorneoEnCurso() {
    // preparacion
    Torneo torneo = new Torneo();
    torneo.setId(1L);
    torneo.setCantidadMinJugadores(10);
    torneo.setCantidadMaxJugadores(15);
    when(servicioTorneoMock.consultarTorneoPorId(1L)).thenReturn(torneo);

    // ejecucion
    ModelAndView mav = controladorTorneo.crearEquipo(1L);

    // validacion
    assertThat(mav.getViewName(), equalToIgnoringCase("creacion-equipo"));
    assertThat(mav.getModel().get("minJugadores").toString(), equalTo("10"));
    assertThat(mav.getModel().get("maxJugadores").toString(), equalTo("15"));
  }

  @Test
  public void crearEquipoSinTorneosCargadosDeberiaUsarLosValoresPorDefecto() {
    // preparacion
    when(servicioTorneoMock.consultarTorneoPorId(1L)).thenReturn(null);

    // ejecucion
    ModelAndView mav = controladorTorneo.crearEquipo(1L);

    // validacion
    assertThat(mav.getViewName(), equalToIgnoringCase("creacion-equipo"));
    assertThat(mav.getModel().get("minJugadores").toString(), equalTo("11"));
    assertThat(mav.getModel().get("maxJugadores").toString(), equalTo("23"));
  }

  @Test
  public void crearTorneoDeberiaRedirigirAcreacionEquipoConIdDelTorneo() {
    //preparacion
    Torneo torneo = new Torneo();
    torneo.setId(5L);
    torneo.setNombre("MegaFutbol");

    RedirectAttributes redirectAttributes = new RedirectAttributesModelMap();

    //ejecucion
    ModelAndView mav = controladorTorneo.crearTorneo(torneo, redirectAttributes);

    // validacion
    assertThat(mav.getViewName(), equalTo("redirect:/creacion-equipo?idTorneo=5"));
  }

  @Test
  public void crearEquipoDeberiaPasarElIdDelTorneoAlModelo() {
    // preparacion
    Torneo torneo = new Torneo();
    torneo.setId(5L);

    when(servicioTorneoMock.consultarTorneoPorId(5L)).thenReturn(torneo);

    // ejecucion
    ModelAndView mav = controladorTorneo.crearEquipo(5L);

    // validacion
    assertThat(mav.getModel().get("idTorneo"), equalTo(5L));
  }

  @Test
  public void crearEquipoDeberiaConsultarElTorneoConElIdRecibido() {
    // preparacion
    Torneo torneo = new Torneo();
    torneo.setId(8L);

    when(servicioTorneoMock.consultarTorneoPorId(8L)).thenReturn(torneo);

    // ejecucion
    controladorTorneo.crearEquipo(8L);

    // validacion
    verify(servicioTorneoMock, times(1)).consultarTorneoPorId(8L);
  }

  //metodo verFixture
  @Test
  public void finalizarTorneoConTodosLosEquiposDeberiaGenerarFixture() throws Exception {
    // preparación
    Torneo torneo = new Torneo();
    torneo.setId(1L);
    torneo.setCantidadDeEquipos(2);

    when(servicioTorneoMock.consultarTorneoPorId(1L)).thenReturn(torneo);
    when(servicioEquipoTorneoMock.contarEquiposPorTorneo(1L)).thenReturn(2);
    when(servicioTorneoMock.obtenerJornadasPorTorneo(1L)).thenReturn(new ArrayList<>());

    Equipo equipo1 = mock(Equipo.class);
    Equipo equipo2 = mock(Equipo.class);

    when(equipo1.getId()).thenReturn(10L);
    when(equipo2.getId()).thenReturn(20L);

    EquipoTorneo agregarEquipo1 = mock(EquipoTorneo.class);
    EquipoTorneo agregarEquipo2 = mock(EquipoTorneo.class);

    when(agregarEquipo1.getEquipo()).thenReturn(equipo1);
    when(agregarEquipo2.getEquipo()).thenReturn(equipo2);

    when(servicioEquipoTorneoMock.obtenerEquiposPorTorneo(1L))
      .thenReturn(Arrays.asList(agregarEquipo1, agregarEquipo2));

    RedirectAttributes redirectAttributes = new RedirectAttributesModelMap();

    // ejecución
    ModelAndView mav = controladorTorneo.finalizarTorneo(1L, redirectAttributes);

    // validación

    verify(servicioTorneoMock).crearTorneoConFixture(eq(torneo), eq(Arrays.asList(10L, 20L)));
  }

  @Test
  public void verFixtureDeberiaMostrarTorneoYJornadas() throws Exception {
    //preparacion

    Torneo torneo = new Torneo();
    torneo.setId(1L);
    torneo.setNombre("Mega Futbol");

    Jornada jornada = new Jornada();
    jornada.setNumero(1);

    List<Jornada> jornadas = new ArrayList<>();
    jornadas.add(jornada);

    when(servicioTorneoMock.consultarTorneoPorId(1L)).thenReturn(torneo);
    when(servicioTorneoMock.obtenerJornadasPorTorneo(1L)).thenReturn(jornadas);

    //ejecucion
    ModelAndView mav = controladorTorneo.verFixture(1L);

    //validacion
    assertThat(mav.getViewName(), equalTo("fixtureTorneo"));
    assertThat(mav.getModel().get("torneo"), equalTo(torneo));
    assertThat(mav.getModel().get("jornadas"), equalTo(jornadas));
  }
}
