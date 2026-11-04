package com.tallerwebi.presentacion;

import com.tallerwebi.dominio.EstadisticaJugador;
import com.tallerwebi.dominio.Jornada;
import com.tallerwebi.dominio.JugadorEquipoTorneo;
import com.tallerwebi.dominio.Partido;
import com.tallerwebi.dominio.RepositorioEstadisticaJugador;
import com.tallerwebi.dominio.RepositorioJugadorEquipoTorneo;
import com.tallerwebi.dominio.ServicioTorneo;
import com.tallerwebi.dominio.Torneo;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class ResumenController {

  private final ServicioTorneo servicioTorneo;
  private final RepositorioEstadisticaJugador repositorioEstadisticaJugador;
  private final RepositorioJugadorEquipoTorneo repositorioJugadorEquipoTorneo;

  public ResumenController(
    ServicioTorneo servicioTorneo,
    RepositorioEstadisticaJugador repositorioEstadisticaJugador,
    RepositorioJugadorEquipoTorneo repositorioJugadorEquipoTorneo
  ) {
    this.servicioTorneo = servicioTorneo;
    this.repositorioEstadisticaJugador = repositorioEstadisticaJugador;
    this.repositorioJugadorEquipoTorneo = repositorioJugadorEquipoTorneo;
  }

  // Carga el torneo correspondiente al ID recibido en la URL.
  @ModelAttribute("torneo")
  public Torneo cargarTorneo(@RequestParam(value = "idTorneo", required = false) Long idTorneo) {
    return idTorneo == null ? null : servicioTorneo.consultarTorneoPorId(idTorneo);
  }

  // Atiende las solicitudes para ingresar a la pantalla de resumen.
  @Transactional(readOnly = true)
  @RequestMapping("/resumen")
  public ModelAndView irAResumen(
    @ModelAttribute("torneo") Torneo torneo,
    @RequestParam(value = "idJornada", required = false) Long idJornada
  ) {
    Map<String, Object> model = new HashMap<>();

    Long idTorneoActual = torneo == null ? null : torneo.getId();
    model.put("idTorneoActual", idTorneoActual);

    if (torneo == null || idJornada == null) {
      model.put("error", "No se encontró el torneo o la jornada solicitada.");
      return new ModelAndView("resumen", model);
    }

    List<Jornada> jornadas = servicioTorneo.obtenerJornadasPorTorneo(idTorneoActual);

    Jornada jornadaSeleccionada = null;

    for (Jornada jornada : jornadas) {
      if (jornada.getId().equals(idJornada)) {
        jornadaSeleccionada = jornada;
        break;
      }
    }

    if (jornadaSeleccionada == null) {
      model.put("error", "No se encontró la jornada solicitada.");
      return new ModelAndView("resumen", model);
    }

    model.put("jornada", jornadaSeleccionada);
    model.put("partidos", jornadaSeleccionada.getPartidos());

    // Obtenemos todas las estadísticas existentes una sola vez.
    Map<Long, EstadisticaJugador> estadisticasPorJugador = obtenerEstadisticasPorJugador();

    // Preparamos los datos de cada partido para la vista.
    List<ResumenPartidoDTO> resumenPartidos = new ArrayList<>();

    for (Partido partido : jornadaSeleccionada.getPartidos()) {
      // Obtenemos las estadísticas de los jugadores del equipo local.
      List<EstadisticaJugador> estadisticasLocal = obtenerEstadisticasDelEquipo(
        partido.getEquipoLocal().getId(),
        idTorneoActual,
        estadisticasPorJugador
      );

      // Obtenemos las estadísticas de los jugadores del equipo visitante.
      List<EstadisticaJugador> estadisticasVisitante = obtenerEstadisticasDelEquipo(
        partido.getEquipoVisitante().getId(),
        idTorneoActual,
        estadisticasPorJugador
      );

      resumenPartidos.add(new ResumenPartidoDTO(partido, estadisticasLocal, estadisticasVisitante));
    }

    model.put("resumenPartidos", resumenPartidos);

    return new ModelAndView("resumen", model);
  }

  // Crea un mapa para encontrar rápidamente las estadísticas de cada jugador.
  private Map<Long, EstadisticaJugador> obtenerEstadisticasPorJugador() {
    Map<Long, EstadisticaJugador> estadisticasPorJugador = new HashMap<>();

    for (EstadisticaJugador estadistica : repositorioEstadisticaJugador.obtenerEstadisticas()) {
      if (estadistica.getJugador() != null) {
        estadisticasPorJugador.put(estadistica.getJugador().getId(), estadistica);
      }
    }

    return estadisticasPorJugador;
  }

  // Obtiene las estadísticas de los jugadores inscriptos en un equipo del torneo.
  private List<EstadisticaJugador> obtenerEstadisticasDelEquipo(
    Long idEquipo,
    Long idTorneo,
    Map<Long, EstadisticaJugador> estadisticasPorJugador
  ) {
    List<JugadorEquipoTorneo> jugadores = repositorioJugadorEquipoTorneo.listarPorEquipoYTorneo(
      idEquipo,
      idTorneo
    );

    List<EstadisticaJugador> estadisticasEquipo = new ArrayList<>();

    for (JugadorEquipoTorneo inscripcion : jugadores) {
      if (inscripcion.getJugador() == null) {
        continue;
      }

      Long idJugador = inscripcion.getJugador().getId();

      EstadisticaJugador estadistica = estadisticasPorJugador.get(idJugador);

      if (estadistica != null) {
        estadisticasEquipo.add(estadistica);
      }
    }

    return estadisticasEquipo;
  }
}
