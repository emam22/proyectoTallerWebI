package com.tallerwebi.presentacion;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.*;

import com.tallerwebi.dominio.Equipo;
import com.tallerwebi.dominio.EquipoTorneo;
import com.tallerwebi.dominio.Jugador;
import com.tallerwebi.dominio.ServicioEquipoTorneo;
import com.tallerwebi.dominio.ServicioJugador;
import com.tallerwebi.dominio.ServicioTorneo;
import com.tallerwebi.dominio.Torneo;
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

  @BeforeEach
  public void init() {
    this.servicioTorneoMock = mock(ServicioTorneo.class);
    this.servicioEquipoTorneoMock = mock(ServicioEquipoTorneo.class);
    this.servicioJugadorMock = mock(ServicioJugador.class);
    this.controlador =
      new ControladorJugadores(
        this.servicioTorneoMock,
        this.servicioEquipoTorneoMock,
        this.servicioJugadorMock
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
  public void deberiaAgregarElJugadorYRedirigir() {
    // preparacion
    RedirectAttributes redirectAttributesMock = mock(RedirectAttributes.class);

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
  public void deberiaAceptarDorsalYPuestoNulosAlAgregarUnJugador() {
    // preparacion
    RedirectAttributes redirectAttributesMock = mock(RedirectAttributes.class);

    // ejecucion
    this.controlador.agregarJugador(2L, 20L, "Ana", "Gomez", null, null, redirectAttributesMock);

    // verificacion
    verify(this.servicioJugadorMock).agregarJugador(eq(20L), any(Jugador.class));
  }
}
