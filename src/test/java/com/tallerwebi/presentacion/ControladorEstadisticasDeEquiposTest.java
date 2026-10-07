package com.tallerwebi.presentacion;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.equalToIgnoringCase;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.tallerwebi.dominio.Equipo;
import com.tallerwebi.dominio.EquipoTorneo;
import com.tallerwebi.dominio.ServicioEstadisticasDeEquipos;
import com.tallerwebi.dominio.ServicioTorneo;
import com.tallerwebi.dominio.Torneo;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.servlet.ModelAndView;

public class ControladorEstadisticasDeEquiposTest {

  private ControladorEstadisticasDeEquipos controlador;
  private ServicioEstadisticasDeEquipos servicioEstadisticasMock;
  private ServicioTorneo servicioTorneoMock;

  @BeforeEach
  public void init() {
    servicioEstadisticasMock = mock(ServicioEstadisticasDeEquipos.class);
    servicioTorneoMock = mock(ServicioTorneo.class);
    controlador =
      new ControladorEstadisticasDeEquipos(servicioEstadisticasMock, servicioTorneoMock);
  }

  @Test
  public void sinParametrosMuestraLaEstadisticaPorDefectoDePuntos() throws Exception {
    // preparacion
    Torneo torneo = new Torneo();
    torneo.setId(7L);
    Map<String, String> metricas = new LinkedHashMap<>();
    metricas.put("puntos", "Puntos");
    when(servicioTorneoMock.obtenerTorneos()).thenReturn(List.of(torneo));
    when(servicioEstadisticasMock.normalizarMetrica(null)).thenReturn("puntos");
    when(servicioEstadisticasMock.obtenerMetricas()).thenReturn(metricas);
    when(servicioEstadisticasMock.obtenerRanking(7L, "puntos")).thenReturn(List.of());

    // ejecucion
    ModelAndView mav = controlador.irAEstadisticasDeEquipos(null, null);

    // validacion
    assertThat(mav.getViewName(), equalToIgnoringCase("estadisticasDeEquipos"));
    assertThat(mav.getModel().get("metricaActual").toString(), equalTo("puntos"));
    verify(servicioEstadisticasMock).obtenerRanking(7L, "puntos");
  }

  @Test
  public void sePuedeSeleccionarOtraMetricaDistintaALaPorDefecto() throws Exception {
    // preparacion
    when(servicioTorneoMock.obtenerTorneos()).thenReturn(List.of());
    when(servicioEstadisticasMock.normalizarMetrica("golesAFavor")).thenReturn("golesAFavor");
    when(servicioEstadisticasMock.obtenerMetricas()).thenReturn(new LinkedHashMap<>());

    // ejecucion
    ModelAndView mav = controlador.irAEstadisticasDeEquipos("golesAFavor", null);

    // validacion
    assertThat(mav.getModel().get("metricaActual").toString(), equalTo("golesAFavor"));
  }

  @Test
  public void seMuestranNombrePosicionYPartidosJugadosDelEquipo() throws Exception {
    // preparacion
    Equipo river = new Equipo();
    river.setNombre("River");
    EquipoTorneo fila = new EquipoTorneo();
    fila.setEquipo(river);
    fila.setPartidosJugados(5);
    fila.setPuntos(12);
    Map<String, String> metricas = new LinkedHashMap<>();
    metricas.put("puntos", "Puntos");
    when(servicioTorneoMock.obtenerTorneos()).thenReturn(List.of());
    when(servicioEstadisticasMock.normalizarMetrica("puntos")).thenReturn("puntos");
    when(servicioEstadisticasMock.obtenerMetricas()).thenReturn(metricas);
    when(servicioEstadisticasMock.obtenerRanking(9L, "puntos")).thenReturn(List.of(fila));
    when(servicioEstadisticasMock.calcularValor(fila, "puntos")).thenReturn(12);

    // ejecucion
    ModelAndView mav = controlador.irAEstadisticasDeEquipos("puntos", 9L);

    // validacion
    List<DatosEstadisticaEquipo> ranking = rankingDe(mav);
    assertThat(ranking.size(), is(1));
    assertThat(ranking.get(0).getPosicion(), is(1));
    assertThat(ranking.get(0).getNombreEquipo(), equalTo("River"));
    assertThat(ranking.get(0).getPartidosJugados(), is(5));
    assertThat(ranking.get(0).getValor(), is(12));
  }

  @Test
  public void sinTorneosElRankingQuedaVacio() throws Exception {
    // preparacion
    when(servicioTorneoMock.obtenerTorneos()).thenReturn(List.of());
    when(servicioEstadisticasMock.normalizarMetrica(null)).thenReturn("puntos");
    when(servicioEstadisticasMock.obtenerMetricas()).thenReturn(new LinkedHashMap<>());

    // ejecucion
    ModelAndView mav = controlador.irAEstadisticasDeEquipos(null, null);

    // validacion
    assertThat(rankingDe(mav).isEmpty(), is(true));
    verify(servicioEstadisticasMock, never()).obtenerRanking(any(), any());
  }

  @SuppressWarnings("unchecked")
  private List<DatosEstadisticaEquipo> rankingDe(ModelAndView mav) {
    return (List<DatosEstadisticaEquipo>) mav.getModel().get("ranking");
  }
}
