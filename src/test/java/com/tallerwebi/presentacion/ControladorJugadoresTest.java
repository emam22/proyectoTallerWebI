package com.tallerwebi.presentacion;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.*;

import com.tallerwebi.dominio.*;
import com.tallerwebi.dominio.excepcion.JugadorNoExiste;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

public class ControladorJugadoresTest {

  private ServicioTorneo servicioTorneoMock;
  private ServicioEquipoTorneo servicioEquipoTorneoMock;
  private ServicioJugador servicioJugadorMock;
  private ControladorJugadores controlador;
  private ServicioEstadisticaJugador servicioEstadisticasMock;

  @BeforeEach
  public void init() {
    this.servicioTorneoMock = mock(ServicioTorneo.class);
    this.servicioEquipoTorneoMock = mock(ServicioEquipoTorneo.class);
    this.servicioJugadorMock = mock(ServicioJugador.class);
    this.servicioEstadisticasMock = mock(ServicioEstadisticaJugador.class);
    this.controlador =
      new ControladorJugadores(
        this.servicioTorneoMock,
        this.servicioEquipoTorneoMock,
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
          1L,
          10L,
          "Lionel",
          "Messi",
          10,
          "Delantero",
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
    this.controlador.agregarJugador(2L, 20L, "Ana", "Gomez", null, null, redirectAttributesMock);

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
          1L,
          99L,
          "Juan",
          "Perez",
          5,
          "Arquero",
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
          1L,
          10L,
          "Juan",
          "Perez",
          5,
          "Arquero",
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
          1L,
          10L,
          "Juan",
          "Perez",
          10,
          "Arquero",
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
    ModelAndView mav = this.controlador.eliminarJugador(1L, 10L, 99L, redirectAttributesMock);

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
    ModelAndView mav = this.controlador.eliminarJugador(1L, 99L, 5L, redirectAttributesMock);

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
    ModelAndView mav = this.controlador.eliminarJugador(1L, 10L, 99L, redirectAttributesMock);

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
      this.controlador.habilitarJugador(1L, 10L, 99L, true, redirectAttributesMock);

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
      this.controlador.habilitarJugador(1L, 10L, 99L, false, redirectAttributesMock);

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
    ModelAndView mav = this.controlador.habilitarJugador(1L, 99L, 5L, true, redirectAttributesMock);

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
      this.controlador.habilitarJugador(1L, 10L, 99L, true, redirectAttributesMock);

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
      this.controlador.habilitarJugador(7L, 10L, 99L, true, redirectAttributesMock);

    // verificacion
    verify(this.servicioJugadorMock, never()).habilitarJugador(any(), anyBoolean());
    assertThat(mav.getViewName(), is("redirect:/lobbyAdmin"));
  }

  @Test
  public void deberiaConfirmarUnPlantelConTodasLasPosiciones() {
    RedirectAttributes redirectAttributesMock = mock(RedirectAttributes.class);
    Torneo torneo = new Torneo();
    torneo.setId(1L);
    inscribirEquipoEnTorneo(1L, 10L, torneo);
    when(this.servicioJugadorMock.obtenerJugadores(10L))
      .thenReturn(
        List.of(
          jugadorEnPosicion("Arquero"),
          jugadorEnPosicion("Defensor"),
          jugadorEnPosicion("Mediocampista"),
          jugadorEnPosicion("Delantero")
        )
      );

    ModelAndView mav = this.controlador.confirmarPlantel(1L, 10L, redirectAttributesMock);

    verify(redirectAttributesMock).addFlashAttribute("ok", "Plantel confirmado correctamente.");
    assertThat(mav.getViewName(), is("redirect:/jugadoresTorneo?idTorneo=1"));
  }

  @Test
  public void noDeberiaConfirmarUnPlantelSiFaltanPosiciones() {
    RedirectAttributes redirectAttributesMock = mock(RedirectAttributes.class);
    Torneo torneo = new Torneo();
    torneo.setId(1L);
    inscribirEquipoEnTorneo(1L, 10L, torneo);
    when(this.servicioJugadorMock.obtenerJugadores(10L))
      .thenReturn(List.of(jugadorEnPosicion("Arquero"), jugadorEnPosicion("Delantero")));

    ModelAndView mav = this.controlador.confirmarPlantel(1L, 10L, redirectAttributesMock);

    verify(redirectAttributesMock)
      .addFlashAttribute(
        "error",
        "No se puede confirmar el plantel. Falta al menos un jugador en las posiciones: " +
        "Defensor, Mediocampista."
      );
    assertThat(mav.getViewName(), is("redirect:/jugadoresTorneo?idTorneo=1"));
  }

  @Test
  public void noDeberiaConfirmarUnPlantelVacio() {
    RedirectAttributes redirectAttributesMock = mock(RedirectAttributes.class);
    Torneo torneo = new Torneo();
    torneo.setId(1L);
    inscribirEquipoEnTorneo(1L, 10L, torneo);
    when(this.servicioJugadorMock.obtenerJugadores(10L)).thenReturn(new ArrayList<>());

    ModelAndView mav = this.controlador.confirmarPlantel(1L, 10L, redirectAttributesMock);

    verify(redirectAttributesMock)
      .addFlashAttribute(
        "error",
        "No se puede confirmar el plantel. Falta al menos un jugador en las posiciones: " +
        "Arquero, Defensor, Mediocampista, Delantero."
      );
    assertThat(mav.getViewName(), is("redirect:/jugadoresTorneo?idTorneo=1"));
  }

  private Jugador jugadorEnPosicion(String posicion) {
    Jugador jugador = new Jugador();
    jugador.setPuesto(posicion);

    return jugador;
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
    ModelAndView mav = this.controlador.sancionarJugador(1L, 10L, 99L, 1, redirectAttributesMock);

    //verificacion
    verify(this.servicioJugadorMock).sancionarJugador(99L, 1);
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

    ModelAndView mav = this.controlador.sancionarJugador(1L, 10L, 99L, 2, redirectAttributesMock);

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
}
