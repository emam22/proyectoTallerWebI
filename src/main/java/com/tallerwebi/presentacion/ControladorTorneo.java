package com.tallerwebi.presentacion;

import com.tallerwebi.dominio.*;
import com.tallerwebi.dominio.excepcion.EquipoExistente;
import com.tallerwebi.dominio.excepcion.EquipoYaInscripto;
import com.tallerwebi.dominio.excepcion.TorneoExistente;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class ControladorTorneo {

  private ServicioTorneo servicioTorneo;
  private ServicioEquipoTorneo servicioEquipoTorneo;
  private ServicioEquipo servicioEquipo;

  private static final String TORNEO = "torneo";
  private static final String ID_TORNEO = "idTorneo";
  private static final String ID_EQUIPO = "idEquipo";
  private static final String REDIRECT_CREACION_EQUIPO = "redirect:/creacion-equipo?idTorneo=";
  private static final String ERROR = "error";

  private static final Logger LOGGER = Logger.getLogger(ControladorTorneo.class.getName());

  @Autowired
  public ControladorTorneo(
    ServicioTorneo servicioTorneo,
    ServicioEquipoTorneo servicioEquipoTorneo,
    ServicioEquipo servicioEquipo
  ) {
    this.servicioTorneo = servicioTorneo;
    this.servicioEquipoTorneo = servicioEquipoTorneo;
    this.servicioEquipo = servicioEquipo;
  }

  @RequestMapping(path = "/formulario-torneo", method = RequestMethod.GET)
  public ModelAndView irAFormularioTorneo() {
    Map<String, Object> model = new HashMap<>();
    model.put(TORNEO, new Torneo());
    return new ModelAndView("formulario-torneo", model);
  }

  @RequestMapping(path = "/crearTorneo", method = RequestMethod.POST)
  public ModelAndView crearTorneo(
    @ModelAttribute("torneo") Torneo torneo,
    RedirectAttributes redirectAttributes
  ) {
    if (torneo.getCantidadMaxJugadores() <= torneo.getCantidadMinJugadores()) {
      redirectAttributes.addFlashAttribute(
        ERROR,
        "La cantidad máxima de jugadores debe ser mayor que la mínima"
      );

      return new ModelAndView("redirect:/formulario-torneo");
    }

    try {
      servicioTorneo.registrarTorneo(torneo);
    } catch (TorneoExistente e) {
      redirectAttributes.addFlashAttribute(ERROR, "Ya existe un torneo con este nombre");

      return new ModelAndView("redirect:/formulario-torneo");
    } catch (Exception e) {
      redirectAttributes.addFlashAttribute(ERROR, "Error al crear el torneo");

      return new ModelAndView("redirect:/formulario-torneo");
    }

    return new ModelAndView(REDIRECT_CREACION_EQUIPO + torneo.getId());
  }

  @RequestMapping(path = "/creacion-equipo", method = RequestMethod.GET)
  public ModelAndView crearEquipo(@RequestParam(ID_TORNEO) Long id) {
    Map<String, Object> model = new HashMap<>();
    Torneo torneoEnCurso = servicioTorneo.consultarTorneoPorId(id);

    int cantidadEquiposActuales = servicioEquipoTorneo.contarEquiposPorTorneo(id);

    int cantidadEquiposNecesarios = 0;
    int minJugadores = 11;
    int maxJugadores = 23;

    if (torneoEnCurso != null) {
      cantidadEquiposNecesarios = torneoEnCurso.getCantidadDeEquipos();

      minJugadores = torneoEnCurso.getCantidadMinJugadores();
      maxJugadores = torneoEnCurso.getCantidadMaxJugadores();
    }

    model.put(ID_TORNEO, id);
    model.put("cantidadEquiposActuales", cantidadEquiposActuales);
    model.put("cantidadEquiposNecesarios", cantidadEquiposNecesarios);
    model.put("minJugadores", minJugadores);
    model.put("maxJugadores", maxJugadores);

    return new ModelAndView("creacion-equipo", model);
  }

  /*Los parametros deben llegar por ModelAttribute*/
  @RequestMapping(path = "/guardarEquipo", method = RequestMethod.POST)
  public ModelAndView guardarEquipo(
    @RequestParam(ID_TORNEO) Long idTorneo,
    @RequestParam("nombreEquipo") String nombreEquipo,
    @RequestParam("colorLocal1") String colorLocal1,
    @RequestParam("colorLocal2") String colorLocal2,
    @RequestParam("colorVisitante1") String colorVisitante1,
    @RequestParam("colorVisitante2") String colorVisitante2,
    RedirectAttributes redirectAttributes
  ) {
    Torneo torneo = servicioTorneo.consultarTorneoPorId(idTorneo);

    int cantidadActual = servicioEquipoTorneo.contarEquiposPorTorneo(idTorneo);

    int cantidadNecesaria = torneo.getCantidadDeEquipos();

    if (cantidadActual >= cantidadNecesaria) {
      return new ModelAndView(REDIRECT_CREACION_EQUIPO + idTorneo);
    }

    Equipo equipo = new Equipo();

    equipo.setNombre(nombreEquipo);
    equipo.setColorLocal1(colorLocal1);
    equipo.setColorLocal2(colorLocal2);
    equipo.setColorVisitante1(colorVisitante1);
    equipo.setColorVisitante2(colorVisitante2);

    try {
      servicioEquipoTorneo.crearEquipoEInscribirlo(equipo, torneo);
    } catch (EquipoExistente e) {
      redirectAttributes.addFlashAttribute(ERROR, "Ya existe un equipo con ese nombre");

      return new ModelAndView(REDIRECT_CREACION_EQUIPO + idTorneo);
    }

    return new ModelAndView(REDIRECT_CREACION_EQUIPO + idTorneo);
  }

  @RequestMapping(path = "/lista-equipos", method = RequestMethod.GET)
  public ModelAndView listarEquipos(@RequestParam(ID_TORNEO) Long idTorneo) {
    Map<String, Object> model = new HashMap<>();

    List<Equipo> equipos = servicioEquipo.obtenerEquipos();

    model.put("equipos", equipos);
    model.put(ID_TORNEO, idTorneo);

    return new ModelAndView("lista-equipos", model);
  }

  @RequestMapping(path = "/cargar-equipo-existente", method = RequestMethod.POST)
  public ModelAndView cargarEquipoExistente(
    @RequestParam(ID_EQUIPO) Long idEquipo,
    @RequestParam(ID_TORNEO) Long idTorneo,
    RedirectAttributes redirectAttributes
  ) {
    Equipo equipo = this.servicioEquipo.consultarEquipoPorId(idEquipo);
    Torneo torneo = this.servicioTorneo.consultarTorneoPorId(idTorneo);

    try {
      this.servicioEquipoTorneo.inscribirEquipoExistente(equipo, torneo);
    } catch (EquipoYaInscripto e) {
      redirectAttributes.addFlashAttribute(ERROR, "El equipo ya está inscripto en este torneo");
    }

    return new ModelAndView(REDIRECT_CREACION_EQUIPO + idTorneo);
  }

  @RequestMapping(path = "/finalizarTorneo", method = RequestMethod.POST)
  public ModelAndView finalizarTorneo(
    @RequestParam(ID_TORNEO) Long idTorneo,
    RedirectAttributes redirectAttributes
  ) {
    Torneo torneo = servicioTorneo.consultarTorneoPorId(idTorneo);

    if (torneo == null) {
      redirectAttributes.addFlashAttribute("error", "No se encontró el torneo.");
      return new ModelAndView("redirect:/lobbyAdmin");
    }

    /* Deberia estar en un metodo del servicio
    int cantidadActual = servicioEquipoTorneo.contarEquiposPorTorneo(idTorneo);

    int cantidadNecesaria = torneo.getCantidadDeEquipos();


    if (cantidadActual < cantidadNecesaria) {
      return new ModelAndView("redirect:/creacion-equipo?idTorneo=" + idTorneo);
    }*/

    try {
      // Comprobamos si el fixture ya fue generado.
      List<Jornada> jornadasExistentes = servicioTorneo.obtenerJornadasPorTorneo(idTorneo);

      if (jornadasExistentes == null || jornadasExistentes.isEmpty()) {
        // Recuperamos las inscripciones del torneo.
        List<EquipoTorneo> inscripciones = servicioEquipoTorneo.obtenerEquiposPorTorneo(idTorneo);

        // Obtenemos los IDs de los equipos inscriptos.
        List<Long> idsEquipos = inscripciones
          .stream()
          .map(inscripcion -> inscripcion.getEquipo().getId())
          .toList();

        // Generamos y guardamos las jornadas y sus partidos.
        servicioTorneo.crearTorneoConFixture(torneo, idsEquipos);
      }

      // Volvemos al lobby con el fixture ya generado.
      return new ModelAndView("redirect:/lobbyAdmin");
    } catch (Exception e) {
      redirectAttributes.addFlashAttribute(
        "error",
        "No se pudo generar el fixture: " + e.getMessage()
      );

      return new ModelAndView("redirect:/lobbyAdmin");
    }
  }

  @RequestMapping(path = "/fixtureTorneo", method = RequestMethod.GET)
  public ModelAndView verFixture(@RequestParam(ID_TORNEO) Long idTorneo) {
    Map<String, Object> model = new HashMap<>();

    Torneo torneo = servicioTorneo.consultarTorneoPorId(idTorneo);
    List<Jornada> jornadas = servicioTorneo.obtenerJornadasPorTorneo(idTorneo);

    model.put("torneo", torneo);
    model.put("jornadas", jornadas);

    return new ModelAndView("fixtureTorneo", model);
  }
}
