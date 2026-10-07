package com.tallerwebi.dominio;

import java.util.List;
import java.util.Map;

public interface ServicioEstadisticasDeEquipos {
  Map<String, String> obtenerMetricas();

  String normalizarMetrica(String metrica);

  Integer calcularValor(EquipoTorneo fila, String metrica);

  List<EquipoTorneo> obtenerRanking(Long idTorneo, String metrica);
}
