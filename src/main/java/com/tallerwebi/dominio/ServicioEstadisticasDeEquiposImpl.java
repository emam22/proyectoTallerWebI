package com.tallerwebi.dominio;

import jakarta.transaction.Transactional;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service("ServicioEstadisticasDeEquipos")
@Transactional
public class ServicioEstadisticasDeEquiposImpl implements ServicioEstadisticasDeEquipos {

  private static final String METRICA_DEFECTO = "puntos";
  private static final String METRICA_GANADOS = "partidosGanados";
  private static final String METRICA_GOLES = "golesAFavor";
  private static final String METRICA_DIFERENCIA = "diferenciaGol";

  private RepositorioEquipoTorneo repositorioEquipoTorneo;

  @Autowired
  public ServicioEstadisticasDeEquiposImpl(RepositorioEquipoTorneo repositorioEquipoTorneo) {
    this.repositorioEquipoTorneo = repositorioEquipoTorneo;
  }

  @Override
  public Map<String, String> obtenerMetricas() {
    Map<String, String> metricas = new LinkedHashMap<>();
    metricas.put(METRICA_DEFECTO, "Puntos");
    metricas.put(METRICA_GANADOS, "Partidos ganados");
    metricas.put(METRICA_GOLES, "Goles a favor");
    metricas.put(METRICA_DIFERENCIA, "Diferencia de gol");
    return metricas;
  }

  @Override
  public String normalizarMetrica(String metrica) {
    if (metrica == null || !obtenerMetricas().containsKey(metrica)) {
      return METRICA_DEFECTO;
    }
    return metrica;
  }

  @Override
  public Integer calcularValor(EquipoTorneo fila, String metrica) {
    if (fila == null) {
      return 0;
    }
    String metricaActual = normalizarMetrica(metrica);
    if (METRICA_GANADOS.equals(metricaActual)) {
      return valorOZero(fila.getPartidosGanados());
    }
    if (METRICA_GOLES.equals(metricaActual)) {
      return valorOZero(fila.getGolesAFavor());
    }
    if (METRICA_DIFERENCIA.equals(metricaActual)) {
      return valorOZero(fila.getGolesAFavor()) - valorOZero(fila.getGolesEnContra());
    }
    return valorOZero(fila.getPuntos());
  }

  @Override
  public List<EquipoTorneo> obtenerRanking(Long idTorneo, String metrica) {
    String metricaActual = normalizarMetrica(metrica);
    List<EquipoTorneo> ranking = new ArrayList<>(repositorioEquipoTorneo.buscarPorTorneo(idTorneo));
    ranking.sort(
      Comparator
        .comparingInt((EquipoTorneo fila) -> calcularValor(fila, metricaActual))
        .reversed()
        .thenComparing(ServicioEstadisticasDeEquiposImpl::nombreDelEquipo)
    );
    return ranking;
  }

  private static int valorOZero(Integer valor) {
    return valor == null ? 0 : valor;
  }

  private static String nombreDelEquipo(EquipoTorneo fila) {
    if (fila.getEquipo() == null || fila.getEquipo().getNombre() == null) {
      return "";
    }
    return fila.getEquipo().getNombre();
  }
}
