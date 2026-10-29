package com.tallerwebi.presentacion;

import com.tallerwebi.dominio.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class LobbyController {

  private static final int MAX_JORNADAS_A_MOSTRAR_EN_LOBBY = 4;
  private final ServicioEquipoTorneo servicioEquipoTorneo;
  private final ServicioTorneo servicioTorneo;
  private final ServicioEstadisticaJugador servicioEstadisticas;
  private final ServicioEquipo servicioEquipo;
  private static final String ESTADO_PENDIENTE = "Pendiente";

  public LobbyController(
    ServicioEquipoTorneo servicioEquipoTorneo,
    ServicioTorneo servicioTorneo,
    ServicioEstadisticaJugador servicioEstadisticas,
    ServicioEquipo servicioEquipo
  ) {
    this.servicioEquipoTorneo = servicioEquipoTorneo;
    this.servicioTorneo = servicioTorneo;
    this.servicioEstadisticas = servicioEstadisticas;
    this.servicioEquipo = servicioEquipo;
  }

  @RequestMapping("/lobby")
  public ModelAndView irALobby(@RequestParam(value = "idTorneo", required = false) Long idTorneo) {
    Map<String, Object> model = new ModelMap();

    Long idTorneoABuscar = idTorneo != null ? idTorneo : 1L;

    List<EquipoTorneo> tabla = servicioEquipoTorneo.obtenerEstadisticas(idTorneoABuscar);
    model.put("tablaPosiciones", tabla);
    model.put("idTorneoActual", idTorneoABuscar);

    List<EstadisticaJugador> estadisticas = servicioEstadisticas.obtenerEstadisticas();

    Map<Long, Equipo> equiposPorJugador = obtenerMapaEquiposPorJugador(estadisticas);

    model.put("estadisticasJugadores", estadisticas);
    model.put("equiposPorJugador", equiposPorJugador);

    //cant real de equipos agregados
    int totalEquipos = servicioEquipoTorneo.contarEquiposPorTorneo(idTorneoABuscar);
    model.put("totalEquipos", totalEquipos);

    //cant de fechas finalizadas

    long fechasJugadas = servicioTorneo.contarFechasFinalizadas(idTorneoABuscar);
    model.put("fechasJugadas", fechasJugadas);

    List<Jornada> jornadas = servicioTorneo.obtenerJornadasPorTorneo(idTorneoABuscar);

    String proximaFechaNombre = "Fixture no generado"; // se buscaria la primer fecha a simular

    for (Jornada jornada : jornadas) {
      if (ESTADO_PENDIENTE.equalsIgnoreCase(jornada.getEstado())) {
        proximaFechaNombre = "Fecha " + jornada.getNumero();
        break;
      }
    }

    if (!jornadas.isEmpty() && "Fixture no generado".equals(proximaFechaNombre)) {
      proximaFechaNombre = "No hay fechas pendientes"; // cuando hay jornadas pero se terminan
    }

    //mostar jornadas en card FIXTURE
    List<Jornada> proximasJornadas = new ArrayList<Jornada>();

    for (Jornada jornada : jornadas) {
      if (ESTADO_PENDIENTE.equalsIgnoreCase(jornada.getEstado())) {
        proximasJornadas.add(jornada);
      }

      if (proximasJornadas.size() == MAX_JORNADAS_A_MOSTRAR_EN_LOBBY) {
        break;
      }
    }

    model.put("proximasJornadas", proximasJornadas);

    model.put("proximaFechaNombre", proximaFechaNombre);

    return new ModelAndView("lobby", model);
  }

  @GetMapping("/estadisticasDeJugadores")
  public ModelAndView verEstadisticasJugadores(
    @RequestParam(value = "idTorneo", required = false) Long idTorneo
  ) {
    Map<String, Object> model = new ModelMap();

    Long idTorneoABuscar = idTorneo != null ? idTorneo : 1L;

    List<EstadisticaJugador> estadisticas = servicioEstadisticas.obtenerEstadisticas();

    model.put("estadisticasDeJugadores", estadisticas);
    model.put("goleador", obtenerGoleador(estadisticas));
    model.put("idTorneoActual", idTorneoABuscar);

    return new ModelAndView("estadisticasDeJugadores", model);
  }

  private EstadisticaJugador obtenerGoleador(List<EstadisticaJugador> estadisticas) {
    EstadisticaJugador goleador = null;

    if (estadisticas == null) {
      return null;
    }

    for (EstadisticaJugador estadistica : estadisticas) {
      if (
        estadistica.getGoles() != null &&
        (goleador == null || estadistica.getGoles() > goleador.getGoles())
      ) {
        goleador = estadistica;
      }
    }

    return goleador;
  }

  private Map<Long, Equipo> obtenerMapaEquiposPorJugador(List<EstadisticaJugador> estadisticas) {
    Map<Long, Equipo> equipos = new HashMap<>();
    if (estadisticas == null) {
      return equipos;
    }

    for (EstadisticaJugador est : estadisticas) {
      if (est.getJugador() != null) {
        Long idJugador = est.getJugador().getId();
        if (idJugador != null && !equipos.containsKey(idJugador)) {
          equipos.put(idJugador, servicioEquipo.consultarEquipoJugador(idJugador));
        }
      }
    }
    return equipos;
  }
}
