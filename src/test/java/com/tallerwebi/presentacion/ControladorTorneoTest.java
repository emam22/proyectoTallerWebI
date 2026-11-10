package com.tallerwebi.presentacion;

import static net.bytebuddy.matcher.ElementMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.text.IsEqualIgnoringCase.equalToIgnoringCase;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

import com.tallerwebi.dominio.*;
import com.tallerwebi.dominio.excepcion.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import java.time.LocalDateTime;
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
  private Usuario usuarioMock;
  private HttpServletRequest requestMock;
  private HttpSession sessionMock;
  private ValidadorTorneo validadorTorneoMock;

  @BeforeEach
  public void init() {
    torneoMock = mock(Torneo.class);
    servicioTorneoMock = mock(ServicioTorneo.class);
    servicioEquipoTorneoMock = mock(ServicioEquipoTorneo.class);
    servicioEquipoMock = mock(ServicioEquipo.class);
    usuarioMock = mock(Usuario.class);
    requestMock = mock(HttpServletRequest.class);
    sessionMock = mock(HttpSession.class);
    validadorTorneoMock = mock(ValidadorTorneo.class);
    controladorTorneo =
      new ControladorTorneo(
        servicioTorneoMock,
        servicioEquipoTorneoMock,
        servicioEquipoMock,
        validadorTorneoMock
      );
  }

  private Torneo torneoConId(Long id) {
    Torneo torneo = new Torneo();
    torneo.setId(id);
    torneo.setFechaInicio(LocalDateTime.now().plusDays(5));

    return torneo;
  }

  private Equipo equipoConId(Long id) {
    Equipo equipo = new Equipo();
    equipo.setId(id);

    return equipo;
  }

  private ActualizarEquipoDto actualizarDto(
    Equipo equipo,
    Torneo torneo,
    String nombre,
    String sigla,
    String colorLocal1,
    String colorLocal2,
    String colorVisitante1,
    String colorVisitante2,
    String escudoRuta
  ) {
    ActualizarEquipoDto dto = new ActualizarEquipoDto();
    dto.setIdEquipo(equipo == null ? null : equipo.getId());
    dto.setIdTorneo(torneo == null ? null : torneo.getId());
    dto.setNombre(nombre);
    dto.setSigla(sigla);
    dto.setColorLocal1(colorLocal1);
    dto.setColorLocal2(colorLocal2);
    dto.setColorVisitante1(colorVisitante1);
    dto.setColorVisitante2(colorVisitante2);
    dto.setEscudoRuta(escudoRuta);

    return dto;
  }

  private EquipoForm equipoForm(String nombre, String sigla) {
    EquipoForm form = new EquipoForm();
    form.setNombreEquipo(nombre);
    form.setSigla(sigla);
    form.setColorLocal1("#FF0000");
    form.setColorLocal2("#FFFFFF");
    form.setColorVisitante1("#0000FF");
    form.setColorVisitante2("#FFFFFF");

    return form;
  }

  @Test
  public void alCrearTorneoSeDebeCrearTorneoYRedirigirACreacionDeEquipo() throws Exception {
    // preparacion
    when(torneoMock.getId()).thenReturn(5L);
    when(usuarioMock.getRol()).thenReturn("admin");
    when(requestMock.getSession()).thenReturn(sessionMock);
    when(sessionMock.getAttribute("USUARIO")).thenReturn(usuarioMock);
    when(validadorTorneoMock.tieneCantidadJugadoresValida(torneoMock)).thenReturn(true);

    when(validadorTorneoMock.tieneFechaInicioValida(torneoMock)).thenReturn(true);

    RedirectAttributes redirectAttributes = new RedirectAttributesModelMap();

    // ejecucion
    ModelAndView mav = controladorTorneo.crearTorneo(torneoMock, requestMock, redirectAttributes);

    // validacion
    assertThat(mav.getViewName(), equalToIgnoringCase("redirect:/creacion-equipo?idTorneo=5"));

    verify(servicioTorneoMock, times(1)).registrarTorneo(torneoMock, usuarioMock);
  }

  @Test
  public void siElTorneoYaExisteSeDebeVolverALFormulario() throws Exception {
    // Preparación
    when(requestMock.getSession()).thenReturn(sessionMock);
    when(sessionMock.getAttribute("USUARIO")).thenReturn(usuarioMock);
    when(usuarioMock.getRol()).thenReturn("admin");
    when(validadorTorneoMock.tieneCantidadJugadoresValida(torneoMock)).thenReturn(true);

    when(validadorTorneoMock.tieneFechaInicioValida(torneoMock)).thenReturn(true);

    doThrow(TorneoExistente.class)
      .when(servicioTorneoMock)
      .registrarTorneo(torneoMock, usuarioMock);

    RedirectAttributes redirectAttributes = new RedirectAttributesModelMap();

    // Ejecución
    ModelAndView mav = controladorTorneo.crearTorneo(torneoMock, requestMock, redirectAttributes);

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
    when(requestMock.getSession()).thenReturn(sessionMock);
    when(sessionMock.getAttribute("USUARIO")).thenReturn(usuarioMock);
    when(usuarioMock.getRol()).thenReturn("admin");
    when(validadorTorneoMock.tieneCantidadJugadoresValida(torneoMock)).thenReturn(true);

    when(validadorTorneoMock.tieneFechaInicioValida(torneoMock)).thenReturn(true);

    doThrow(RuntimeException.class)
      .when(servicioTorneoMock)
      .registrarTorneo(torneoMock, usuarioMock);

    RedirectAttributes redirectAttributes = new RedirectAttributesModelMap();

    // ejecucion
    ModelAndView modelAndView = controladorTorneo.crearTorneo(
      torneoMock,
      requestMock,
      redirectAttributes
    );

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
    torneo.setCantidadDeEquipos(8);

    when(validadorTorneoMock.tieneCantidadJugadoresValida(torneo)).thenReturn(true);

    when(validadorTorneoMock.tieneFechaInicioValida(torneo)).thenReturn(true);
    when(requestMock.getSession()).thenReturn(sessionMock);
    when(sessionMock.getAttribute("USUARIO")).thenReturn(usuarioMock);
    when(usuarioMock.getRol()).thenReturn("admin");

    RedirectAttributes redirectAttributes = new RedirectAttributesModelMap();

    //ejecucion
    ModelAndView mav = controladorTorneo.crearTorneo(torneo, requestMock, redirectAttributes);

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
    // Preparación
    Torneo torneo = new Torneo();
    torneo.setId(1L);
    torneo.setCantidadDeEquipos(2);

    RedirectAttributes redirectAttributes = new RedirectAttributesModelMap();

    // Ejecución
    ModelAndView mav = controladorTorneo.finalizarTorneo(torneo, redirectAttributes);

    // Validación
    assertThat(mav.getViewName(), equalTo("redirect:/lobbyAdmin"));

    verify(servicioTorneoMock, times(1)).finalizarTorneo(1L);
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
    when(sessionMock.getAttribute("USUARIO")).thenReturn(usuarioMock);

    doThrow(
      new IllegalArgumentException("La cantidad máxima de jugadores debe ser mayor que la mínima")
    )
      .when(servicioTorneoMock)
      .registrarTorneo(torneoMock, usuarioMock);

    RedirectAttributes redirectAttributes = new RedirectAttributesModelMap();

    // ejecución
    ModelAndView mav = controladorTorneo.crearTorneo(torneoMock, requestMock, redirectAttributes);

    // validación
    assertThat(mav.getViewName(), equalTo("redirect:/formulario-torneo"));
    assertThat(
      redirectAttributes.getFlashAttributes().get("error"),
      equalTo("La cantidad máxima de jugadores debe ser mayor que la mínima")
    );

    verify(servicioTorneoMock, never()).registrarTorneo(torneoMock, usuarioMock);
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
  public void alGuardarEquipoExistenteDebeMostrarErrorYRedirigir()
    throws EquipoExistente, SiglaExistente, SiglaInvalida {
    // preparación
    Long idTorneo = 1L;

    Torneo torneo = mock(Torneo.class);
    when(torneo.getId()).thenReturn(idTorneo);
    when(servicioTorneoMock.consultarTorneoPorId(idTorneo)).thenReturn(torneo);
    when(torneo.getCantidadDeEquipos()).thenReturn(4);
    when(servicioEquipoTorneoMock.contarEquiposPorTorneo(idTorneo)).thenReturn(0);

    doThrow(new EquipoExistente())
      .when(servicioEquipoTorneoMock)
      .crearEquipoEInscribirlo(any(Equipo.class), any(Torneo.class));

    RedirectAttributes redirectAttributes = new RedirectAttributesModelMap();

    // ejecución
    ModelAndView mav = controladorTorneo.guardarEquipo(
      torneo,
      equipoForm("Los Leones", "BOC"),
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
    ModelAndView mav = controladorTorneo.desvincularEquipo(
      equipoConId(idEquipo),
      torneoConId(idTorneo),
      redirectAttributes
    );

    // verificacion
    verify(servicioEquipoTorneoMock).desvincularEquipo(idEquipo, idTorneo);

    assertEquals("redirect:/desvincular-equipo?idTorneo=8", mav.getViewName());

    assertEquals(
      "Equipo desvinculado correctamente",
      redirectAttributes.getFlashAttributes().get("mensaje")
    );
  }

  @Test
  public void deberiaActualizarElEquipoYRedirigirAlAdministrarTorneo()
    throws EquipoExistente, SiglaExistente, SiglaInvalida {
    // Preparación
    Long idEquipo = 1L;
    Long idTorneo = 2L;

    Equipo equipo = new Equipo();
    equipo.setId(idEquipo);
    equipo.setNombre("Equipo Viejo");
    equipo.setSigla("EV");

    when(servicioEquipoMock.consultarEquipoPorId(idEquipo)).thenReturn(equipo);

    // Ejecución
    ModelAndView mav = controladorTorneo.actualizarEquipo(
      actualizarDto(
        equipo,
        torneoConId(idTorneo),
        "Equipo Nuevo",
        "EN",
        "#ffffff",
        "#000000",
        "#ff0000",
        "#00ff00",
        null
      )
    );

    // Validación
    assertThat(mav.getViewName(), equalTo("redirect:/administrarTorneoAdmin/" + idTorneo));
    assertThat(equipo.getNombre(), equalTo("Equipo Nuevo"));
    assertThat(equipo.getSigla(), equalTo("EN"));
    assertThat(equipo.getColorLocal1(), equalTo("#ffffff"));

    verify(servicioEquipoMock, times(1)).actualizarEquipo(equipo);
  }

  @Test
  public void deberiaRedirigirAEditarEquipoSiElEquipoNoExisteAlActualizar()
    throws EquipoExistente, SiglaExistente, SiglaInvalida {
    // Preparación
    Long idEquipo = 99L;
    Long idTorneo = 2L;

    when(servicioEquipoMock.consultarEquipoPorId(idEquipo)).thenReturn(null);

    // Ejecución
    ModelAndView mav = controladorTorneo.actualizarEquipo(
      actualizarDto(
        null,
        torneoConId(idTorneo),
        "Nombre",
        "NOM",
        "#ffffff",
        "#000000",
        "#ff0000",
        "#00ff00",
        null
      )
    );

    // Validación
    assertThat(mav.getViewName(), equalTo("redirect:/editar-equipo?idTorneo=" + idTorneo));

    verify(servicioEquipoMock, never()).actualizarEquipo(any());
  }

  @Test
  public void finalizarTorneoSiNoExisteDeberiaRedirigirAlLobbyAdmin() {
    RedirectAttributes redirectAttributes = new RedirectAttributesModelMap();

    // Ejecución
    ModelAndView mav = controladorTorneo.finalizarTorneo(null, redirectAttributes);

    // Validación
    assertThat(mav.getViewName(), equalTo("redirect:/lobbyAdmin"));
    assertThat(
      redirectAttributes.getFlashAttributes().get("error"),
      equalTo("No se encontró el torneo.")
    );
  }

  @Test
  public void finalizarTorneoConCantidadIncorrectaDeEquiposDeberiaMostrarError() throws Exception {
    // Preparación
    Torneo torneo = new Torneo();
    torneo.setId(1L);
    torneo.setCantidadDeEquipos(4);

    when(servicioTorneoMock.consultarTorneoPorId(1L)).thenReturn(torneo);

    doThrow(
      new IllegalArgumentException(
        "La cantidad de equipos inscriptos no coincide con la configurada."
      )
    )
      .when(servicioTorneoMock)
      .finalizarTorneo(1L);

    Equipo equipo = new Equipo();
    equipo.setId(10L);

    EquipoTorneo inscripcion = new EquipoTorneo();
    inscripcion.setEquipo(equipo);

    when(servicioEquipoTorneoMock.obtenerEquiposPorTorneo(1L))
      .thenReturn(Arrays.asList(inscripcion));

    RedirectAttributes redirectAttributes = new RedirectAttributesModelMap();

    // Ejecución
    ModelAndView mav = controladorTorneo.finalizarTorneo(torneo, redirectAttributes);

    // Validación
    assertThat(mav.getViewName(), equalTo("redirect:/lobbyAdmin"));
    assertThat(
      redirectAttributes.getFlashAttributes().get("error"),
      equalTo("La cantidad de equipos inscriptos no coincide con la configurada.")
    );
    verify(servicioTorneoMock, never()).crearTorneoConFixture(any(), anyList());
  }

  @Test
  public void finalizarTorneoConFixtureExistenteNoDeberiaGenerarloNuevamente() throws Exception {
    // Preparación
    Torneo torneo = new Torneo();
    torneo.setId(1L);
    torneo.setCantidadDeEquipos(2);

    when(servicioTorneoMock.consultarTorneoPorId(1L)).thenReturn(torneo);

    Equipo equipo1 = new Equipo();
    equipo1.setId(10L);

    Equipo equipo2 = new Equipo();
    equipo2.setId(20L);

    EquipoTorneo inscripcion1 = new EquipoTorneo();
    inscripcion1.setEquipo(equipo1);

    EquipoTorneo inscripcion2 = new EquipoTorneo();
    inscripcion2.setEquipo(equipo2);

    when(servicioEquipoTorneoMock.obtenerEquiposPorTorneo(1L))
      .thenReturn(Arrays.asList(inscripcion1, inscripcion2));

    Jornada jornada = mock(Jornada.class);
    when(jornada.getPartidos()).thenReturn(Arrays.asList(mock(Partido.class)));

    when(servicioTorneoMock.obtenerJornadasPorTorneo(1L)).thenReturn(Arrays.asList(jornada));

    RedirectAttributes redirectAttributes = new RedirectAttributesModelMap();

    // Ejecución
    ModelAndView mav = controladorTorneo.finalizarTorneo(torneo, redirectAttributes);

    // Validación
    assertThat(mav.getViewName(), equalTo("redirect:/lobbyAdmin"));

    verify(servicioTorneoMock, never()).crearTorneoConFixture(any(), anyList());
  }

  @Test
  public void finalizarTorneoSiFallaLaConsultaDeInscripcionesDeberiaMostrarError()
    throws Exception {
    // Preparación
    Torneo torneo = new Torneo();
    torneo.setId(1L);
    torneo.setCantidadDeEquipos(2);

    doThrow(new RuntimeException("Error de consulta")).when(servicioTorneoMock).finalizarTorneo(1L);

    when(servicioEquipoTorneoMock.obtenerEquiposPorTorneo(1L))
      .thenThrow(new RuntimeException("Error de consulta"));

    RedirectAttributes redirectAttributes = new RedirectAttributesModelMap();

    // Ejecución
    ModelAndView mav = controladorTorneo.finalizarTorneo(torneo, redirectAttributes);

    // Validación
    assertThat(mav.getViewName(), equalTo("redirect:/lobbyAdmin"));
    assertThat(
      redirectAttributes.getFlashAttributes().get("error"),
      equalTo("No se pudo generar el fixture: Error de consulta")
    );
  }

  @Test
  public void finalizarTorneoSiFallaLaCreacionDelFixtureDeberiaMostrarError() throws Exception {
    // Preparación
    Torneo torneo = new Torneo();
    torneo.setId(1L);
    torneo.setCantidadDeEquipos(2);

    when(servicioTorneoMock.consultarTorneoPorId(1L)).thenReturn(torneo);

    Equipo equipo1 = new Equipo();
    equipo1.setId(10L);

    Equipo equipo2 = new Equipo();
    equipo2.setId(20L);

    EquipoTorneo inscripcion1 = new EquipoTorneo();
    inscripcion1.setEquipo(equipo1);

    EquipoTorneo inscripcion2 = new EquipoTorneo();
    inscripcion2.setEquipo(equipo2);

    when(servicioEquipoTorneoMock.obtenerEquiposPorTorneo(1L))
      .thenReturn(Arrays.asList(inscripcion1, inscripcion2));

    when(servicioTorneoMock.obtenerJornadasPorTorneo(1L)).thenReturn(new ArrayList<>());

    doThrow(new RuntimeException("Error al crear fixture"))
      .when(servicioTorneoMock)
      .finalizarTorneo(1L);

    RedirectAttributes redirectAttributes = new RedirectAttributesModelMap();

    // Ejecución
    ModelAndView mav = controladorTorneo.finalizarTorneo(torneo, redirectAttributes);

    // Validación
    assertThat(mav.getViewName(), equalTo("redirect:/lobbyAdmin"));
    assertThat(
      redirectAttributes.getFlashAttributes().get("error"),
      equalTo("No se pudo generar el fixture: Error al crear fixture")
    );
  }

  @Test
  public void finalizarTorneoSiLasJornadasSonNullDeberiaGenerarFixture() {
    Torneo torneo = mock(Torneo.class);

    when(torneo.getId()).thenReturn(1L);
    when(servicioTorneoMock.consultarTorneoPorId(1L)).thenReturn(torneo);
    when(torneo.getCantidadDeEquipos()).thenReturn(2);

    Equipo equipo1 = new Equipo();
    equipo1.setId(10L);

    Equipo equipo2 = new Equipo();
    equipo2.setId(20L);

    EquipoTorneo inscripcion1 = new EquipoTorneo();
    inscripcion1.setEquipo(equipo1);

    EquipoTorneo inscripcion2 = new EquipoTorneo();
    inscripcion2.setEquipo(equipo2);

    when(servicioEquipoTorneoMock.obtenerEquiposPorTorneo(1L))
      .thenReturn(Arrays.asList(inscripcion1, inscripcion2));

    when(servicioTorneoMock.obtenerJornadasPorTorneo(1L)).thenReturn(null);

    RedirectAttributes redirectAttributes = new RedirectAttributesModelMap();

    ModelAndView resultado = controladorTorneo.finalizarTorneo(torneo, redirectAttributes);

    assertEquals("redirect:/lobbyAdmin", resultado.getViewName());
  }

  @Test
  public void alListarEquiposAdminDebeMostrarLaVistaYElIdDelTorneo() {
    Long idTorneo = 8L;
    List<Equipo> equipos = new ArrayList<>();

    when(servicioEquipoMock.obtenerEquipos()).thenReturn(equipos);

    ModelAndView mav = controladorTorneo.listarEquiposAdmin(idTorneo);

    assertEquals("lista-equipos-admin", mav.getViewName());
    assertEquals(equipos, mav.getModel().get("equipos"));
    assertEquals(idTorneo, mav.getModel().get("idTorneo"));

    verify(servicioEquipoMock).obtenerEquipos();
  }

  @Test
  public void mostrarEquiposParaDesvincularDebeCargarLasInscripciones()
    throws FechaLimiteEditarTorneo {
    Long idTorneo = 5L;
    List<EquipoTorneo> inscripciones = new ArrayList<>();

    RedirectAttributes redirectAttributes = mock(RedirectAttributes.class);

    when(servicioEquipoTorneoMock.obtenerEquiposPorTorneo(idTorneo)).thenReturn(inscripciones);

    ModelAndView mav = controladorTorneo.mostrarEquiposParaDesvincular(
      idTorneo,
      redirectAttributes
    );

    assertEquals("desvincularEquipo", mav.getViewName());
    assertEquals(idTorneo, mav.getModel().get("idTorneo"));
    assertEquals(inscripciones, mav.getModel().get("equipos"));

    verify(servicioEquipoTorneoMock).obtenerEquiposPorTorneo(idTorneo);
  }

  @Test
  public void mostrarEquiposParaEditarDebeCargarLasInscripciones() throws FechaLimiteEditarTorneo {
    Long idTorneo = 3L;
    List<EquipoTorneo> inscripciones = new ArrayList<>();

    RedirectAttributes r = mock(RedirectAttributes.class);

    when(servicioEquipoTorneoMock.obtenerEquiposPorTorneo(idTorneo)).thenReturn(inscripciones);

    ModelAndView mav = controladorTorneo.mostrarEquiposParaEditar(idTorneo, r);

    assertEquals("editarEquipo", mav.getViewName());
    assertEquals(idTorneo, mav.getModel().get("idTorneo"));
    assertEquals(inscripciones, mav.getModel().get("equipos"));

    verify(servicioEquipoTorneoMock).obtenerEquiposPorTorneo(idTorneo);
  }

  @Test
  public void mostrarFormularioEditarEquipoSiExisteDebeMostrarElFormulario() {
    Long idEquipo = 4L;
    Long idTorneo = 7L;

    Equipo equipo = new Equipo();
    equipo.setId(idEquipo);
    equipo.setNombre("Los Leones");

    when(servicioEquipoMock.consultarEquipoPorId(idEquipo)).thenReturn(equipo);

    ModelAndView mav = controladorTorneo.mostrarFormularioEditarEquipo(idEquipo, idTorneo);

    assertEquals("formulario-editar-equipo", mav.getViewName());
    assertEquals(equipo, mav.getModel().get("equipo"));
    assertEquals(idTorneo, mav.getModel().get("idTorneo"));

    verify(servicioEquipoMock).consultarEquipoPorId(idEquipo);
  }

  @Test
  public void mostrarFormularioEditarEquipoSiNoExisteDebeRedirigir() {
    Long idEquipo = 1L;
    Long idTorneo = 2L;

    when(servicioEquipoMock.consultarEquipoPorId(idEquipo)).thenReturn(null);

    ModelAndView mav = controladorTorneo.mostrarFormularioEditarEquipo(idEquipo, idTorneo);

    assertEquals("redirect:/editarEquipo?idTorneo=" + idTorneo, mav.getViewName());

    verify(servicioEquipoMock).consultarEquipoPorId(idEquipo);
  }

  @Test
  public void alGuardarEquipoCorrectamenteDebeInscribirloYRedirigir() throws Exception {
    // Preparación
    Long idTorneo = 1L;

    Torneo torneo = mock(Torneo.class);
    when(torneo.getId()).thenReturn(idTorneo);
    when(servicioTorneoMock.consultarTorneoPorId(idTorneo)).thenReturn(torneo);
    when(torneo.getCantidadDeEquipos()).thenReturn(4);
    when(servicioEquipoTorneoMock.contarEquiposPorTorneo(idTorneo)).thenReturn(2);

    RedirectAttributes redirectAttributes = new RedirectAttributesModelMap();

    // Ejecución
    ModelAndView mav = controladorTorneo.guardarEquipo(
      torneo,
      equipoForm("Los Leones", "BOC"),
      redirectAttributes
    );

    // Validación
    assertThat(mav.getViewName(), equalTo("redirect:/creacion-equipo?idTorneo=1"));

    verify(servicioEquipoTorneoMock)
      .crearEquipoEInscribirlo(
        argThat(equipo ->
          equipo.getNombre().equals("Los Leones") &&
          equipo.getColorLocal1().equals("#FF0000") &&
          equipo.getColorLocal2().equals("#FFFFFF") &&
          equipo.getColorVisitante1().equals("#0000FF") &&
          equipo.getColorVisitante2().equals("#FFFFFF")
        ),
        eq(torneo)
      );
  }

  @Test
  public void alGuardarEquipoSiElTorneoEstaCompletoDebeRedirigirSinInscribirlo()
    throws EquipoExistente, SiglaExistente, SiglaInvalida {
    // Preparación
    Long idTorneo = 1L;

    Torneo torneo = mock(Torneo.class);
    when(torneo.getId()).thenReturn(idTorneo);
    when(servicioTorneoMock.consultarTorneoPorId(idTorneo)).thenReturn(torneo);
    when(torneo.getCantidadDeEquipos()).thenReturn(4);
    when(servicioEquipoTorneoMock.contarEquiposPorTorneo(idTorneo)).thenReturn(4);

    RedirectAttributes redirectAttributes = new RedirectAttributesModelMap();

    // Ejecución
    ModelAndView mav = controladorTorneo.guardarEquipo(
      torneo,
      equipoForm("Los Leones", "BOC"),
      redirectAttributes
    );

    // Validación
    assertThat(mav.getViewName(), equalTo("redirect:/creacion-equipo?idTorneo=1"));

    verify(servicioEquipoTorneoMock, never())
      .crearEquipoEInscribirlo(any(Equipo.class), any(Torneo.class));
  }

  @Test
  public void alListarEquiposAdminDebeMostrarLaVistaYConsultarLosEquipos() {
    Long idTorneo = 1L;
    List<Equipo> equipos = new ArrayList<>();

    when(servicioEquipoMock.obtenerEquipos()).thenReturn(equipos);

    ModelAndView mav = controladorTorneo.listarEquiposAdmin(idTorneo);

    assertThat(mav.getViewName(), equalTo("lista-equipos-admin"));
    assertThat(mav.getModel().get("idTorneo"), equalTo(idTorneo));
    assertThat(mav.getModel().get("equipos"), equalTo(equipos));

    verify(servicioEquipoMock).obtenerEquipos();
  }

  @Test
  public void alMostrarEquiposParaDesvincularDebeMostrarLaVistaYLosEquipos()
    throws FechaLimiteEditarTorneo {
    Long idTorneo = 1L;
    List<EquipoTorneo> equipos = new ArrayList<>();

    RedirectAttributes redirectAttributes = mock(RedirectAttributes.class);

    when(servicioEquipoTorneoMock.obtenerEquiposPorTorneo(idTorneo)).thenReturn(equipos);

    ModelAndView mav = controladorTorneo.mostrarEquiposParaDesvincular(
      idTorneo,
      redirectAttributes
    );

    assertThat(mav.getViewName(), equalTo("desvincularEquipo"));
    assertThat(mav.getModel().get("idTorneo"), equalTo(idTorneo));
    assertThat(mav.getModel().get("equipos"), equalTo(equipos));

    verify(servicioEquipoTorneoMock).obtenerEquiposPorTorneo(idTorneo);
  }

  @Test
  public void alMostrarEquiposParaEditarDebeMostrarLaVistaYLosEquipos()
    throws FechaLimiteEditarTorneo {
    Long idTorneo = 1L;
    List<EquipoTorneo> equipos = new ArrayList<>();

    RedirectAttributes r = mock(RedirectAttributes.class);

    when(servicioEquipoTorneoMock.obtenerEquiposPorTorneo(idTorneo)).thenReturn(equipos);

    ModelAndView mav = controladorTorneo.mostrarEquiposParaEditar(idTorneo, r);

    assertThat(mav.getViewName(), equalTo("editarEquipo"));
    assertThat(mav.getModel().get("idTorneo"), equalTo(idTorneo));
    assertThat(mav.getModel().get("equipos"), equalTo(equipos));

    verify(servicioEquipoTorneoMock).obtenerEquiposPorTorneo(idTorneo);
  }

  @Test
  public void alCrearEquipoAdminDebeMostrarLaVistaDeCreacion() throws Exception {
    Long idTorneo = 1L;
    Torneo torneo = new Torneo();
    torneo.setId(idTorneo);
    torneo.setFechaInicio(LocalDateTime.now().plusDays(5));
    torneo.setCantidadDeEquipos(8); // <-- AGREGAR ESTA LÍNEA
    torneo.setCantidadMinJugadores(11); // <-- AGREGAR ESTA LÍNEA
    torneo.setCantidadMaxJugadores(23); // <-- AGREGAR ESTA LÍNEA

    when(servicioTorneoMock.consultarTorneoPorId(idTorneo)).thenReturn(torneo);
    when(servicioEquipoTorneoMock.contarEquiposPorTorneo(idTorneo)).thenReturn(0);

    RedirectAttributes redirectAttributes = new RedirectAttributesModelMap();

    ModelAndView mav = controladorTorneo.crearEquipoAdmin(idTorneo, redirectAttributes);

    assertThat(mav.getViewName(), equalTo("creacion-equipo-admin"));
  }

  @Test
  public void alCrearEquipoAdminDebeCargarLosDatosDelTorneo() throws Exception {
    // 1. Preparación
    Long idTorneo = 1L;
    Torneo torneo = new Torneo();
    torneo.setId(idTorneo);
    torneo.setCantidadDeEquipos(8);
    torneo.setCantidadMinJugadores(11);
    torneo.setCantidadMaxJugadores(23);
    torneo.setFechaInicio(LocalDateTime.now().plusDays(5)); // Fecha futura indispensable

    when(servicioTorneoMock.consultarTorneoPorId(idTorneo)).thenReturn(torneo);
    when(servicioEquipoTorneoMock.contarEquiposPorTorneo(idTorneo)).thenReturn(2);

    RedirectAttributes redirectAttributes = new RedirectAttributesModelMap();

    // 2. Ejecución
    ModelAndView mav = controladorTorneo.crearEquipoAdmin(idTorneo, redirectAttributes);

    // 3. Validación
    assertEquals("creacion-equipo-admin", mav.getViewName());
    assertEquals(idTorneo, mav.getModel().get("idTorneo"));
    assertEquals(2, mav.getModel().get("cantidadEquiposActuales"));
    assertEquals(8, mav.getModel().get("cantidadEquiposNecesarios"));
    assertEquals(11, mav.getModel().get("minJugadores"));
    assertEquals(23, mav.getModel().get("maxJugadores"));
  }

  @Test
  public void alCargarEquipoExistenteDebeInscribirloYRedirigir() throws Exception {
    Long idEquipo = 5L;
    Long idTorneo = 1L;

    Equipo equipo = new Equipo();
    equipo.setId(idEquipo);
    Torneo torneo = new Torneo();
    torneo.setId(idTorneo);

    CrearEquipoDto dto = new CrearEquipoDto();
    dto.setIdEquipo(idEquipo);
    dto.setIdTorneo(idTorneo);

    when(servicioEquipoMock.consultarEquipoPorId(idEquipo)).thenReturn(equipo);
    when(servicioTorneoMock.consultarTorneoPorId(idTorneo)).thenReturn(torneo);

    RedirectAttributes redirectAttributes = new RedirectAttributesModelMap();

    ModelAndView mav = controladorTorneo.cargarEquipoExistente(dto, redirectAttributes);

    assertEquals("redirect:/creacion-equipo?idTorneo=1", mav.getViewName());

    verify(servicioEquipoTorneoMock).inscribirEquipoExistente(equipo, torneo);
  }

  @Test
  public void alCargarEquipoYaInscriptoDebeMostrarErrorYRedirigir() throws Exception {
    Long idEquipo = 5L;
    Long idTorneo = 1L;

    Equipo equipo = new Equipo();
    equipo.setId(idEquipo);
    Torneo torneo = new Torneo();
    torneo.setId(idTorneo);

    CrearEquipoDto dto = new CrearEquipoDto();
    dto.setIdEquipo(idEquipo);
    dto.setIdTorneo(idTorneo);

    when(servicioEquipoMock.consultarEquipoPorId(idEquipo)).thenReturn(equipo);
    when(servicioTorneoMock.consultarTorneoPorId(idTorneo)).thenReturn(torneo);

    doThrow(new EquipoYaInscripto())
      .when(servicioEquipoTorneoMock)
      .inscribirEquipoExistente(equipo, torneo);

    RedirectAttributes redirectAttributes = new RedirectAttributesModelMap();

    ModelAndView mav = controladorTorneo.cargarEquipoExistente(dto, redirectAttributes);

    assertEquals("redirect:/creacion-equipo?idTorneo=1", mav.getViewName());
    assertEquals(
      "El equipo ya está inscripto en este torneo",
      redirectAttributes.getFlashAttributes().get("error")
    );
  }

  @Test
  public void eliminarTorneoDebeEliminarYRedirigirALobbyAdmin() {
    Long idTorneo = 1L;
    Torneo torneo = new Torneo();
    torneo.setId(idTorneo);
    torneo.setFechaInicio(LocalDateTime.now().plusDays(5));

    when(servicioTorneoMock.consultarTorneoPorId(idTorneo)).thenReturn(torneo);
    RedirectAttributes redirectAttributes = new RedirectAttributesModelMap();

    ModelAndView mav = controladorTorneo.eliminarTorneoAdmin(torneo, redirectAttributes);

    verify(servicioTorneoMock, times(1)).eliminarTorneo(idTorneo);
    assertThat(mav.getViewName(), equalTo("redirect:/lobbyAdmin"));
    assertThat(
      redirectAttributes.getFlashAttributes().get("mensaje").toString(),
      equalTo("Torneo eliminado correctamente.")
    );
  }

  @Test
  public void eliminarTorneoFueraDeFechaLimiteDebeRedirigirConError()
    throws FechaLimiteEditarTorneo {
    Long idTorneo = 1L;

    Torneo torneo = new Torneo();
    torneo.setId(idTorneo);
    torneo.setFechaInicio(LocalDateTime.now().plusHours(10));

    when(servicioTorneoMock.consultarTorneoPorId(idTorneo)).thenReturn(torneo);

    doThrow(
      new FechaLimiteEditarTorneo(
        "La fecha actual es mayor a la permitida para hacer cambios en el torneo."
      )
    )
      .when(validadorTorneoMock)
      .validarModificacion(torneo);

    RedirectAttributes redirectAttributes = new RedirectAttributesModelMap();

    ModelAndView mav = controladorTorneo.eliminarTorneoAdmin(torneo, redirectAttributes);

    verify(servicioTorneoMock, never()).eliminarTorneo(idTorneo);
    assertThat(mav.getViewName(), equalTo("redirect:/administrarTorneoAdmin/1"));
    assertThat(redirectAttributes.getFlashAttributes().get("error"), notNullValue());
  }

  @Test
  public void crearEquipoAdminFueraDeFechaLimiteDebeRedirigirConError() throws Exception {
    Long idTorneo = 1L;

    Torneo torneo = new Torneo();
    torneo.setId(idTorneo);
    torneo.setFechaInicio(LocalDateTime.now().plusHours(5));

    when(servicioTorneoMock.consultarTorneoPorId(idTorneo)).thenReturn(torneo);

    doThrow(
      new FechaLimiteEditarTorneo(
        "La fecha actual es mayor a la permitida para hacer cambios en el torneo."
      )
    )
      .when(validadorTorneoMock)
      .validarModificacion(torneo);

    RedirectAttributes redirectAttributes = new RedirectAttributesModelMap();

    ModelAndView mav = controladorTorneo.crearEquipoAdmin(idTorneo, redirectAttributes);

    assertThat(mav.getViewName(), equalTo("redirect:/administrarTorneoAdmin/1"));
    assertThat(redirectAttributes.getFlashAttributes().get("error"), notNullValue());
  }

  @Test
  public void mostrarEquiposParaEditarFueraDeFechaLimiteDebeRedirigirConError() throws Exception {
    Long idTorneo = 1L;
    Torneo torneo = new Torneo();
    torneo.setId(idTorneo);
    torneo.setFechaInicio(LocalDateTime.now().plusHours(10)); // Vencido plazo

    when(servicioTorneoMock.consultarTorneoPorId(idTorneo)).thenReturn(torneo);

    doThrow(
      new FechaLimiteEditarTorneo(
        "La fecha actual es mayor a la permitida para hacer cambios en el torneo."
      )
    )
      .when(validadorTorneoMock)
      .validarModificacion(torneo);

    RedirectAttributes redirectAttributes = new RedirectAttributesModelMap();

    ModelAndView mav = controladorTorneo.mostrarEquiposParaEditar(idTorneo, redirectAttributes);

    assertThat(mav.getViewName(), equalTo("redirect:/administrarTorneoAdmin/1"));
    assertThat(redirectAttributes.getFlashAttributes().get("error"), notNullValue());
  }

  @Test
  public void mostrarEquiposParaDesvincularFueraDeFechaLimiteDebeRedirigirConError()
    throws Exception {
    Long idTorneo = 1L;
    Torneo torneo = new Torneo();
    torneo.setId(idTorneo);
    torneo.setFechaInicio(LocalDateTime.now().plusHours(10)); // Vencido plazo

    when(servicioTorneoMock.consultarTorneoPorId(idTorneo)).thenReturn(torneo);

    doThrow(
      new FechaLimiteEditarTorneo(
        "La fecha actual es mayor a la permitida para hacer cambios en el torneo."
      )
    )
      .when(validadorTorneoMock)
      .validarModificacion(torneo);

    RedirectAttributes redirectAttributes = new RedirectAttributesModelMap();

    ModelAndView mav = controladorTorneo.mostrarEquiposParaDesvincular(
      idTorneo,
      redirectAttributes
    );

    assertThat(mav.getViewName(), equalTo("redirect:/administrarTorneoAdmin/1"));
    assertThat(redirectAttributes.getFlashAttributes().get("error"), notNullValue());
  }

  @Test
  public void eliminarTorneoCuandoServicioFallaDebeRedirigirConError() {
    Long idTorneo = 1L;
    Torneo torneo = new Torneo();
    torneo.setId(idTorneo);
    torneo.setFechaInicio(LocalDateTime.now().plusDays(5));

    when(servicioTorneoMock.consultarTorneoPorId(idTorneo)).thenReturn(torneo);
    doThrow(new RuntimeException("Fallo en base"))
      .when(servicioTorneoMock)
      .eliminarTorneo(idTorneo);
    RedirectAttributes redirectAttributes = new RedirectAttributesModelMap();

    ModelAndView mav = controladorTorneo.eliminarTorneoAdmin(torneo, redirectAttributes);

    assertThat(mav.getViewName(), equalTo("redirect:/administrarTorneoAdmin/1"));
    assertThat(redirectAttributes.getFlashAttributes().get("error"), notNullValue());
  }

  @Test
  public void testFechaLimiteEditarTorneoConstructores() {
    FechaLimiteEditarTorneo ex1 = new FechaLimiteEditarTorneo();
    FechaLimiteEditarTorneo ex2 = new FechaLimiteEditarTorneo("Mensaje");
    assertThat(ex1, notNullValue());
    assertThat(ex2.getMessage(), equalTo("Mensaje"));
  }

  @Test
  public void alElegirEquipoParaSancionSeDebeMostrarLaVistaConLosEquiposDelTorneo() {
    // Preparación
    Long idTorneo = 5L;
    List<EquipoTorneo> equipos = new ArrayList<>();

    when(servicioEquipoTorneoMock.obtenerEquiposPorTorneo(idTorneo)).thenReturn(equipos);

    // Ejecución
    ModelAndView mav = controladorTorneo.elegirEquipoSancion(idTorneo);

    // Validación
    assertThat(mav.getViewName(), equalTo("elegirEquipoSancion"));
    assertThat(mav.getModel().get("equipos"), equalTo(equipos));
    assertThat(mav.getModel().get("idTorneo"), equalTo(idTorneo));

    verify(servicioEquipoTorneoMock, times(1)).obtenerEquiposPorTorneo(idTorneo);
  }

  @Test
  public void deberiaMostrarErrorSiLaRutaDelEscudoNoEsValida()
    throws EquipoExistente, SiglaExistente, SiglaInvalida {
    // Preparación
    Long idEquipo = 1L;
    Long idTorneo = 2L;

    Equipo equipo = new Equipo();
    equipo.setId(idEquipo);

    when(servicioEquipoMock.consultarEquipoPorId(idEquipo)).thenReturn(equipo);

    // Ejecución
    ModelAndView mav = controladorTorneo.actualizarEquipo(
      actualizarDto(
        equipo,
        torneoConId(idTorneo),
        "Equipo Nuevo",
        "EN",
        "#ffffff",
        "#000000",
        "#ff0000",
        "#00ff00",
        "ruta-invalida/escudo.png"
      )
    );

    // Validación
    assertThat(mav.getViewName(), equalTo("formulario-editar-equipo"));
    assertThat(mav.getModel().get("error"), equalTo("La ruta del escudo no es válida."));

    verify(servicioEquipoMock, never()).actualizarEquipo(any());
  }

  @Test
  public void queAlEliminarTorneoRedireccioneAlLobbyAdmin() {
    Long idTorneo = 1L;
    Torneo torneo = torneoConId(idTorneo);
    RedirectAttributes redirectAttributes = new RedirectAttributesModelMap();

    ModelAndView mav = controladorTorneo.eliminarTorneoAdmin(torneo, redirectAttributes);

    // Verifica que se haya llamado al servicio
    verify(servicioTorneoMock).eliminarTorneo(idTorneo);

    // Verifica la redirección al lobby de admin
    assertThat(mav.getViewName(), equalTo("redirect:/lobbyAdmin")); // O el string constante que uses
  }

  @Test
  public void alGuardarEquipoConSiglaInvalidaDebeMostrarErrorYRedirigir() throws Exception {
    // Preparación
    Long idTorneo = 1L;
    Torneo torneo = new Torneo();
    torneo.setId(idTorneo);
    torneo.setCantidadDeEquipos(10);

    when(servicioTorneoMock.consultarTorneoPorId(idTorneo)).thenReturn(torneo);

    when(servicioEquipoTorneoMock.contarEquiposPorTorneo(idTorneo)).thenReturn(0);

    doThrow(new SiglaInvalida())
      .when(servicioEquipoTorneoMock)
      .crearEquipoEInscribirlo(any(Equipo.class), eq(torneo));

    RedirectAttributes redirectAttributes = new RedirectAttributesModelMap();

    // Ejecución
    ModelAndView mav = controladorTorneo.guardarEquipo(
      torneo,
      equipoForm("Boca Juniors", "BO"),
      redirectAttributes
    );

    // Verificación
    assertEquals("redirect:/creacion-equipo?idTorneo=1", mav.getViewName());

    assertEquals(
      "La cantidad de letras de la sigla no es válida",
      redirectAttributes.getFlashAttributes().get("error")
    );

    verify(servicioEquipoTorneoMock, times(1))
      .crearEquipoEInscribirlo(any(Equipo.class), eq(torneo));
  }

  @Test
  public void alGuardarEquipoAdminConSiglaInvalidaDebeMostrarErrorYRedirigir() throws Exception {
    // Preparación
    Long idTorneo = 1L;
    Torneo torneo = new Torneo();
    torneo.setId(idTorneo);
    torneo.setCantidadDeEquipos(10);

    when(servicioTorneoMock.consultarTorneoPorId(idTorneo)).thenReturn(torneo);
    when(servicioEquipoTorneoMock.contarEquiposPorTorneo(idTorneo)).thenReturn(0);

    doThrow(new SiglaInvalida())
      .when(servicioEquipoTorneoMock)
      .crearEquipoEInscribirlo(any(Equipo.class), eq(torneo));

    RedirectAttributes redirectAttributes = new RedirectAttributesModelMap();

    // Ejecución
    ModelAndView mav = controladorTorneo.guardarEquipoAdmin(
      torneo,
      equipoForm("Boca Juniors", "BO"),
      redirectAttributes
    );

    // Verificación
    assertEquals("redirect:/creacion-equipo-admin?idTorneo=1", mav.getViewName());

    assertEquals(
      "La cantidad de letras de la sigla no es válida",
      redirectAttributes.getFlashAttributes().get("error")
    );

    verify(servicioEquipoTorneoMock, times(1))
      .crearEquipoEInscribirlo(any(Equipo.class), eq(torneo));
  }

  @Test
  public void alActualizarEquipoConSiglaInvalidaDebeMostrarError() throws Exception {
    // Preparación
    Long idEquipo = 1L;
    Long idTorneo = 2L;

    Equipo equipo = new Equipo();
    equipo.setId(idEquipo);

    when(servicioEquipoMock.consultarEquipoPorId(idEquipo)).thenReturn(equipo);

    doThrow(new SiglaInvalida()).when(servicioEquipoMock).actualizarEquipo(equipo);

    // Ejecución
    ModelAndView mav = controladorTorneo.actualizarEquipo(
      actualizarDto(
        equipo,
        torneoConId(idTorneo),
        "Boca Juniors",
        "BO",
        "Azul",
        "Amarillo",
        "Blanco",
        "Negro",
        null
      )
    );

    // Verificación
    assertEquals("formulario-editar-equipo", mav.getViewName());
    assertEquals("La cantidad de letras de la sigla no es válida", mav.getModel().get("error"));
    assertEquals(idTorneo, mav.getModel().get("idTorneo"));

    verify(servicioEquipoMock, times(1)).actualizarEquipo(equipo);
  }
}
