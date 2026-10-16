package com.tallerwebi.presentacion;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.equalToIgnoringCase;
import static org.mockito.Mockito.*;

import com.tallerwebi.dominio.*;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.servlet.ModelAndView;

public class ControladorTablaEquipoTest {

  private ServicioTorneo servicioTorneoMock;
  private ServicioEquipoTorneo servicioEquipoTorneoMock;
  private ControladorTablaEquipos controladorTablaEquipos;

  @BeforeEach
  public void init() {
    servicioTorneoMock = mock(ServicioTorneo.class);
    servicioEquipoTorneoMock = mock(ServicioEquipoTorneo.class);
    controladorTablaEquipos =
      new ControladorTablaEquipos(servicioEquipoTorneoMock, servicioTorneoMock);
  }

  @Test
  public void alIrATablaEquiposSeDebeMostrarLaVistaConLosDatos() {
    // Preparación
    Long idTorneo = 1L;
    Torneo torneoMock = mock(Torneo.class);
    List<EquipoTorneo> tablaMock = List.of(mock(EquipoTorneo.class));

    when(servicioTorneoMock.consultarTorneoPorId(idTorneo)).thenReturn(torneoMock);

    when(servicioEquipoTorneoMock.obtenerEstadisticas(idTorneo)).thenReturn(tablaMock);

    // Ejecución
    ModelAndView mav = controladorTablaEquipos.irATablaEquipos(idTorneo);

    // Validación
    assertThat(mav.getViewName(), equalToIgnoringCase("tablaEquipos"));

    assertThat(mav.getModel().get("torneo"), equalTo(torneoMock));

    assertThat(mav.getModel().get("tablaPosiciones"), equalTo(tablaMock));

    verify(servicioTorneoMock, times(1)).consultarTorneoPorId(idTorneo);

    verify(servicioEquipoTorneoMock, times(1)).obtenerEstadisticas(idTorneo);
  }

  @Test
  public void alIrATablaEquiposSinIdSeDebeMostrarLaVista() {
    // Preparación
    when(servicioTorneoMock.consultarTorneoPorId(null)).thenReturn(null);

    when(servicioEquipoTorneoMock.obtenerEstadisticas(null)).thenReturn(List.of());

    // Ejecución
    ModelAndView mav = controladorTablaEquipos.irATablaEquipos(null);

    // Validación
    assertThat(mav.getViewName(), equalToIgnoringCase("tablaEquipos"));

    assertThat(mav.getModel().get("torneo"), equalTo(null));

    assertThat(mav.getModel().get("tablaPosiciones"), equalTo(List.of()));

    verify(servicioTorneoMock, times(1)).consultarTorneoPorId(null);

    verify(servicioEquipoTorneoMock, times(1)).obtenerEstadisticas(null);
  }
}
