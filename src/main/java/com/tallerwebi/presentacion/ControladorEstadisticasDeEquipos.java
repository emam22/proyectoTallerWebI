package com.tallerwebi.presentacion;

import com.tallerwebi.dominio.EquipoTorneo;
import com.tallerwebi.dominio.ServicioEstadisticasDeEquipos;
import com.tallerwebi.dominio.ServicioTorneo;
import com.tallerwebi.dominio.Torneo;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class ControladorEstadisticasDeEquipos {

  private ServicioEstadisticasDeEquipos servicioEstadisticas;
  private ServicioTorneo servicioTorneo;

  @Autowired
  public ControladorEstadisticasDeEquipos(
    ServicioEstadisticasDeEquipos servicioEstadisticas,
    ServicioTorneo servicioTorneo
  ) {
    this.servicioEstadisticas = servicioEstadisticas;
    this.servicioTorneo = servicioTorneo;
  }

  @RequestMapping("/estadisticasDeEquipos")
  public ModelAndView irAEstadisticasDeEquipos(
    @RequestParam(value = "metrica", required = false) String metrica,
    @RequestParam(value = "idTorneo", required = false) Long idTorneo
  ) {
    Map<String, Object> model = new ModelMap();

    List<Torneo> torneos = servicioTorneo.obtenerTorneos();
    Long idTorneoActivo = idTorneo;
    if (idTorneoActivo == null && !torneos.isEmpty()) {
      idTorneoActivo = torneos.get(0).getId();
    }

    String metricaActual = servicioEstadisticas.normalizarMetrica(metrica);
    Map<String, String> metricas = servicioEstadisticas.obtenerMetricas();

    List<EquipoTorneo> ranking = Collections.emptyList();
    if (idTorneoActivo != null) {
      ranking = servicioEstadisticas.obtenerRanking(idTorneoActivo, metricaActual);
    }

    List<DatosEstadisticaEquipo> filas = new ArrayList<>();
    int posicion = 1;
    for (EquipoTorneo fila : ranking) {
      filas.add(
        new DatosEstadisticaEquipo(
          posicion,
          nombreDe(fila),
          jugadosDe(fila),
          servicioEstadisticas.calcularValor(fila, metricaActual)
        )
      );
      posicion = posicion + 1;
    }

    model.put("metricas", metricas);
    model.put("metricaActual", metricaActual);
    model.put("metricaActualTitulo", metricas.get(metricaActual));
    model.put("ranking", filas);
    model.put("idTorneo", idTorneoActivo);
    model.put("torneos", torneos);
    return new ModelAndView("estadisticasDeEquipos", model);
  }

  private String nombreDe(EquipoTorneo fila) {
    if (fila.getEquipo() == null || fila.getEquipo().getNombre() == null) {
      return "Sin equipo";
    }
    return fila.getEquipo().getNombre();
  }

  private int jugadosDe(EquipoTorneo fila) {
    if (fila.getPartidosJugados() == null) {
      return 0;
    }
    return fila.getPartidosJugados();
  }
}
