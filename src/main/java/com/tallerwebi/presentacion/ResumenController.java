package com.tallerwebi.presentacion;

import com.tallerwebi.dominio.*;
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
  private final RepositorioEventoPartido repositorioEventoPartido;

  public ResumenController(
    ServicioTorneo servicioTorneo,
    RepositorioEstadisticaJugador repositorioEstadisticaJugador,
    RepositorioJugadorEquipoTorneo repositorioJugadorEquipoTorneo,
    RepositorioEventoPartido repositorioEventoPartido
  ) {
    this.servicioTorneo = servicioTorneo;
    this.repositorioEstadisticaJugador = repositorioEstadisticaJugador;
    this.repositorioJugadorEquipoTorneo = repositorioJugadorEquipoTorneo;
    this.repositorioEventoPartido = repositorioEventoPartido;
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
    @ModelAttribute("buscarResumenDTO") BuscarResumenDTO buscarResumenDTO
  ) {
    Map<String, Object> model = new HashMap<>();

    Long idTorneoActual = buscarResumenDTO.getIdTorneo();
    Long idJornada = buscarResumenDTO.getIdJornada();

    Torneo torneo = idTorneoActual == null
      ? null
      : servicioTorneo.consultarTorneoPorId(idTorneoActual);

    model.put("idTorneoActual", idTorneoActual);

    if (torneo == null || idJornada == null) {
      model.put("error", "No se encontró el torneo o la jornada solicitada.");
      return new ModelAndView("resumen", model);
    }

    List<Jornada> jornadas = servicioTorneo.obtenerJornadasPorTorneo(idTorneoActual);

    Jornada jornadaSeleccionada = buscarJornada(jornadas, idJornada);

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
      resumenPartidos.add(crearResumenPartido(partido, idTorneoActual, estadisticasPorJugador));
    }

    model.put("resumenPartidos", resumenPartidos);

    return new ModelAndView("resumen", model);
  }

  // Busca la jornada solicitada dentro de las jornadas del torneo.
  private Jornada buscarJornada(List<Jornada> jornadas, Long idJornada) {
    for (Jornada jornada : jornadas) {
      if (jornada.getId().equals(idJornada)) {
        return jornada;
      }
    }

    return null;
  }

  // Prepara las estadísticas y los eventos correspondientes a un partido.
  private ResumenPartidoDTO crearResumenPartido(
    Partido partido,
    Long idTorneo,
    Map<Long, EstadisticaJugador> estadisticasPorJugador
  ) {
    List<EstadisticaJugador> estadisticasLocal = obtenerEstadisticasDelEquipo(
      partido.getEquipoLocal().getId(),
      idTorneo,
      estadisticasPorJugador
    );

    List<EstadisticaJugador> estadisticasVisitante = obtenerEstadisticasDelEquipo(
      partido.getEquipoVisitante().getId(),
      idTorneo,
      estadisticasPorJugador
    );

    List<EventoPartidoDto> eventos = obtenerEventosDelPartido(partido, idTorneo);

    return new ResumenPartidoDTO(partido, estadisticasLocal, estadisticasVisitante, eventos);
  }

  // Obtiene los eventos del partido y el equipo al que pertenece cada jugador.
  private List<EventoPartidoDto> obtenerEventosDelPartido(Partido partido, Long idTorneo) {
    List<EventoPartidoDto> eventos = new ArrayList<>();

    for (EventoPartido evento : repositorioEventoPartido.obtenerEventosPorPartido(
      partido.getId()
    )) {
      String nombreEquipo = obtenerNombreEquipoDelJugador(evento, idTorneo);

      eventos.add(new EventoPartidoDto(evento, nombreEquipo));
    }

    return eventos;
  }

  // Obtiene el nombre del equipo al que pertenece el jugador en el torneo.
  private String obtenerNombreEquipoDelJugador(EventoPartido evento, Long idTorneo) {
    if (evento.getJugador() == null || evento.getJugador().getId() == null) {
      return null;
    }

    JugadorEquipoTorneo inscripcion = repositorioJugadorEquipoTorneo.buscarPorJugadorYTorneo(
      evento.getJugador().getId(),
      idTorneo
    );

    if (inscripcion == null || inscripcion.getEquipo() == null) {
      return null;
    }

    return inscripcion.getEquipo().getNombre();
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
