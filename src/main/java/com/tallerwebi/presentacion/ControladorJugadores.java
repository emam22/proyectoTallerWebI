package com.tallerwebi.presentacion;

import com.tallerwebi.dominio.*;
import com.tallerwebi.dominio.excepcion.EquipoExistente;
import com.tallerwebi.dominio.excepcion.JugadorNoExiste;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class ControladorJugadores {

  private static final String ID_TORNEO = "idTorneo";
  private static final String REDIRECT_JUGADORES = "redirect:/jugadoresTorneo?idTorneo=";
  private static final String OK = "ok";
  private static final String ERROR = "error";

  private final ServicioTorneo servicioTorneo;
  private final ServicioEquipoTorneo servicioEquipoTorneo;
  private final ServicioJugador servicioJugador;
  private final ServicioEstadisticaJugador servicioEstadisticas;

  @Autowired
  public ControladorJugadores(
    ServicioTorneo servicioTorneo,
    ServicioEquipoTorneo servicioEquipoTorneo,
    ServicioJugador servicioJugador,
    ServicioEstadisticaJugador servicioEstadisticas
  ) {
    this.servicioTorneo = servicioTorneo;
    this.servicioEquipoTorneo = servicioEquipoTorneo;
    this.servicioJugador = servicioJugador;
    this.servicioEstadisticas = servicioEstadisticas;
  }

  @RequestMapping(path = "/jugadoresTorneo", method = RequestMethod.GET)
  public ModelAndView gestionarJugadores(@RequestParam(ID_TORNEO) Long idTorneo) {
    Torneo torneo = servicioTorneo.consultarTorneoPorId(idTorneo);
    if (torneo == null) {
      return new ModelAndView("redirect:/lobbyAdmin");
    }

    Map<String, Object> model = new ModelMap();
    model.put("torneo", torneo);
    model.put("equipos", equiposConJugadores(idTorneo));
    return new ModelAndView("jugadoresTorneo", model);
  }

  @RequestMapping(path = "/jugadoresTorneo/agregar", method = RequestMethod.POST)
  public ModelAndView agregarJugador(
    @RequestParam(ID_TORNEO) Long idTorneo,
    @RequestParam("idEquipo") Long idEquipo,
    @RequestParam("nombre") String nombre,
    @RequestParam("apellido") String apellido,
    @RequestParam(value = "dorsal", required = false) Integer dorsal,
    @RequestParam(value = "puesto", required = false) String puesto,
    RedirectAttributes redirectAttributes
  ) throws JugadorNoExiste {
    Torneo torneo = servicioTorneo.consultarTorneoPorId(idTorneo);

    if (torneo == null || !equipoInscriptoEnTorneo(idTorneo, idEquipo)) {
      return rechazar(
        REDIRECT_JUGADORES + idTorneo,
        "El equipo no esta inscripto en este torneo.",
        redirectAttributes
      );
    }

    List<Jugador> jugadores = servicioJugador.obtenerJugadores(idEquipo);

    if (superaMaximo(jugadores, torneo)) {
      return rechazar(
        REDIRECT_JUGADORES + idTorneo,
        "El equipo ya alcanzo la cantidad maxima de jugadores.",
        redirectAttributes
      );
    }

    if (dorsal != null && tieneDorsal(jugadores, dorsal)) {
      return rechazar(
        REDIRECT_JUGADORES + idTorneo,
        "Ya hay un jugador con el dorsal " + dorsal + " en este equipo.",
        redirectAttributes
      );
    }

    Jugador jugador = new Jugador();
    jugador.setNombre(nombre);
    jugador.setApellido(apellido);
    jugador.setDorsal(dorsal);
    jugador.setPuesto(puesto);

    servicioJugador.agregarJugador(idEquipo, jugador);

    try {
      servicioEstadisticas.iniciarEstadisticas(jugador);
    } catch (JugadorNoExiste e) {
      redirectAttributes.addFlashAttribute(ERROR, "No se pudo agregar al jugador.");

      return new ModelAndView(REDIRECT_JUGADORES + idTorneo);
    }

    redirectAttributes.addFlashAttribute(OK, "Jugador agregado correctamente.");

    return new ModelAndView(REDIRECT_JUGADORES + idTorneo);
  }

  @RequestMapping(path = "/jugadoresTorneo/eliminar", method = RequestMethod.POST)
  public ModelAndView eliminarJugador(
    @RequestParam(ID_TORNEO) Long idTorneo,
    @RequestParam("idEquipo") Long idEquipo,
    @RequestParam("idJugador") Long idJugador,
    RedirectAttributes redirectAttributes
  ) {
    Torneo torneo = servicioTorneo.consultarTorneoPorId(idTorneo);

    if (torneo == null || !equipoInscriptoEnTorneo(idTorneo, idEquipo)) {
      return rechazar(
        REDIRECT_JUGADORES + idTorneo,
        "El equipo no esta inscripto en este torneo.",
        redirectAttributes
      );
    }

    List<Jugador> jugadores = servicioJugador.obtenerJugadores(idEquipo);

    if (alcanzaMinimo(jugadores, torneo)) {
      return rechazar(
        REDIRECT_JUGADORES + idTorneo,
        "El equipo no puede quedar por debajo del minimo de jugadores.",
        redirectAttributes
      );
    }

    servicioEstadisticas.eliminarEstadisticas(idJugador);
    servicioJugador.eliminarJugador(idEquipo, idJugador);

    redirectAttributes.addFlashAttribute(OK, "Jugador eliminado correctamente.");
    return new ModelAndView(REDIRECT_JUGADORES + idTorneo);
  }

  private ModelAndView rechazar(
    String vista,
    String motivo,
    RedirectAttributes redirectAttributes
  ) {
    redirectAttributes.addFlashAttribute(ERROR, motivo);
    return new ModelAndView(vista);
  }

  private boolean equipoInscriptoEnTorneo(Long idTorneo, Long idEquipo) {
    for (EquipoTorneo equipoTorneo : servicioEquipoTorneo.obtenerEquiposPorTorneo(idTorneo)) {
      Equipo equipo = equipoTorneo.getEquipo();
      if (equipo != null && idEquipo.equals(equipo.getId())) {
        return true;
      }
    }
    return false;
  }

  private boolean superaMaximo(List<Jugador> jugadores, Torneo torneo) {
    Integer maximo = torneo.getCantidadMaxJugadores();
    return maximo != null && jugadores.size() >= maximo;
  }

  private boolean alcanzaMinimo(List<Jugador> jugadores, Torneo torneo) {
    Integer minimo = torneo.getCantidadMinJugadores();
    return minimo != null && jugadores.size() <= minimo;
  }

  private boolean tieneDorsal(List<Jugador> jugadores, Integer dorsal) {
    for (Jugador existente : jugadores) {
      if (existente.getDorsal() != null && existente.getDorsal().equals(dorsal)) {
        return true;
      }
    }
    return false;
  }

  private List<EquipoJugadores> equiposConJugadores(Long idTorneo) {
    List<EquipoJugadores> equipos = new ArrayList<>();
    for (EquipoTorneo equipoTorneo : servicioEquipoTorneo.obtenerEquiposPorTorneo(idTorneo)) {
      Equipo equipo = equipoTorneo.getEquipo();
      if (equipo != null) {
        equipos.add(new EquipoJugadores(equipo, servicioJugador.obtenerJugadores(equipo.getId())));
      }
    }
    return equipos;
  }
}
