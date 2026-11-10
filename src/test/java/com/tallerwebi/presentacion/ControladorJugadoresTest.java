package com.tallerwebi.presentacion;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.*;

import com.tallerwebi.dominio.*;
import com.tallerwebi.dominio.excepcion.JugadorNoExiste;
import com.tallerwebi.dominio.excepcion.PlantelIncompletoException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

public class ControladorJugadoresTest {

  private ServicioTorneo servicioTorneoMock;
  private ServicioEquipoTorneo servicioEquipoTorneoMock;
  private ServicioEquipo servicioEquipoMock;
  private ServicioJugador servicioJugadorMock;
  private ControladorJugadores controlador;
  private ServicioEstadisticaJugador servicioEstadisticasMock;

  @BeforeEach
  public void init() {
    this.servicioTorneoMock = mock(ServicioTorneo.class);
    this.servicioEquipoTorneoMock = mock(ServicioEquipoTorneo.class);
    this.servicioEquipoMock = mock(ServicioEquipo.class);
    this.servicioJugadorMock = mock(ServicioJugador.class);
    this.servicioEstadisticasMock = mock(ServicioEstadisticaJugador.class);
    this.controlador =
      new ControladorJugadores(
        this.servicioTorneoMock,
        this.servicioEquipoTorneoMock,
        this.servicioEquipoMock,
        this.servicioJugadorMock,
        this.servicioEstadisticasMock
      );
  }

  @SuppressWarnings("unchecked")
  private List<EquipoJugadores> equipos(ModelAndView mav) {
    return (List<EquipoJugadores>) mav.getModel().get("equipos");
  }

  @Test
  public void deberiaRedirigirCuandoElTorneoNoExiste() {
    // preparacion
    when(this.servicioTorneoMock.consultarTorneoPorId(7L)).thenReturn(null);

    // ejecucion
    ModelAndView mav = this.controlador.gestionarJugadores(7L);

    // verificacion
    assertThat(mav.getViewName(), is("redirect:/lobbyAdmin"));
  }

  @Test
  public void deberiaMostrarLosEquiposConSusJugadores() {
    // preparacion
    Torneo torneo = new Torneo();
    torneo.setId(1L);
    when(this.servicioTorneoMock.consultarTorneoPorId(1L)).thenReturn(torneo);

    Equipo equipo = new Equipo();
    equipo.setId(10L);
    EquipoTorneo equipoTorneo = new EquipoTorneo();
    equipoTorneo.setEquipo(equipo);
    when(this.servicioEquipoTorneoMock.obtenerEquiposPorTorneo(1L))
      .thenReturn(List.of(equipoTorneo));

    List<Jugador> jugadores = new ArrayList<>();
    when(this.servicioJugadorMock.obtenerJugadores(10L)).thenReturn(jugadores);

    // ejecucion
    ModelAndView mav = this.controlador.gestionarJugadores(1L);

    // verificacion
    assertThat(mav.getViewName(), is("jugadoresTorneo"));
    assertThat(mav.getModel().get("torneo"), is(torneo));
    assertThat(equipos(mav), hasSize(1));
    assertThat(equipos(mav).get(0).getEquipo(), is(equipo));
    assertThat(equipos(mav).get(0).getJugadores(), is(jugadores));
  }

  @Test
  public void deberiaOmitirLosEquiposTorneoSinEquipoAsociado() {
    // preparacion
    when(this.servicioTorneoMock.consultarTorneoPorId(1L)).thenReturn(new Torneo());
    when(this.servicioEquipoTorneoMock.obtenerEquiposPorTorneo(1L))
      .thenReturn(List.of(new EquipoTorneo()));

    // ejecucion
    ModelAndView mav = this.controlador.gestionarJugadores(1L);

    // verificacion
    assertThat(equipos(mav), hasSize(0));
    verify(this.servicioJugadorMock, never()).obtenerJugadores(any());
  }

  @Test
  public void deberiaAgregarElJugadorYRedirigir() throws JugadorNoExiste {
    // preparacion
    RedirectAttributes redirectAttributesMock = mock(RedirectAttributes.class);
    Torneo torneo = new Torneo();
    torneo.setId(1L);
    torneo.setCantidadMaxJugadores(25);
    inscribirEquipoEnTorneo(1L, 10L, torneo);
    when(this.servicioJugadorMock.obtenerJugadores(10L)).thenReturn(new ArrayList<>());

    // ejecucion
    ModelAndView mav =
      this.controlador.agregarJugador(
          agregarDto(torneo, equipo(10L), "Lionel", "Messi", 10, "Delantero"),
          redirectAttributesMock
        );

    // verificacion
    ArgumentCaptor<Jugador> captor = ArgumentCaptor.forClass(Jugador.class);
    verify(this.servicioJugadorMock).agregarJugador(eq(10L), captor.capture());
    assertThat(captor.getValue().getNombre(), is("Lionel"));
    assertThat(captor.getValue().getApellido(), is("Messi"));
    assertThat(captor.getValue().getDorsal(), is(10));
    assertThat(captor.getValue().getPuesto(), is("Delantero"));
    verify(redirectAttributesMock).addFlashAttribute(eq("ok"), anyString());
    assertThat(mav.getViewName(), is("redirect:/jugadoresTorneo?idTorneo=1"));
  }

  @Test
  public void deberiaAceptarDorsalYPuestoNulosAlAgregarUnJugador() throws JugadorNoExiste {
    // preparacion
    RedirectAttributes redirectAttributesMock = mock(RedirectAttributes.class);
    Torneo torneo = new Torneo();
    torneo.setId(2L);
    torneo.setCantidadMaxJugadores(25);
    inscribirEquipoEnTorneo(2L, 20L, torneo);
    when(this.servicioJugadorMock.obtenerJugadores(20L)).thenReturn(new ArrayList<>());

    // ejecucion
    this.controlador.agregarJugador(
        agregarDto(torneo, equipo(20L), "Ana", "Gomez", null, null),
        redirectAttributesMock
      );

    // verificacion
    verify(this.servicioJugadorMock).agregarJugador(eq(20L), any(Jugador.class));
  }

  @Test
  public void noDeberiaAgregarSiElEquipoNoEstaInscriptoEnElTorneo() throws JugadorNoExiste {
    // preparacion
    RedirectAttributes redirectAttributesMock = mock(RedirectAttributes.class);
    Torneo torneo = new Torneo();
    torneo.setId(1L);
    when(this.servicioTorneoMock.consultarTorneoPorId(1L)).thenReturn(torneo);
    when(this.servicioEquipoTorneoMock.obtenerEquiposPorTorneo(1L)).thenReturn(new ArrayList<>());

    // ejecucion
    ModelAndView mav =
      this.controlador.agregarJugador(
          agregarDto(torneo, equipo(99L), "Juan", "Perez", 5, "Arquero"),
          redirectAttributesMock
        );

    // verificacion
    verify(this.servicioJugadorMock, never()).agregarJugador(any(), any());
    verify(redirectAttributesMock).addFlashAttribute(eq("error"), anyString());
    assertThat(mav.getViewName(), is("redirect:/jugadoresTorneo?idTorneo=1"));
  }

  @Test
  public void noDeberiaAgregarSiElEquipoAlcanzoLaCantidadMaximaDeJugadores()
    throws JugadorNoExiste {
    // preparacion
    RedirectAttributes redirectAttributesMock = mock(RedirectAttributes.class);
    Torneo torneo = new Torneo();
    torneo.setId(1L);
    torneo.setCantidadMaxJugadores(1);
    inscribirEquipoEnTorneo(1L, 10L, torneo);
    when(this.servicioJugadorMock.obtenerJugadores(10L)).thenReturn(List.of(new Jugador()));

    // ejecucion
    ModelAndView mav =
      this.controlador.agregarJugador(
          agregarDto(torneo, equipo(10L), "Juan", "Perez", 5, "Arquero"),
          redirectAttributesMock
        );

    // verificacion
    verify(this.servicioJugadorMock, never()).agregarJugador(any(), any());
    verify(redirectAttributesMock).addFlashAttribute(eq("error"), anyString());
    assertThat(mav.getViewName(), is("redirect:/jugadoresTorneo?idTorneo=1"));
  }

  @Test
  public void noDeberiaAgregarSiYaExisteUnJugadorConElMismoDorsal() throws JugadorNoExiste {
    // preparacion
    RedirectAttributes redirectAttributesMock = mock(RedirectAttributes.class);
    Torneo torneo = new Torneo();
    torneo.setId(1L);
    torneo.setCantidadMaxJugadores(25);
    inscribirEquipoEnTorneo(1L, 10L, torneo);
    Jugador existente = new Jugador();
    existente.setDorsal(10);
    when(this.servicioJugadorMock.obtenerJugadores(10L)).thenReturn(List.of(existente));

    // ejecucion
    ModelAndView mav =
      this.controlador.agregarJugador(
          agregarDto(torneo, equipo(10L), "Juan", "Perez", 10, "Arquero"),
          redirectAttributesMock
        );

    // verificacion
    verify(this.servicioJugadorMock, never()).agregarJugador(any(), any());
    verify(redirectAttributesMock).addFlashAttribute(eq("error"), anyString());
    assertThat(mav.getViewName(), is("redirect:/jugadoresTorneo?idTorneo=1"));
  }

  @Test
  public void deberiaEliminarElJugadorYRedirigir() {
    // preparacion
    RedirectAttributes redirectAttributesMock = mock(RedirectAttributes.class);
    Torneo torneo = new Torneo();
    torneo.setId(1L);
    torneo.setCantidadMinJugadores(1);
    inscribirEquipoEnTorneo(1L, 10L, torneo);
    when(this.servicioJugadorMock.obtenerJugadores(10L))
      .thenReturn(List.of(new Jugador(), new Jugador()));

    // ejecucion
    ModelAndView mav =
      this.controlador.eliminarJugador(torneo, equipo(10L), jugador(99L), redirectAttributesMock);

    // verificacion
    verify(this.servicioJugadorMock).eliminarJugador(eq(10L), eq(99L));
    verify(redirectAttributesMock).addFlashAttribute(eq("ok"), anyString());
    assertThat(mav.getViewName(), is("redirect:/jugadoresTorneo?idTorneo=1"));
  }

  @Test
  public void noDeberiaEliminarSiElEquipoNoEstaInscriptoEnElTorneo() {
    // preparacion
    RedirectAttributes redirectAttributesMock = mock(RedirectAttributes.class);
    Torneo torneo = new Torneo();
    torneo.setId(1L);
    when(this.servicioTorneoMock.consultarTorneoPorId(1L)).thenReturn(torneo);
    when(this.servicioEquipoTorneoMock.obtenerEquiposPorTorneo(1L)).thenReturn(new ArrayList<>());

    // ejecucion
    ModelAndView mav =
      this.controlador.eliminarJugador(torneo, equipo(99L), jugador(5L), redirectAttributesMock);

    // verificacion
    verify(this.servicioJugadorMock, never()).eliminarJugador(any(), any());
    verify(redirectAttributesMock).addFlashAttribute(eq("error"), anyString());
    assertThat(mav.getViewName(), is("redirect:/jugadoresTorneo?idTorneo=1"));
  }

  @Test
  public void noDeberiaEliminarSiElEquipoAlcanzoElMinimoDeJugadores() {
    // preparacion
    RedirectAttributes redirectAttributesMock = mock(RedirectAttributes.class);
    Torneo torneo = new Torneo();
    torneo.setId(1L);
    torneo.setCantidadMinJugadores(2);
    inscribirEquipoEnTorneo(1L, 10L, torneo);
    when(this.servicioJugadorMock.obtenerJugadores(10L))
      .thenReturn(List.of(new Jugador(), new Jugador()));

    // ejecucion
    ModelAndView mav =
      this.controlador.eliminarJugador(torneo, equipo(10L), jugador(99L), redirectAttributesMock);

    // verificacion
    verify(this.servicioJugadorMock, never()).eliminarJugador(any(), any());
    verify(redirectAttributesMock).addFlashAttribute(eq("error"), anyString());
    assertThat(mav.getViewName(), is("redirect:/jugadoresTorneo?idTorneo=1"));
  }

  @Test
  public void deberiaHabilitarElJugadorYRedirigir() {
    // preparacion
    RedirectAttributes redirectAttributesMock = mock(RedirectAttributes.class);
    Torneo torneo = new Torneo();
    torneo.setId(1L);
    inscribirEquipoEnTorneo(1L, 10L, torneo);
    when(this.servicioJugadorMock.obtenerJugadores(10L)).thenReturn(List.of(jugador(99L)));

    // ejecucion
    ModelAndView mav =
      this.controlador.habilitarJugador(
          habilitarDto(torneo, equipo(10L), jugador(99L), true),
          redirectAttributesMock
        );

    // verificacion
    verify(this.servicioJugadorMock).habilitarJugador(eq(99L), eq(true));
    verify(redirectAttributesMock).addFlashAttribute(eq("ok"), anyString());
    assertThat(mav.getViewName(), is("redirect:/jugadoresTorneoAdmin?idTorneo=1"));
  }

  @Test
  public void deberiaDeshabilitarElJugador() {
    // preparacion
    RedirectAttributes redirectAttributesMock = mock(RedirectAttributes.class);
    Torneo torneo = new Torneo();
    torneo.setId(1L);
    inscribirEquipoEnTorneo(1L, 10L, torneo);
    when(this.servicioJugadorMock.obtenerJugadores(10L)).thenReturn(List.of(jugador(99L)));

    // ejecucion
    ModelAndView mav =
      this.controlador.habilitarJugador(
          habilitarDto(torneo, equipo(10L), jugador(99L), false),
          redirectAttributesMock
        );

    // verificacion
    verify(this.servicioJugadorMock).habilitarJugador(eq(99L), eq(false));
    verify(redirectAttributesMock).addFlashAttribute(eq("ok"), anyString());
    assertThat(mav.getViewName(), is("redirect:/jugadoresTorneoAdmin?idTorneo=1"));
  }

  @Test
  public void noDeberiaHabilitarSiElEquipoNoEstaInscriptoEnElTorneo() {
    // preparacion
    RedirectAttributes redirectAttributesMock = mock(RedirectAttributes.class);
    Torneo torneo = new Torneo();
    torneo.setId(1L);
    when(this.servicioTorneoMock.consultarTorneoPorId(1L)).thenReturn(torneo);
    when(this.servicioEquipoTorneoMock.obtenerEquiposPorTorneo(1L)).thenReturn(new ArrayList<>());

    // ejecucion
    ModelAndView mav =
      this.controlador.habilitarJugador(
          habilitarDto(torneo, equipo(99L), jugador(5L), true),
          redirectAttributesMock
        );

    // verificacion
    verify(this.servicioJugadorMock, never()).habilitarJugador(any(), anyBoolean());
    verify(redirectAttributesMock).addFlashAttribute(eq("error"), anyString());
    assertThat(mav.getViewName(), is("redirect:/jugadoresTorneoAdmin?idTorneo=1"));
  }

  @Test
  public void noDeberiaHabilitarSiElJugadorNoPerteneceAlEquipo() {
    // preparacion
    RedirectAttributes redirectAttributesMock = mock(RedirectAttributes.class);
    Torneo torneo = new Torneo();
    torneo.setId(1L);
    inscribirEquipoEnTorneo(1L, 10L, torneo);
    when(this.servicioJugadorMock.obtenerJugadores(10L)).thenReturn(List.of(jugador(1L)));

    // ejecucion
    ModelAndView mav =
      this.controlador.habilitarJugador(
          habilitarDto(torneo, equipo(10L), jugador(99L), true),
          redirectAttributesMock
        );

    // verificacion
    verify(this.servicioJugadorMock, never()).habilitarJugador(any(), anyBoolean());
    verify(redirectAttributesMock).addFlashAttribute(eq("error"), anyString());
    assertThat(mav.getViewName(), is("redirect:/jugadoresTorneoAdmin?idTorneo=1"));
  }

  @Test
  public void noDeberiaHabilitarSiElTorneoNoExiste() {
    // preparacion
    RedirectAttributes redirectAttributesMock = mock(RedirectAttributes.class);
    when(this.servicioTorneoMock.consultarTorneoPorId(7L)).thenReturn(null);

    // ejecucion
    ModelAndView mav =
      this.controlador.habilitarJugador(
          habilitarDto(null, equipo(10L), jugador(99L), true),
          redirectAttributesMock
        );

    // verificacion
    verify(this.servicioJugadorMock, never()).habilitarJugador(any(), anyBoolean());
    assertThat(mav.getViewName(), is("redirect:/lobbyAdmin"));
  }

  @Test
  public void deberiaConfirmarUnPlantelConTodasLasPosiciones() throws PlantelIncompletoException {
    RedirectAttributes redirectAttributesMock = mock(RedirectAttributes.class);
    Torneo torneo = new Torneo();
    torneo.setId(1L);
    inscribirEquipoEnTorneo(1L, 10L, torneo);

    ModelAndView mav =
      this.controlador.confirmarPlantel(torneo, equipo(10L), redirectAttributesMock);

    verify(this.servicioEquipoTorneoMock).confirmarPlantel(10L);
    verify(redirectAttributesMock).addFlashAttribute("ok", "Plantel confirmado correctamente.");
    assertThat(mav.getViewName(), is("redirect:/jugadoresTorneo?idTorneo=1"));
  }

  @Test
  public void noDeberiaConfirmarUnPlantelSiFaltanPosiciones() throws PlantelIncompletoException {
    RedirectAttributes redirectAttributesMock = mock(RedirectAttributes.class);
    Torneo torneo = new Torneo();
    torneo.setId(1L);
    inscribirEquipoEnTorneo(1L, 10L, torneo);
    doThrow(new PlantelIncompletoException(List.of("Defensor", "Mediocampista")))
      .when(this.servicioEquipoTorneoMock)
      .confirmarPlantel(10L);

    ModelAndView mav =
      this.controlador.confirmarPlantel(torneo, equipo(10L), redirectAttributesMock);

    verify(redirectAttributesMock)
      .addFlashAttribute(
        "error",
        "No se puede confirmar el plantel. Falta al menos un jugador en las posiciones: " +
        "Defensor, Mediocampista."
      );
    assertThat(mav.getViewName(), is("redirect:/jugadoresTorneo?idTorneo=1"));
  }

  @Test
  public void noDeberiaConfirmarUnPlantelVacio() throws PlantelIncompletoException {
    RedirectAttributes redirectAttributesMock = mock(RedirectAttributes.class);
    Torneo torneo = new Torneo();
    torneo.setId(1L);
    inscribirEquipoEnTorneo(1L, 10L, torneo);
    doThrow(
      new PlantelIncompletoException(List.of("Arquero", "Defensor", "Mediocampista", "Delantero"))
    )
      .when(this.servicioEquipoTorneoMock)
      .confirmarPlantel(10L);

    ModelAndView mav =
      this.controlador.confirmarPlantel(torneo, equipo(10L), redirectAttributesMock);

    verify(redirectAttributesMock)
      .addFlashAttribute(
        "error",
        "No se puede confirmar el plantel. Falta al menos un jugador en las posiciones: " +
        "Arquero, Defensor, Mediocampista, Delantero."
      );
    assertThat(mav.getViewName(), is("redirect:/jugadoresTorneo?idTorneo=1"));
  }

  private AgregarJugadorDto agregarDto(
    Torneo torneo,
    Equipo equipo,
    String nombre,
    String apellido,
    Integer dorsal,
    String puesto
  ) {
    if (equipo != null) {
      when(this.servicioEquipoMock.consultarEquipoPorId(equipo.getId())).thenReturn(equipo);
    }

    AgregarJugadorDto dto = new AgregarJugadorDto();
    dto.setIdTorneo(torneo == null ? null : torneo.getId());
    dto.setIdEquipo(equipo == null ? null : equipo.getId());
    dto.setNombre(nombre);
    dto.setApellido(apellido);
    dto.setDorsal(dorsal);
    dto.setPuesto(puesto);

    return dto;
  }

  private HabilitarJugadorDto habilitarDto(
    Torneo torneo,
    Equipo equipo,
    Jugador jugador,
    Boolean habilitado
  ) {
    if (equipo != null) {
      when(this.servicioEquipoMock.consultarEquipoPorId(equipo.getId())).thenReturn(equipo);
    }
    if (jugador != null) {
      when(this.servicioJugadorMock.consultarJugador(jugador.getId())).thenReturn(jugador);
    }

    HabilitarJugadorDto dto = new HabilitarJugadorDto();
    dto.setIdTorneo(torneo == null ? null : torneo.getId());
    dto.setIdEquipo(equipo == null ? null : equipo.getId());
    dto.setIdJugador(jugador == null ? null : jugador.getId());
    dto.setHabilitado(habilitado);

    return dto;
  }

  private SancionarJugadorDto sancionarDto(
    Torneo torneo,
    Equipo equipo,
    Jugador jugador,
    Integer fechas
  ) {
    if (equipo != null) {
      when(this.servicioEquipoMock.consultarEquipoPorId(equipo.getId())).thenReturn(equipo);
    }
    if (jugador != null) {
      when(this.servicioJugadorMock.consultarJugador(jugador.getId())).thenReturn(jugador);
    }

    SancionarJugadorDto dto = new SancionarJugadorDto();
    dto.setIdTorneo(torneo == null ? null : torneo.getId());
    dto.setIdEquipo(equipo == null ? null : equipo.getId());
    dto.setIdJugador(jugador == null ? null : jugador.getId());
    dto.setFechas(fechas);

    return dto;
  }

  private Equipo equipo(Long id) {
    Equipo equipo = new Equipo();
    equipo.setId(id);

    return equipo;
  }

  private Jugador jugador(Long id) {
    Jugador jugador = new Jugador();
    jugador.setId(id);

    return jugador;
  }

  private void inscribirEquipoEnTorneo(Long idTorneo, Long idEquipo, Torneo torneo) {
    when(this.servicioTorneoMock.consultarTorneoPorId(idTorneo)).thenReturn(torneo);
    Equipo equipo = new Equipo();
    equipo.setId(idEquipo);
    EquipoTorneo equipoTorneo = new EquipoTorneo();
    equipoTorneo.setEquipo(equipo);
    when(this.servicioEquipoTorneoMock.obtenerEquiposPorTorneo(idTorneo))
      .thenReturn(List.of(equipoTorneo));
  }

  @Test
  public void deberiaAceptarUnaFechaDeSancion() {
    //preparcion
    RedirectAttributes redirectAttributesMock = mock(RedirectAttributes.class);
    Torneo torneo = new Torneo();
    torneo.setId(1L);

    inscribirEquipoEnTorneo(1L, 10L, torneo);
    when(this.servicioJugadorMock.obtenerJugadores(10L)).thenReturn(List.of(jugador(99L)));

    //ejecucion
    ModelAndView mav =
      this.controlador.sancionarJugador(
          sancionarDto(torneo, equipo(10L), jugador(99L), 1),
          redirectAttributesMock
        );

    //verificacion
    verify(this.servicioJugadorMock).sancionarJugadorEnTorneo(99L, 1L, 1);
    verify(redirectAttributesMock).addFlashAttribute("ok", "Jugador sancionado por 1 fecha(s).");
    assertThat(mav.getViewName(), is("redirect:/jugadoresEquipoSancion?idTorneo=1&idEquipo=10"));
  }

  @Test
  public void noDeberiaSancionarSiElEquipoNoTieneJugadores() {
    RedirectAttributes redirectAttributesMock = mock(RedirectAttributes.class);
    Torneo torneo = new Torneo();
    torneo.setId(1L);

    inscribirEquipoEnTorneo(1L, 10L, torneo);
    when(this.servicioJugadorMock.obtenerJugadores(10L)).thenReturn(new ArrayList<>());

    ModelAndView mav =
      this.controlador.sancionarJugador(
          sancionarDto(torneo, equipo(10L), jugador(99L), 2),
          redirectAttributesMock
        );

    verify(this.servicioJugadorMock, never()).sancionarJugador(any(), any());
    verify(redirectAttributesMock).addFlashAttribute(eq("error"), anyString());
    assertThat(mav.getViewName(), is("redirect:/jugadoresEquipoSancion?idTorneo=1&idEquipo=10"));
  }

  @Test
  public void deberiaMostrarJugadoresDelEquipoParaSancionar() {
    // preparacion
    Long idTorneo = 1L;
    Long idEquipo = 5L;

    Torneo torneo = new Torneo();
    torneo.setId(idTorneo);

    Equipo equipo = new Equipo();
    equipo.setId(idEquipo);

    EquipoTorneo equipoTorneo = new EquipoTorneo();
    equipoTorneo.setEquipo(equipo);

    List<EquipoTorneo> equiposTorneo = new ArrayList<>();
    equiposTorneo.add(equipoTorneo);

    List<Jugador> jugadores = new ArrayList<>();
    Jugador jugador = new Jugador();

    when(this.servicioTorneoMock.consultarTorneoPorId(idTorneo)).thenReturn(torneo);
    when(this.servicioEquipoTorneoMock.obtenerEquiposPorTorneo(idTorneo)).thenReturn(equiposTorneo);
    when(this.servicioJugadorMock.obtenerJugadores(idEquipo)).thenReturn(jugadores);

    // ejecucion
    ModelAndView mav = this.controlador.mostrarJugadoresEquipoSancion(idTorneo, idEquipo);

    // verificacion
    assertThat(mav.getViewName(), is("jugadoresEquipoSancion"));
    assertThat(mav.getModel().get("equipo"), is(equipo));
    assertThat(mav.getModel().get("jugadores"), is(jugadores));
    assertThat(mav.getModel().get("idTorneo"), is(idTorneo));

    verify(this.servicioJugadorMock).obtenerJugadores(idEquipo);
  }

  @Test
  public void noDeberiaMostrarJugadoresSiElEquipoNoEstaInscriptoEnElTorneo() {
    // preparacion
    Long idTorneo = 1L;
    Long idEquipo = 99L;

    Torneo torneo = new Torneo();
    torneo.setId(idTorneo);

    when(this.servicioTorneoMock.consultarTorneoPorId(idTorneo)).thenReturn(torneo);
    when(this.servicioEquipoTorneoMock.obtenerEquiposPorTorneo(idTorneo))
      .thenReturn(new ArrayList<>());

    // ejecucion
    ModelAndView mav = this.controlador.mostrarJugadoresEquipoSancion(idTorneo, idEquipo);

    // verificacion
    verify(this.servicioJugadorMock, never()).obtenerJugadores(any());
    assertThat(mav.getViewName(), is("redirect:/elegirEquipoSancion?idTorneo=1"));
  }

  @Test
  public void deberiaConfirmarElPlantelAlEditarYRedirigir() throws PlantelIncompletoException {
    // Preparación
    RedirectAttributes redirectAttributesMock = mock(RedirectAttributes.class);
    Torneo torneo = new Torneo();
    torneo.setId(1L);
    inscribirEquipoEnTorneo(1L, 10L, torneo);

    // Ejecución
    ModelAndView mav =
      this.controlador.confirmarPlantelEditar(torneo, equipo(10L), redirectAttributesMock);

    // Verificación
    verify(this.servicioEquipoTorneoMock).confirmarPlantel(10L);
    verify(redirectAttributesMock).addFlashAttribute("ok", "Plantel confirmado correctamente.");
    assertThat(mav.getViewName(), is("redirect:/jugadoresTorneoEditar?idTorneo=1&idEquipo=10"));
  }

  @Test
  public void noDeberiaConfirmarElPlantelAlEditarSiFaltanPosiciones()
    throws PlantelIncompletoException {
    // Preparación
    RedirectAttributes redirectAttributesMock = mock(RedirectAttributes.class);
    Torneo torneo = new Torneo();
    torneo.setId(1L);
    inscribirEquipoEnTorneo(1L, 10L, torneo);

    doThrow(new PlantelIncompletoException(List.of("Defensor", "Mediocampista")))
      .when(this.servicioEquipoTorneoMock)
      .confirmarPlantel(10L);

    // Ejecución
    ModelAndView mav =
      this.controlador.confirmarPlantelEditar(torneo, equipo(10L), redirectAttributesMock);

    // Verificación
    verify(this.servicioEquipoTorneoMock).confirmarPlantel(10L);
    verify(redirectAttributesMock)
      .addFlashAttribute(
        "error",
        "No se puede confirmar el plantel. Falta al menos un jugador en las posiciones: " +
        "Defensor, Mediocampista."
      );
    assertThat(mav.getViewName(), is("redirect:/jugadoresTorneoEditar?idTorneo=1&idEquipo=10"));
  }

  @Test
  public void deberiaHabilitarElJugadorAlEditarYRedirigir() {
    // Preparación
    RedirectAttributes redirectAttributesMock = mock(RedirectAttributes.class);
    Torneo torneo = new Torneo();
    torneo.setId(1L);
    inscribirEquipoEnTorneo(1L, 10L, torneo);
    when(this.servicioJugadorMock.obtenerJugadores(10L)).thenReturn(List.of(jugador(99L)));

    // Ejecución
    ModelAndView mav =
      this.controlador.habilitarJugadorEditar(
          habilitarDto(torneo, equipo(10L), jugador(99L), true),
          redirectAttributesMock
        );

    // Verificación
    verify(this.servicioJugadorMock).habilitarJugador(99L, true);
    verify(redirectAttributesMock).addFlashAttribute(eq("ok"), anyString());
    assertThat(mav.getViewName(), is("redirect:/jugadoresTorneoEditar?idTorneo=1&idEquipo=10"));
  }

  @Test
  public void deberiaDeshabilitarElJugadorAlEditar() {
    // Preparación
    RedirectAttributes redirectAttributesMock = mock(RedirectAttributes.class);
    Torneo torneo = new Torneo();
    torneo.setId(1L);
    inscribirEquipoEnTorneo(1L, 10L, torneo);
    when(this.servicioJugadorMock.obtenerJugadores(10L)).thenReturn(List.of(jugador(99L)));

    // Ejecución
    ModelAndView mav =
      this.controlador.habilitarJugadorEditar(
          habilitarDto(torneo, equipo(10L), jugador(99L), false),
          redirectAttributesMock
        );

    // Verificación
    verify(this.servicioJugadorMock).habilitarJugador(99L, false);
    verify(redirectAttributesMock).addFlashAttribute(eq("ok"), anyString());
    assertThat(mav.getViewName(), is("redirect:/jugadoresTorneoEditar?idTorneo=1&idEquipo=10"));
  }

  @Test
  public void noDeberiaHabilitarAlEditarSiElEquipoNoEstaInscriptoEnElTorneo() {
    // Preparación
    RedirectAttributes redirectAttributesMock = mock(RedirectAttributes.class);
    Torneo torneo = new Torneo();
    torneo.setId(1L);
    when(this.servicioTorneoMock.consultarTorneoPorId(1L)).thenReturn(torneo);
    when(this.servicioEquipoTorneoMock.obtenerEquiposPorTorneo(1L)).thenReturn(new ArrayList<>());

    // Ejecución
    ModelAndView mav =
      this.controlador.habilitarJugadorEditar(
          habilitarDto(torneo, equipo(99L), jugador(5L), true),
          redirectAttributesMock
        );

    // Verificación
    verify(this.servicioJugadorMock, never()).habilitarJugador(any(), anyBoolean());
    verify(redirectAttributesMock).addFlashAttribute(eq("error"), anyString());
    assertThat(mav.getViewName(), is("redirect:/jugadoresTorneoEditar?idTorneo=1&idEquipo=99"));
  }

  @Test
  public void noDeberiaHabilitarAlEditarSiElJugadorNoPerteneceAlEquipo() {
    // Preparación
    RedirectAttributes redirectAttributesMock = mock(RedirectAttributes.class);
    Torneo torneo = new Torneo();
    torneo.setId(1L);
    inscribirEquipoEnTorneo(1L, 10L, torneo);
    when(this.servicioJugadorMock.obtenerJugadores(10L)).thenReturn(List.of(jugador(1L)));

    // Ejecución
    ModelAndView mav =
      this.controlador.habilitarJugadorEditar(
          habilitarDto(torneo, equipo(10L), jugador(99L), true),
          redirectAttributesMock
        );

    // Verificación
    verify(this.servicioJugadorMock, never()).habilitarJugador(any(), anyBoolean());
    verify(redirectAttributesMock).addFlashAttribute(eq("error"), anyString());
    assertThat(mav.getViewName(), is("redirect:/jugadoresTorneoEditar?idTorneo=1&idEquipo=10"));
  }

  @Test
  public void noDeberiaHabilitarAlEditarSiElTorneoNoExiste() {
    // Preparación
    RedirectAttributes redirectAttributesMock = mock(RedirectAttributes.class);
    when(this.servicioTorneoMock.consultarTorneoPorId(7L)).thenReturn(null);

    // Ejecución
    ModelAndView mav =
      this.controlador.habilitarJugadorEditar(
          habilitarDto(null, equipo(10L), jugador(99L), true),
          redirectAttributesMock
        );

    // Verificación
    verify(this.servicioJugadorMock, never()).habilitarJugador(any(), anyBoolean());
    assertThat(mav.getViewName(), is("redirect:/lobbyAdmin"));
  }

  @Test
  public void noDeberiaEliminarElJugadorEditarSiElEquipoNoEstaInscriptoEnElTorneo() {
    // Preparación
    RedirectAttributes redirectAttributesMock = mock(RedirectAttributes.class);
    Torneo torneo = new Torneo();
    torneo.setId(1L);
    when(this.servicioTorneoMock.consultarTorneoPorId(1L)).thenReturn(torneo);
    when(this.servicioEquipoTorneoMock.obtenerEquiposPorTorneo(1L)).thenReturn(new ArrayList<>());

    // Ejecución
    ModelAndView mav =
      this.controlador.eliminarJugadorEditar(
          torneo,
          equipo(10L),
          jugador(99L),
          redirectAttributesMock
        );

    // Verificación
    verify(this.servicioJugadorMock, never()).eliminarJugador(any(), any());
    verify(redirectAttributesMock).addFlashAttribute(eq("error"), anyString());
    assertThat(mav.getViewName(), is("redirect:/jugadoresTorneoEditar?idTorneo=1&idEquipo=10"));
  }

  @Test
  public void noDeberiaMostrarJugadoresSiElTorneoNoExiste() {
    // Preparación
    when(this.servicioTorneoMock.consultarTorneoPorId(1L)).thenReturn(null);

    // Ejecución
    ModelAndView mav = this.controlador.mostrarJugadoresEquipoSancion(1L, 10L);

    // Verificación
    assertThat(mav.getViewName(), is("redirect:/lobbyAdmin"));
    verify(this.servicioJugadorMock, never()).obtenerJugadores(any());
    verify(this.servicioJugadorMock, never()).listarInscripcionesPorEquipoYTorneo(any(), any());
  }

  @Test
  public void deberiaCargarLasSancionesPendientesDeCadaJugador() {
    // Preparación
    Torneo torneo = new Torneo();
    torneo.setId(1L);

    Equipo equipo = equipo(10L);
    EquipoTorneo equipoTorneo = new EquipoTorneo();
    equipoTorneo.setEquipo(equipo);

    Jugador jugador1 = jugador(99L);
    Jugador jugador2 = jugador(100L);

    JugadorEquipoTorneo inscripcion1 = new JugadorEquipoTorneo();
    inscripcion1.setJugador(jugador1);
    inscripcion1.setFechasSuspensionPendientes(2);

    JugadorEquipoTorneo inscripcion2 = new JugadorEquipoTorneo();
    inscripcion2.setJugador(jugador2);
    inscripcion2.setFechasSuspensionPendientes(1);

    when(this.servicioTorneoMock.consultarTorneoPorId(1L)).thenReturn(torneo);
    when(this.servicioEquipoTorneoMock.obtenerEquiposPorTorneo(1L))
      .thenReturn(List.of(equipoTorneo));
    when(this.servicioJugadorMock.obtenerJugadores(10L)).thenReturn(new ArrayList<>());
    when(this.servicioJugadorMock.listarInscripcionesPorEquipoYTorneo(10L, 1L))
      .thenReturn(List.of(inscripcion1, inscripcion2));

    // Ejecución
    ModelAndView mav = this.controlador.mostrarJugadoresEquipoSancion(1L, 10L);

    // Verificación
    @SuppressWarnings("unchecked")
    Map<Long, Integer> sanciones = (Map<Long, Integer>) mav.getModel().get("sancionesPorJugador");

    assertThat(sanciones.get(99L), is(2));
    assertThat(sanciones.get(100L), is(1));
    assertThat(sanciones.size(), is(2));
  }

  @Test
  public void deberiaIgnorarInscripcionesSinJugadorAsociadoAlCargarSanciones() {
    // Preparación
    Torneo torneo = new Torneo();
    torneo.setId(1L);

    Equipo equipo = equipo(10L);
    EquipoTorneo equipoTorneo = new EquipoTorneo();
    equipoTorneo.setEquipo(equipo);

    JugadorEquipoTorneo inscripcionSinJugador = new JugadorEquipoTorneo();
    inscripcionSinJugador.setFechasSuspensionPendientes(3);

    when(this.servicioTorneoMock.consultarTorneoPorId(1L)).thenReturn(torneo);
    when(this.servicioEquipoTorneoMock.obtenerEquiposPorTorneo(1L))
      .thenReturn(List.of(equipoTorneo));
    when(this.servicioJugadorMock.obtenerJugadores(10L)).thenReturn(new ArrayList<>());
    when(this.servicioJugadorMock.listarInscripcionesPorEquipoYTorneo(10L, 1L))
      .thenReturn(List.of(inscripcionSinJugador));

    // Ejecución
    ModelAndView mav = this.controlador.mostrarJugadoresEquipoSancion(1L, 10L);

    // Verificación
    @SuppressWarnings("unchecked")
    Map<Long, Integer> sanciones = (Map<Long, Integer>) mav.getModel().get("sancionesPorJugador");

    assertThat(sanciones.isEmpty(), is(true));
  }
}
