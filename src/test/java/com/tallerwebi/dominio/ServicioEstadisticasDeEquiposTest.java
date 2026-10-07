package com.tallerwebi.dominio;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ServicioEstadisticasDeEquiposTest {

  private RepositorioEquipoTorneo repositorioEquipoTorneoMock;
  private ServicioEstadisticasDeEquipos servicio;

  @BeforeEach
  public void init() {
    repositorioEquipoTorneoMock = mock(RepositorioEquipoTorneo.class);
    servicio = new ServicioEstadisticasDeEquiposImpl(repositorioEquipoTorneoMock);
  }

  private EquipoTorneo fila(
    String nombre,
    int puntos,
    int ganados,
    int golesAFavor,
    int golesEnContra
  ) {
    Equipo equipo = new Equipo();
    equipo.setNombre(nombre);
    EquipoTorneo fila = new EquipoTorneo();
    fila.setEquipo(equipo);
    fila.setPuntos(puntos);
    fila.setPartidosGanados(ganados);
    fila.setGolesAFavor(golesAFavor);
    fila.setGolesEnContra(golesEnContra);
    fila.setPartidosJugados(5);
    return fila;
  }

  @Test
  public void elRankingPorPuntosOrdenaDeMayorAMenor() {
    // preparacion
    EquipoTorneo river = fila("River", 10, 3, 8, 2);
    EquipoTorneo boca = fila("Boca", 15, 5, 9, 1);
    EquipoTorneo sanLorenzo = fila("San Lorenzo", 4, 1, 3, 7);
    when(repositorioEquipoTorneoMock.buscarPorTorneo(1L))
      .thenReturn(List.of(river, boca, sanLorenzo));

    // ejecucion
    List<EquipoTorneo> ranking = servicio.obtenerRanking(1L, "puntos");

    // validacion
    assertThat(ranking.get(0).getEquipo().getNombre(), equalTo("Boca"));
    assertThat(ranking.get(1).getEquipo().getNombre(), equalTo("River"));
    assertThat(ranking.get(2).getEquipo().getNombre(), equalTo("San Lorenzo"));
  }

  @Test
  public void elRankingPorGolesAFavorUsaLosGolesComoCriterio() {
    // preparacion
    EquipoTorneo conMuchosGoles = fila("Ataque", 5, 2, 20, 15);
    EquipoTorneo conPocosGoles = fila("Defensa", 9, 3, 4, 1);
    when(repositorioEquipoTorneoMock.buscarPorTorneo(2L))
      .thenReturn(List.of(conPocosGoles, conMuchosGoles));

    // ejecucion
    List<EquipoTorneo> ranking = servicio.obtenerRanking(2L, "golesAFavor");

    // validacion
    assertThat(ranking.get(0).getEquipo().getNombre(), equalTo("Ataque"));
    assertThat(ranking.get(1).getEquipo().getNombre(), equalTo("Defensa"));
  }

  @Test
  public void unaMetricaInvalidaUsaPuntosPorDefecto() {
    assertThat(servicio.normalizarMetrica("cualquiera"), equalTo("puntos"));
    assertThat(servicio.normalizarMetrica(null), equalTo("puntos"));
  }

  @Test
  public void laDiferenciaDeGolEsGolesAFavorMenosGolesEnContra() {
    EquipoTorneo equipo = fila("Central", 0, 0, 10, 4);
    assertThat(servicio.calcularValor(equipo, "diferenciaGol"), equalTo(6));
  }

  @Test
  public void losValoresNulosSeCuentanComoCero() {
    EquipoTorneo filaSinDatos = new EquipoTorneo();
    assertThat(servicio.calcularValor(filaSinDatos, "puntos"), equalTo(0));
    assertThat(servicio.calcularValor(null, "puntos"), equalTo(0));
  }

  @Test
  public void lasMetricasDisponiblesIncluyenPuntosPorDefecto() {
    Map<String, String> metricas = servicio.obtenerMetricas();
    assertThat(metricas, hasKey("puntos"));
    assertThat(metricas, hasKey("diferenciaGol"));
    assertThat(metricas.size(), is(4));
  }
}
