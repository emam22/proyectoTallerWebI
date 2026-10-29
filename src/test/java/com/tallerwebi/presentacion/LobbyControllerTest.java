package com.tallerwebi.presentacion;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.equalToIgnoringCase;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Mockito.*;

import com.tallerwebi.dominio.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.servlet.ModelAndView;

public class LobbyControllerTest {

  private ServicioEquipoTorneo servicioEquipoTorneoMock;
  private ServicioTorneo servicioTorneoMock;
  private ServicioEstadisticaJugador servicioEstadisticasMock;
  private ServicioEquipo servicioEquipoMock;

  private LobbyController lobbyController;

  @BeforeEach
  public void init() {
    servicioEquipoTorneoMock = mock(ServicioEquipoTorneo.class);
    servicioTorneoMock = mock(ServicioTorneo.class);
    servicioEstadisticasMock = mock(ServicioEstadisticaJugador.class);
    servicioEquipoMock = mock(ServicioEquipo.class);

    lobbyController =
      new LobbyController(
        servicioEquipoTorneoMock,
        servicioTorneoMock,
        servicioEstadisticasMock,
        servicioEquipoMock
      );
  }

  @Test
  public void alVerLasEstadisticasDeJugadoresDebeExponerElGoleador() {
    // Preparación
    EstadisticaJugador goleador = new EstadisticaJugador();
    goleador.setGoles(5);

    EstadisticaJugador otro = new EstadisticaJugador();
    otro.setGoles(1);

    List<EstadisticaJugador> estadisticas = new ArrayList<>();
    estadisticas.add(goleador);
    estadisticas.add(otro);

    when(servicioEstadisticasMock.obtenerEstadisticas()).thenReturn(estadisticas);

    // Ejecución
    ModelAndView mav = lobbyController.verEstadisticasJugadores(1L);

    // Validación
    assertThat(mav.getViewName(), equalToIgnoringCase("estadisticasDeJugadores"));

    assertThat(mav.getModel().get("estadisticasDeJugadores"), equalTo(estadisticas));

    assertThat(mav.getModel().get("goleador"), equalTo(goleador));

    assertThat(mav.getModel().get("idTorneoActual"), equalTo(1L));

    verify(servicioEstadisticasMock, times(1)).obtenerEstadisticas();
  }

  @Test
  public void alVerLasEstadisticasDeJugadoresSinRegistrosNoDebeExponerGoleador() {
    // Preparación
    when(servicioEstadisticasMock.obtenerEstadisticas()).thenReturn(new ArrayList<>());

    // Ejecución
    ModelAndView mav = lobbyController.verEstadisticasJugadores(null);

    // Validación
    assertThat(mav.getViewName(), equalToIgnoringCase("estadisticasDeJugadores"));

    assertThat(mav.getModel().get("goleador"), nullValue());

    assertThat(mav.getModel().get("idTorneoActual"), equalTo(1L));
  }

  @Test
  public void queIrAlLobbyCargueEstadisticasYEquipos() {
    Long idTorneo = 1L;
    Torneo torneo = new Torneo();
    torneo.setId(idTorneo);

    List<EstadisticaJugador> stats = new ArrayList<>();
    EstadisticaJugador stat = new EstadisticaJugador();
    Jugador jugador = new Jugador();
    jugador.setId(5L);
    stat.setJugador(jugador);
    stats.add(stat);

    Equipo equipo = new Equipo();
    equipo.setId(10L);
    equipo.setEscudo("escudo.png");

    when(servicioTorneoMock.consultarTorneoPorId(idTorneo)).thenReturn(torneo);
    when(servicioEstadisticasMock.obtenerEstadisticas()).thenReturn(stats);
    when(servicioEquipoMock.consultarEquipoJugador(5L)).thenReturn(equipo);

    ModelAndView mav = lobbyController.irALobby(idTorneo);

    assertThat(mav.getViewName(), equalTo("lobby"));
    assertThat(mav.getModel().get("estadisticasJugadores"), notNullValue());

    @SuppressWarnings("unchecked")
    Map<Long, Equipo> mapa = (Map<Long, Equipo>) mav.getModel().get("equiposPorJugador");
    assertThat(mapa, notNullValue());
    assertThat(mapa.get(5L), equalTo(equipo));
  }

  @Test
  public void queIrAlLobbyFuncioneConListaDeEstadisticasVacia() {
    Long idTorneo = 1L;
    when(servicioTorneoMock.consultarTorneoPorId(idTorneo)).thenReturn(new Torneo());
    when(servicioEstadisticasMock.obtenerEstadisticas()).thenReturn(new ArrayList<>());

    ModelAndView mav = lobbyController.irALobby(idTorneo);

    assertThat(mav.getViewName(), equalTo("lobby"));
  }
}
