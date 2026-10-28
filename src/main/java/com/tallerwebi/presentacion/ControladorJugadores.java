package com.tallerwebi.presentacion;

import com.tallerwebi.dominio.*;
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
  private static final String ID_EQUIPO = "idEquipo";
  private static final String REDIRECT_JUGADORES = "redirect:/jugadoresTorneo?idTorneo=";
  private static final String REDIRECT_JUGADORES_ADMIN = "redirect:/jugadoresTorneoAdmin?idTorneo=";
  private static final String ERROR_EQUIPO_NO_INSCRIPTO =
    "El equipo no esta inscripto en este torneo.";
  private static final String ID_JUGADOR = "idJugador";

  private static final String OK = "ok";
  private static final String ERROR = "error";
  private static final String REDIRECT_LOBBY_ADMIN = "redirect:/lobbyAdmin";

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

  // =========================
  // MOSTRAR JUGADORES
  // =========================

  @RequestMapping(path = "/jugadoresTorneo", method = RequestMethod.GET)
  public ModelAndView gestionarJugadores(@RequestParam(ID_TORNEO) Long idTorneo) {
    return gestionarJugadoresComun(idTorneo, "jugadoresTorneo");
  }

  @RequestMapping(path = "/jugadoresTorneoAdmin", method = RequestMethod.GET)
  public ModelAndView gestionarJugadoresAdmin(@RequestParam(ID_TORNEO) Long idTorneo) {
    return gestionarJugadoresComun(idTorneo, "jugadoresTorneoAdmin");
  }

  @RequestMapping(path = "/jugadoresTorneoEditar", method = RequestMethod.GET)
  public ModelAndView gestionarJugadoresEditar(
    @RequestParam(ID_TORNEO) Long idTorneo,
    @RequestParam(ID_EQUIPO) Long idEquipo
  ) {
    ModelAndView modelo = gestionarJugadoresComun(idTorneo, "jugadoresTorneoEditar");

    if (modelo.getModel().containsKey("torneo")) {
      modelo.addObject(ID_EQUIPO, idEquipo);
    }

    return modelo;
  }

  private ModelAndView gestionarJugadoresComun(Long idTorneo, String vista) {
    Torneo torneo = servicioTorneo.consultarTorneoPorId(idTorneo);

    if (torneo == null) {
      return new ModelAndView(REDIRECT_LOBBY_ADMIN);
    }

    Map<String, Object> model = new ModelMap();
    model.put("torneo", torneo);
    model.put("equipos", equiposConJugadores(idTorneo));
    model.put(ID_TORNEO, idTorneo);

    return new ModelAndView(vista, model);
  }

  // =========================
  // AGREGAR JUGADOR
  // =========================

  @RequestMapping(path = "/jugadoresTorneo/agregar", method = RequestMethod.POST)
  public ModelAndView agregarJugador(
    @RequestParam(ID_TORNEO) Long idTorneo,
    @RequestParam(ID_EQUIPO) Long idEquipo,
    @RequestParam("nombre") String nombre,
    @RequestParam("apellido") String apellido,
    @RequestParam(value = "dorsal", required = false) Integer dorsal,
    @RequestParam(value = "puesto", required = false) String puesto,
    RedirectAttributes redirectAttributes
  ) throws JugadorNoExiste {
    return agregarJugadorComun(
      idTorneo,
      idEquipo,
      nombre,
      apellido,
      dorsal,
      puesto,
      redirectAttributes,
      REDIRECT_JUGADORES + idTorneo
    );
  }

  @RequestMapping(path = "/jugadoresTorneoAdmin/agregar", method = RequestMethod.POST)
  public ModelAndView agregarJugadorAdmin(
    @RequestParam(ID_TORNEO) Long idTorneo,
    @RequestParam(ID_EQUIPO) Long idEquipo,
    @RequestParam("nombre") String nombre,
    @RequestParam("apellido") String apellido,
    @RequestParam(value = "dorsal", required = false) Integer dorsal,
    @RequestParam(value = "puesto", required = false) String puesto,
    RedirectAttributes redirectAttributes
  ) throws JugadorNoExiste {
    return agregarJugadorComun(
      idTorneo,
      idEquipo,
      nombre,
      apellido,
      dorsal,
      puesto,
      redirectAttributes,
      REDIRECT_JUGADORES_ADMIN + idTorneo
    );
  }

  @RequestMapping(path = "/jugadoresTorneoEditar/agregar", method = RequestMethod.POST)
  public ModelAndView agregarJugadorEditar(
    @RequestParam(ID_TORNEO) Long idTorneo,
    @RequestParam(ID_EQUIPO) Long idEquipo,
    @RequestParam("nombre") String nombre,
    @RequestParam("apellido") String apellido,
    @RequestParam(value = "dorsal", required = false) Integer dorsal,
    @RequestParam(value = "puesto", required = false) String puesto,
    RedirectAttributes redirectAttributes
  ) throws JugadorNoExiste {
    return agregarJugadorComun(
      idTorneo,
      idEquipo,
      nombre,
      apellido,
      dorsal,
      puesto,
      redirectAttributes,
      redirectJugadoresEditar(idTorneo, idEquipo)
    );
  }

  private ModelAndView agregarJugadorComun(
    Long idTorneo,
    Long idEquipo,
    String nombre,
    String apellido,
    Integer dorsal,
    String puesto,
    RedirectAttributes redirectAttributes,
    String redireccion
  ) throws JugadorNoExiste {
    Torneo torneo = servicioTorneo.consultarTorneoPorId(idTorneo);

    if (torneo == null || !equipoInscriptoEnTorneo(idTorneo, idEquipo)) {
      return rechazar(redireccion, ERROR_EQUIPO_NO_INSCRIPTO, redirectAttributes);
    }

    List<Jugador> jugadores = servicioJugador.obtenerJugadores(idEquipo);

    if (superaMaximo(jugadores, torneo)) {
      return rechazar(
        redireccion,
        "El equipo ya alcanzo la cantidad maxima de jugadores.",
        redirectAttributes
      );
    }

    if (dorsal != null && tieneDorsal(jugadores, dorsal)) {
      return rechazar(
        redireccion,
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

      return new ModelAndView(redireccion);
    }

    redirectAttributes.addFlashAttribute(OK, "Jugador agregado correctamente.");

    return new ModelAndView(redireccion);
  }

  // =========================
  // ELIMINAR JUGADOR
  // =========================

  @RequestMapping(path = "/jugadoresTorneo/eliminar", method = RequestMethod.POST)
  public ModelAndView eliminarJugador(
    @RequestParam(ID_TORNEO) Long idTorneo,
    @RequestParam(ID_EQUIPO) Long idEquipo,
    @RequestParam(ID_JUGADOR) Long idJugador,
    RedirectAttributes redirectAttributes
  ) {
    return eliminarJugadorComun(
      idTorneo,
      idEquipo,
      idJugador,
      redirectAttributes,
      REDIRECT_JUGADORES + idTorneo
    );
  }

  @RequestMapping(path = "/jugadoresTorneoAdmin/eliminar", method = RequestMethod.POST)
  public ModelAndView eliminarJugadorAdmin(
    @RequestParam(ID_TORNEO) Long idTorneo,
    @RequestParam(ID_EQUIPO) Long idEquipo,
    @RequestParam(ID_JUGADOR) Long idJugador,
    RedirectAttributes redirectAttributes
  ) {
    return eliminarJugadorComun(
      idTorneo,
      idEquipo,
      idJugador,
      redirectAttributes,
      REDIRECT_JUGADORES_ADMIN + idTorneo
    );
  }

  @RequestMapping(path = "/jugadoresTorneoEditar/eliminar", method = RequestMethod.POST)
  public ModelAndView eliminarJugadorEditar(
    @RequestParam(ID_TORNEO) Long idTorneo,
    @RequestParam(ID_EQUIPO) Long idEquipo,
    @RequestParam(ID_JUGADOR) Long idJugador,
    RedirectAttributes redirectAttributes
  ) {
    return eliminarJugadorComun(
      idTorneo,
      idEquipo,
      idJugador,
      redirectAttributes,
      redirectJugadoresEditar(idTorneo, idEquipo)
    );
  }

  private ModelAndView eliminarJugadorComun(
    Long idTorneo,
    Long idEquipo,
    Long idJugador,
    RedirectAttributes redirectAttributes,
    String redireccion
  ) {
    Torneo torneo = servicioTorneo.consultarTorneoPorId(idTorneo);

    if (torneo == null || !equipoInscriptoEnTorneo(idTorneo, idEquipo)) {
      return rechazar(redireccion, ERROR_EQUIPO_NO_INSCRIPTO, redirectAttributes);
    }

    List<Jugador> jugadores = servicioJugador.obtenerJugadores(idEquipo);

    if (alcanzaMinimo(jugadores, torneo)) {
      return rechazar(
        redireccion,
        "El equipo no puede quedar por debajo del minimo de jugadores.",
        redirectAttributes
      );
    }

    servicioEstadisticas.eliminarEstadisticas(idJugador);
    servicioJugador.eliminarJugador(idEquipo, idJugador);

    redirectAttributes.addFlashAttribute(OK, "Jugador eliminado correctamente.");

    return new ModelAndView(redireccion);
  }

  // =========================
  // HABILITAR JUGADOR
  // =========================

  @RequestMapping(path = "/jugadoresTorneoAdmin/habilitar", method = RequestMethod.POST)
  public ModelAndView habilitarJugador(
    @RequestParam(ID_TORNEO) Long idTorneo,
    @RequestParam(ID_EQUIPO) Long idEquipo,
    @RequestParam(ID_JUGADOR) Long idJugador,
    @RequestParam(value = "habilitado", required = false) Boolean habilitado,
    RedirectAttributes redirectAttributes
  ) {
    return habilitarJugadorComun(
      idTorneo,
      idEquipo,
      idJugador,
      habilitado,
      redirectAttributes,
      REDIRECT_JUGADORES_ADMIN + idTorneo
    );
  }

  @RequestMapping(path = "/jugadoresTorneoEditar/habilitar", method = RequestMethod.POST)
  public ModelAndView habilitarJugadorEditar(
    @RequestParam(ID_TORNEO) Long idTorneo,
    @RequestParam(ID_EQUIPO) Long idEquipo,
    @RequestParam(ID_JUGADOR) Long idJugador,
    @RequestParam(value = "habilitado", required = false) Boolean habilitado,
    RedirectAttributes redirectAttributes
  ) {
    return habilitarJugadorComun(
      idTorneo,
      idEquipo,
      idJugador,
      habilitado,
      redirectAttributes,
      redirectJugadoresEditar(idTorneo, idEquipo)
    );
  }

  private ModelAndView habilitarJugadorComun(
    Long idTorneo,
    Long idEquipo,
    Long idJugador,
    Boolean habilitado,
    RedirectAttributes redirectAttributes,
    String redireccion
  ) {
    Torneo torneo = servicioTorneo.consultarTorneoPorId(idTorneo);

    if (torneo == null) {
      return new ModelAndView(REDIRECT_LOBBY_ADMIN);
    }

    if (!equipoInscriptoEnTorneo(idTorneo, idEquipo)) {
      return rechazar(redireccion, ERROR_EQUIPO_NO_INSCRIPTO, redirectAttributes);
    }

    if (!perteneceAlEquipo(idEquipo, idJugador)) {
      return rechazar(redireccion, "El jugador no pertenece a este equipo.", redirectAttributes);
    }

    boolean nuevoEstado = habilitado != null && habilitado;

    servicioJugador.habilitarJugador(idJugador, nuevoEstado);

    redirectAttributes.addFlashAttribute(
      OK,
      nuevoEstado ? "Jugador habilitado correctamente." : "Jugador deshabilitado correctamente."
    );

    return new ModelAndView(redireccion);
  }

  @RequestMapping(path = "/jugadoresEquipoSancion", method = RequestMethod.GET)
  public ModelAndView mostrarJugadoresEquipoSancion(
    @RequestParam(ID_TORNEO) Long idTorneo,
    @RequestParam(ID_EQUIPO) Long idEquipo
  ) {
    Torneo torneo = servicioTorneo.consultarTorneoPorId(idTorneo);

    if (torneo == null) {
      return new ModelAndView(REDIRECT_LOBBY_ADMIN);
    }

    if (!equipoInscriptoEnTorneo(idTorneo, idEquipo)) {
      return new ModelAndView("redirect:/elegirEquipoSancion?idTorneo=" + idTorneo);
    }

    List<Jugador> jugadores = servicioJugador.obtenerJugadores(idEquipo);

    Equipo equipo = null;

    for (EquipoTorneo equipoTorneo : servicioEquipoTorneo.obtenerEquiposPorTorneo(idTorneo)) {
      if (equipoTorneo.getEquipo() != null && idEquipo.equals(equipoTorneo.getEquipo().getId())) {
        equipo = equipoTorneo.getEquipo();
        break;
      }
    }

    if (equipo == null) {
      return new ModelAndView("redirect:/elegirEquipoSancion?idTorneo=" + idTorneo);
    }

    ModelAndView modelo = new ModelAndView("jugadoresEquipoSancion");
    modelo.addObject("equipo", equipo);
    modelo.addObject("jugadores", jugadores);
    modelo.addObject(ID_TORNEO, idTorneo);

    return modelo;
  }

  //sancionar jugador
  @RequestMapping(path = "/jugadoresTorneoAdmin/sancionar", method = RequestMethod.POST)
  public ModelAndView sancionarJugador(
    @RequestParam("idTorneo") Long idTorneo,
    @RequestParam("idEquipo") Long idEquipo,
    @RequestParam(ID_JUGADOR) Long idJugador,
    @RequestParam("fechas") Integer fechas,
    RedirectAttributes redirectAttributes
  ) {
    String redireccion =
      "redirect:/jugadoresEquipoSancion?idTorneo=" + idTorneo + "&idEquipo=" + idEquipo;

    Torneo torneo = servicioTorneo.consultarTorneoPorId(idTorneo);

    if (torneo == null) {
      return new ModelAndView(REDIRECT_LOBBY_ADMIN);
    }

    if (!equipoInscriptoEnTorneo(idTorneo, idEquipo)) {
      return rechazar(redireccion, ERROR_EQUIPO_NO_INSCRIPTO, redirectAttributes);
    }

    if (!perteneceAlEquipo(idEquipo, idJugador)) {
      return rechazar(redireccion, "El jugador no pertenece a este equipo.", redirectAttributes);
    }

    if (fechas == null || fechas <= 0) {
      return rechazar(
        redireccion,
        "La cantidad de fechas debe ser mayor a cero.",
        redirectAttributes
      );
    }

    servicioJugador.sancionarJugador(idJugador, fechas);

    redirectAttributes.addFlashAttribute(OK, "Jugador sancionado por " + fechas + " fecha(s).");

    return new ModelAndView(redireccion);
  }

  // =========================
  // MÉTODOS AUXILIARES
  // =========================

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

  private boolean perteneceAlEquipo(Long idEquipo, Long idJugador) {
    if (idJugador == null) {
      return false;
    }

    for (Jugador jugador : servicioJugador.obtenerJugadores(idEquipo)) {
      if (jugador.getId() != null && jugador.getId().equals(idJugador)) {
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

  private String redirectJugadoresEditar(Long idTorneo, Long idEquipo) {
    return "redirect:/jugadoresTorneoEditar?idTorneo=" + idTorneo + "&idEquipo=" + idEquipo;
  }
}
