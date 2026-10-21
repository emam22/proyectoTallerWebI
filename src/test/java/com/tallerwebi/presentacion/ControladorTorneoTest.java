package com.tallerwebi.presentacion;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.text.IsEqualIgnoringCase.equalToIgnoringCase;
import static org.hamcrest.text.IsEqualIgnoringCase.equalToIgnoringCase;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

import com.tallerwebi.dominio.*;
import com.tallerwebi.dominio.excepcion.EquipoExistente;
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
    // preparacion
    when(torneoMock.getId()).thenReturn(5L);
    when(torneoMock.getCantidadMinJugadores()).thenReturn(11);
    when(torneoMock.getCantidadMaxJugadores()).thenReturn(23);

    RedirectAttributes redirectAttributes = new RedirectAttributesModelMap();

    // ejecucion
    ModelAndView mav = controladorTorneo.crearTorneo(torneoMock, redirectAttributes);

    // validacion
    assertThat(mav.getViewName(), equalToIgnoringCase("redirect:/creacion-equipo?idTorneo=5"));

    verify(servicioTorneoMock, times(1)).registrarTorneo(torneoMock);
  }

  @Test
  public void siElTorneoYaExisteSeDebeVolverALFormulario() throws Exception {
    // Preparación
    when(torneoMock.getCantidadMinJugadores()).thenReturn(11);
    when(torneoMock.getCantidadMaxJugadores()).thenReturn(23);

    doThrow(TorneoExistente.class).when(servicioTorneoMock).registrarTorneo(torneoMock);

    RedirectAttributes redirectAttributes = new RedirectAttributesModelMap();

    // Ejecución
    ModelAndView mav = controladorTorneo.crearTorneo(torneoMock, redirectAttributes);

    // Validación
    assertThat(mav.getViewName(), equalToIgnoringCase("redirect:/formulario-torneo"));

    assertThat(
      redirectAttributes.getFlashAttributes().get("error").toString(),
      equalToIgnoringCase("Ya existe un torneo con este nombre")
    );
  }

  @Test
  public void errorEnRegistrarTorneoDeberiaVolverAFormularioTorneoYMostrarError() throws Exception {
    // preparacion
    when(torneoMock.getCantidadMinJugadores()).thenReturn(11);
    when(torneoMock.getCantidadMaxJugadores()).thenReturn(23);

    doThrow(RuntimeException.class).when(servicioTorneoMock).registrarTorneo(torneoMock);

    RedirectAttributes redirectAttributes = new RedirectAttributesModelMap();

    // ejecucion
    ModelAndView modelAndView = controladorTorneo.crearTorneo(torneoMock, redirectAttributes);

    // validacion
    assertThat(modelAndView.getViewName(), equalToIgnoringCase("redirect:/formulario-torneo"));

    assertThat(
      redirectAttributes.getFlashAttributes().get("error").toString(),
      equalToIgnoringCase("Error al crear el torneo")
    );
  }

  @Test
  public void crearEquipoDebePasarMinYMaxDelTorneoEnCurso() {
    // preparacion
    Torneo torneo = new Torneo();
    torneo.setId(1L);
    torneo.setCantidadDeEquipos(8);
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
    torneo.setCantidadMaxJugadores(23);
    torneo.setCantidadMinJugadores(11);
    torneo.setCantidadDeEquipos(8);

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
    torneo.setCantidadDeEquipos(8);
    torneo.setCantidadMinJugadores(11);
    torneo.setCantidadMaxJugadores(23);

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
    torneo.setCantidadDeEquipos(8);
    torneo.setCantidadMinJugadores(11);
    torneo.setCantidadMaxJugadores(23);

    when(servicioTorneoMock.consultarTorneoPorId(8L)).thenReturn(torneo);

    // ejecucion
    controladorTorneo.crearEquipo(8L);

    // verificacion
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

  //mas test para seguir validando metodos

  @Test
  public void siLaCantidadMaximaNoEsValidaDebeVolverAlFormulario() throws Exception {
    // preparación
    when(torneoMock.getCantidadMinJugadores()).thenReturn(11);
    when(torneoMock.getCantidadMaxJugadores()).thenReturn(11);

    RedirectAttributes redirectAttributes = new RedirectAttributesModelMap();

    // ejecución
    ModelAndView mav = controladorTorneo.crearTorneo(torneoMock, redirectAttributes);

    // validación
    assertThat(mav.getViewName(), equalTo("redirect:/formulario-torneo"));
    assertThat(
      redirectAttributes.getFlashAttributes().get("error"),
      equalTo("La cantidad máxima de jugadores debe ser mayor que la mínima")
    );

    verify(servicioTorneoMock, never()).registrarTorneo(torneoMock);
  }

  @Test
  public void alIrAlFormularioTorneoDebeMostrarLaVistaYCrearUnTorneo() {
    // ejecución
    ModelAndView mav = controladorTorneo.irAFormularioTorneo();

    // Validación
    assertThat(mav.getViewName(), equalTo("formulario-torneo"));
    assertThat(mav.getModel().containsKey("torneo"), equalTo(true));
  }

  @Test
  public void alListarEquiposDebeMostrarLaVistaYConsultarLosEquipos() {
    // preparación
    Long idTorneo = 5L;
    List<Equipo> equipos = new ArrayList<>();

    when(servicioEquipoMock.obtenerEquipos()).thenReturn(equipos);

    // ejecución
    ModelAndView mav = controladorTorneo.listarEquipos(idTorneo);

    // validación
    assertThat(mav.getViewName(), equalTo("lista-equipos"));
    assertThat(mav.getModel().get("equipos"), equalTo(equipos));
    verify(servicioEquipoMock, times(1)).obtenerEquipos();
  }

  @Test
  public void alGuardarEquipoExistenteDebeMostrarErrorYRedirigir() throws EquipoExistente {
    // preparación
    Long idTorneo = 1L;

    Torneo torneo = mock(Torneo.class);
    when(servicioTorneoMock.consultarTorneoPorId(idTorneo)).thenReturn(torneo);
    when(torneo.getCantidadDeEquipos()).thenReturn(4);
    when(servicioEquipoTorneoMock.contarEquiposPorTorneo(idTorneo)).thenReturn(0);

    doThrow(new EquipoExistente())
      .when(servicioEquipoTorneoMock)
      .crearEquipoEInscribirlo(any(Equipo.class), any(Torneo.class));

    RedirectAttributes redirectAttributes = new RedirectAttributesModelMap();

    // ejecución
    ModelAndView mav = controladorTorneo.guardarEquipo(
      idTorneo,
      "Los Leones",
      "#FF0000",
      "#FFFFFF",
      "#0000FF",
      "#FFFFFF",
      redirectAttributes
    );

    // validación
    assertThat(mav.getViewName(), equalTo("redirect:/creacion-equipo?idTorneo=1"));
    assertThat(
      redirectAttributes.getFlashAttributes().get("error"),
      equalTo("Ya existe un equipo con ese nombre")
    );
  }

  @Test
  public void desvincularEquipoDeberiaDesvincularEquipoYRedirigir() {
    // preparacion
    Long idEquipo = 3L;
    Long idTorneo = 8L;

    RedirectAttributes redirectAttributes = new RedirectAttributesModelMap();

    // ejecucion
    ModelAndView mav = controladorTorneo.desvincularEquipo(idEquipo, idTorneo, redirectAttributes);

    // verificacion
    verify(servicioEquipoTorneoMock).desvincularEquipo(idEquipo, idTorneo);

    assertEquals("redirect:/desvincular-equipo?idTorneo=8", mav.getViewName());

    assertEquals(
      "Equipo desvinculado correctamente",
      redirectAttributes.getFlashAttributes().get("mensaje")
    );
  }

  @Test
  public void deberiaActualizarElEquipoYRedirigirAlAdministrarTorneo() {
    // preparacion
    Long idEquipo = 1L;
    Long idTorneo = 2L;
    Equipo equipo = new Equipo();
    equipo.setId(idEquipo);
    equipo.setNombre("Equipo Viejo");

    when(servicioEquipoMock.consultarEquipoPorId(idEquipo)).thenReturn(equipo);

    // ejecucion
    ModelAndView mav = controladorTorneo.actualizarEquipo(
      idEquipo,
      idTorneo,
      "Equipo Nuevo",
      "#ffffff",
      "#000000",
      "#ff0000",
      "#00ff00"
    );

    // validacion
    assertThat(mav.getViewName(), equalTo("redirect:/administrarTorneoAdmin/" + idTorneo));
    assertThat(equipo.getNombre(), equalTo("Equipo Nuevo"));
    assertThat(equipo.getColorLocal1(), equalTo("#ffffff"));

    verify(servicioEquipoMock, times(1)).actualizarEquipo(equipo);
  }

  @Test
  public void deberiaRedirigirAEditarEquipoSiElEquipoNoExisteAlActualizar() {
    // preparacion
    Long idEquipo = 99L;
    Long idTorneo = 2L;

    when(servicioEquipoMock.consultarEquipoPorId(idEquipo)).thenReturn(null);

    // ejecucion
    ModelAndView mav = controladorTorneo.actualizarEquipo(
      idEquipo,
      idTorneo,
      "Nombre",
      "#ffffff",
      "#000000",
      "#ff0000",
      "#00ff00"
    );

    // validacion
    assertThat(mav.getViewName(), equalTo("redirect:/editarEquipo?idTorneo=" + idTorneo));
    verify(servicioEquipoMock, never()).actualizarEquipo(any());
  }
}
