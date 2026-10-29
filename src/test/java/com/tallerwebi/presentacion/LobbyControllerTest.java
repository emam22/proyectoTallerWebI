package com.tallerwebi.presentacion;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.equalToIgnoringCase;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Mockito.*;

import com.tallerwebi.dominio.*;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.servlet.ModelAndView;

public class LobbyControllerTest {

  private ServicioEquipoTorneo servicioEquipoTorneoMock;
  private ServicioTorneo servicioTorneoMock;
  private ServicioEstadisticaJugador servicioEstadisticasMock;
  private LobbyController lobbyController;

  @BeforeEach
  public void init() {
    servicioEquipoTorneoMock = mock(ServicioEquipoTorneo.class);
    servicioTorneoMock = mock(ServicioTorneo.class);
    servicioEstadisticasMock = mock(ServicioEstadisticaJugador.class);

    lobbyController =
      new LobbyController(servicioEquipoTorneoMock, servicioTorneoMock, servicioEstadisticasMock);
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
}
