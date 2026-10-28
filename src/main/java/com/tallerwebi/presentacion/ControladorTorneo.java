package com.tallerwebi.presentacion;

import com.tallerwebi.dominio.*;
import com.tallerwebi.dominio.excepcion.*;
import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.*;
import java.util.logging.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
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
  private static final String REDIRECT_CREACION_EQUIPO_ADMIN =
    "redirect:/creacion-equipo-admin?idTorneo=";
  private static final String REDIRECT_LOBBY_ADMIN = "redirect:/lobbyAdmin";
  private static final String REDIRECT_ADMINISTRAR_TORNEO_ADMIN =
    "redirect:/administrarTorneoAdmin/";
  private static final String REDIRECT_FORMULARIO_TORNEO = "redirect:/formulario-torneo";

  private static final Logger LOGGER = Logger.getLogger(ControladorTorneo.class.getName());
  private static final String EQUIPOS = "equipos";

  private static final String VISTA_FORMULARIO_EDITAR_EQUIPO = "formulario-editar-equipo";
  private static final String ATRIBUTO_EQUIPO = "equipo";

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

      return new ModelAndView(REDIRECT_FORMULARIO_TORNEO);
    }

    if (torneo.getFechaInicio() == null || torneo.getFechaInicio().isBefore(LocalDateTime.now())) {
      redirectAttributes.addFlashAttribute(
        ERROR,
        "No puedes iniciar un torneo con una fecha anterior a la actual"
      );
      return new ModelAndView(REDIRECT_FORMULARIO_TORNEO);
    }

    try {
      servicioTorneo.registrarTorneo(torneo);
    } catch (TorneoExistente e) {
      redirectAttributes.addFlashAttribute(ERROR, "Ya existe un torneo con este nombre");

      return new ModelAndView(REDIRECT_FORMULARIO_TORNEO);
    } catch (Exception e) {
      redirectAttributes.addFlashAttribute(ERROR, "Error al crear el torneo");
      return new ModelAndView(REDIRECT_FORMULARIO_TORNEO);
    }

    return new ModelAndView(REDIRECT_CREACION_EQUIPO + torneo.getId());
  }

  @RequestMapping(path = "/creacion-equipo-admin", method = RequestMethod.GET)
  public ModelAndView crearEquipoAdmin(
    @RequestParam(ID_TORNEO) Long id,
    RedirectAttributes redirectAttributes
  ) throws FechaLimiteEditarTorneo {
    try {
      validarModificacionTorneo(id);
    } catch (FechaLimiteEditarTorneo e) {
      redirectAttributes.addFlashAttribute(ERROR, e.getMessage());
      return new ModelAndView(REDIRECT_ADMINISTRAR_TORNEO_ADMIN + id);
    }
    return new ModelAndView("creacion-equipo-admin", obtenerModeloCreacionEquipo(id));
  }

  @RequestMapping(path = "/creacion-equipo", method = RequestMethod.GET)
  public ModelAndView crearEquipo(@RequestParam(ID_TORNEO) Long id) {
    return new ModelAndView("creacion-equipo", obtenerModeloCreacionEquipo(id));
  }

  private Map<String, Object> obtenerModeloCreacionEquipo(Long id) {
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

    model.put("idTorneo", id);
    model.put("cantidadEquiposActuales", cantidadEquiposActuales);
    model.put("cantidadEquiposNecesarios", cantidadEquiposNecesarios);
    model.put("minJugadores", minJugadores);
    model.put("maxJugadores", maxJugadores);

    return model;
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
    @RequestParam("siglas") String siglas,
    @RequestParam(value = "escudoRuta", required = false) String escudoRuta,
    RedirectAttributes redirectAttributes
  ) {
    return obtenerModeloGuardarEquipo(
      idTorneo,
      nombreEquipo,
      colorLocal1,
      colorLocal2,
      colorVisitante1,
      colorVisitante2,
      siglas,
      escudoRuta,
      redirectAttributes,
      REDIRECT_CREACION_EQUIPO + idTorneo
    );
  }

  @RequestMapping(path = "/guardarEquipoAdmin", method = RequestMethod.POST)
  public ModelAndView guardarEquipoAdmin(
    @RequestParam(ID_TORNEO) Long idTorneo,
    @RequestParam("nombreEquipo") String nombreEquipo,
    @RequestParam("colorLocal1") String colorLocal1,
    @RequestParam("colorLocal2") String colorLocal2,
    @RequestParam("colorVisitante1") String colorVisitante1,
    @RequestParam("colorVisitante2") String colorVisitante2,
    @RequestParam("siglas") String siglas,
    @RequestParam(value = "escudoRuta", required = false) String escudoRuta,
    RedirectAttributes redirectAttributes
  ) {
    return obtenerModeloGuardarEquipo(
      idTorneo,
      nombreEquipo,
      colorLocal1,
      colorLocal2,
      colorVisitante1,
      colorVisitante2,
      siglas,
      escudoRuta,
      redirectAttributes,
      REDIRECT_CREACION_EQUIPO_ADMIN + idTorneo
    );
  }

  private ModelAndView obtenerModeloGuardarEquipo(
    Long idTorneo,
    String nombreEquipo,
    String colorLocal1,
    String colorLocal2,
    String colorVisitante1,
    String colorVisitante2,
    String siglas,
    String escudoRuta,
    RedirectAttributes redirectAttributes,
    String redireccion
  ) {
    Torneo torneo = servicioTorneo.consultarTorneoPorId(idTorneo);

    int cantidadActual = servicioEquipoTorneo.contarEquiposPorTorneo(idTorneo);

    int cantidadNecesaria = torneo.getCantidadDeEquipos();

    if (cantidadActual >= cantidadNecesaria) {
      return new ModelAndView(redireccion);
    }

    Equipo equipo = new Equipo();
    equipo.setNombre(nombreEquipo);
    equipo.setColorLocal1(colorLocal1);
    equipo.setColorLocal2(colorLocal2);
    equipo.setColorVisitante1(colorVisitante1);
    equipo.setColorVisitante2(colorVisitante2);
    equipo.setSiglas(siglas);

    if (escudoRuta != null && !escudoRuta.isBlank()) {
      if (!escudoRuta.startsWith("/img/escudos/")) {
        redirectAttributes.addFlashAttribute(ERROR, "La ruta del escudo no es válida.");
        return new ModelAndView(redireccion);
      }

      equipo.setEscudo(escudoRuta);
    }

    try {
      servicioEquipoTorneo.crearEquipoEInscribirlo(equipo, torneo);
    } catch (EquipoExistente e) {
      redirectAttributes.addFlashAttribute(ERROR, "Ya existe un equipo con ese nombre");
    } catch (SiglasExistentes e) {
      redirectAttributes.addFlashAttribute(ERROR, "Ya existe un equipo con esas siglas");
    }

    return new ModelAndView(redireccion);
  }

  @RequestMapping(path = "/lista-equipos", method = RequestMethod.GET)
  public ModelAndView listarEquipos(@RequestParam(ID_TORNEO) Long idTorneo) {
    return listarEquiposComun(idTorneo, "lista-equipos");
  }

  @RequestMapping(path = "/lista-equipos-admin", method = RequestMethod.GET)
  public ModelAndView listarEquiposAdmin(@RequestParam(ID_TORNEO) Long idTorneo) {
    return listarEquiposComun(idTorneo, "lista-equipos-admin");
  }

  private ModelAndView listarEquiposComun(Long idTorneo, String vista) {
    Map<String, Object> model = new HashMap<>();

    List<Equipo> equipos = servicioEquipo.obtenerEquipos();

    model.put(EQUIPOS, equipos);
    model.put(ID_TORNEO, idTorneo);

    return new ModelAndView(vista, model);
  }

  @RequestMapping(path = "/cargar-equipo-existente", method = RequestMethod.POST)
  public ModelAndView cargarEquipoExistente(
    @RequestParam(ID_EQUIPO) Long idEquipo,
    @RequestParam(ID_TORNEO) Long idTorneo,
    RedirectAttributes redirectAttributes
  ) {
    return cargarEquipoExistenteComun(
      idEquipo,
      idTorneo,
      redirectAttributes,
      REDIRECT_CREACION_EQUIPO + idTorneo
    );
  }

  @RequestMapping(path = "/cargar-equipo-existente-admin", method = RequestMethod.POST)
  public ModelAndView cargarEquipoExistenteAdmin(
    @RequestParam(ID_EQUIPO) Long idEquipo,
    @RequestParam(ID_TORNEO) Long idTorneo,
    RedirectAttributes redirectAttributes
  ) {
    return cargarEquipoExistenteComun(
      idEquipo,
      idTorneo,
      redirectAttributes,
      REDIRECT_CREACION_EQUIPO_ADMIN + idTorneo
    );
  }

  private ModelAndView cargarEquipoExistenteComun(
    Long idEquipo,
    Long idTorneo,
    RedirectAttributes redirectAttributes,
    String redireccion
  ) {
    Equipo equipo = servicioEquipo.consultarEquipoPorId(idEquipo);
    Torneo torneo = servicioTorneo.consultarTorneoPorId(idTorneo);

    try {
      servicioEquipoTorneo.inscribirEquipoExistente(equipo, torneo);
    } catch (EquipoYaInscripto e) {
      redirectAttributes.addFlashAttribute(ERROR, "El equipo ya está inscripto en este torneo");
    }

    return new ModelAndView(redireccion);
  }

  @RequestMapping(path = "/finalizarTorneo", method = RequestMethod.POST)
  public ModelAndView finalizarTorneo(
    @RequestParam(ID_TORNEO) Long idTorneo,
    RedirectAttributes redirectAttributes
  ) {
    return finalizarTorneoComun(idTorneo, redirectAttributes, REDIRECT_LOBBY_ADMIN);
  }

  @RequestMapping(path = "/finalizarTorneoAdmin", method = RequestMethod.POST)
  public ModelAndView finalizarTorneoAdmin(
    @RequestParam(ID_TORNEO) Long idTorneo,
    RedirectAttributes redirectAttributes
  ) {
    return finalizarTorneoComun(
      idTorneo,
      redirectAttributes,
      REDIRECT_ADMINISTRAR_TORNEO_ADMIN + idTorneo
    );
  }

  private ModelAndView finalizarTorneoComun(
    Long idTorneo,
    RedirectAttributes redirectAttributes,
    String redireccion
  ) {
    Torneo torneo = servicioTorneo.consultarTorneoPorId(idTorneo);

    if (torneo == null) {
      redirectAttributes.addFlashAttribute(ERROR, "No se encontró el torneo.");
      return new ModelAndView(redireccion);
    }

    try {
      List<EquipoTorneo> inscripciones = servicioEquipoTorneo.obtenerEquiposPorTorneo(idTorneo);

      List<Long> idsEquipos = inscripciones
        .stream()
        .map(inscripcion -> inscripcion.getEquipo().getId())
        .toList();

      if (idsEquipos.size() != torneo.getCantidadDeEquipos()) {
        redirectAttributes.addFlashAttribute(
          ERROR,
          "La cantidad de equipos inscriptos no coincide con la configurada."
        );
        return new ModelAndView(redireccion);
      }

      List<Jornada> jornadasExistentes = servicioTorneo.obtenerJornadasPorTorneo(idTorneo);

      if (
        jornadasExistentes == null ||
        jornadasExistentes.isEmpty() ||
        jornadasExistentes.stream().allMatch(jornada -> jornada.getPartidos().isEmpty())
      ) {
        servicioTorneo.crearTorneoConFixture(torneo, idsEquipos);
      }

      return new ModelAndView(redireccion);
    } catch (Exception e) {
      redirectAttributes.addFlashAttribute(
        ERROR,
        "No se pudo generar el fixture: " + e.getMessage()
      );

      return new ModelAndView(redireccion);
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

  @RequestMapping(path = "/desvincular-equipo", method = RequestMethod.GET)
  public ModelAndView mostrarEquiposParaDesvincular(
    @RequestParam(ID_TORNEO) Long idTorneo,
    RedirectAttributes redirectAttributes
  ) throws FechaLimiteEditarTorneo {
    try {
      validarModificacionTorneo(idTorneo);
    } catch (FechaLimiteEditarTorneo e) {
      redirectAttributes.addFlashAttribute(ERROR, e.getMessage());
      return new ModelAndView(REDIRECT_ADMINISTRAR_TORNEO_ADMIN + idTorneo);
    }
    Map<String, Object> model = new HashMap<>();

    List<EquipoTorneo> equipos = servicioEquipoTorneo.obtenerEquiposPorTorneo(idTorneo);

    model.put("idTorneo", idTorneo);
    model.put(EQUIPOS, equipos);

    return new ModelAndView("desvincularEquipo", model);
  }

  @RequestMapping(path = "/desvincular-equipo", method = RequestMethod.POST)
  public ModelAndView desvincularEquipo(
    @RequestParam(ID_EQUIPO) Long idEquipo,
    @RequestParam(ID_TORNEO) Long idTorneo,
    RedirectAttributes redirectAttributes
  ) {
    servicioEquipoTorneo.desvincularEquipo(idEquipo, idTorneo);

    redirectAttributes.addFlashAttribute("mensaje", "Equipo desvinculado correctamente");

    return new ModelAndView("redirect:/desvincular-equipo?idTorneo=" + idTorneo);
  }

  @RequestMapping(path = "/editar-equipo", method = RequestMethod.GET)
  public ModelAndView mostrarEquiposParaEditar(
    @RequestParam(ID_TORNEO) Long idTorneo,
    RedirectAttributes redirectAttributes
  ) throws FechaLimiteEditarTorneo {
    try {
      validarModificacionTorneo(idTorneo);
    } catch (FechaLimiteEditarTorneo e) {
      redirectAttributes.addFlashAttribute(ERROR, e.getMessage());
      return new ModelAndView(REDIRECT_ADMINISTRAR_TORNEO_ADMIN + idTorneo);
    }
    ModelAndView mav = new ModelAndView("editarEquipo");

    mav.addObject(ID_TORNEO, idTorneo);

    mav.addObject(EQUIPOS, servicioEquipoTorneo.obtenerEquiposPorTorneo(idTorneo));

    return mav;
  }

  @RequestMapping(path = "/formulario-editar-equipo", method = RequestMethod.GET)
  public ModelAndView mostrarFormularioEditarEquipo(
    @RequestParam(ID_EQUIPO) Long idEquipo,
    @RequestParam(ID_TORNEO) Long idTorneo
  ) {
    Equipo equipo = servicioEquipo.consultarEquipoPorId(idEquipo);

    if (equipo == null) {
      return new ModelAndView("redirect:/editarEquipo?idTorneo=" + idTorneo);
    }

    ModelAndView mav = new ModelAndView(VISTA_FORMULARIO_EDITAR_EQUIPO);

    mav.addObject(ATRIBUTO_EQUIPO, equipo);
    mav.addObject(ID_TORNEO, idTorneo);

    return mav;
  }

  @RequestMapping(path = "/actualizar-equipo", method = RequestMethod.POST)
  public ModelAndView actualizarEquipo(
    @RequestParam(ID_EQUIPO) Long idEquipo,
    @RequestParam(ID_TORNEO) Long idTorneo,
    @RequestParam("nombre") String nombre,
    @RequestParam("siglas") String siglas,
    @RequestParam("colorLocal1") String colorLocal1,
    @RequestParam("colorLocal2") String colorLocal2,
    @RequestParam("colorVisitante1") String colorVisitante1,
    @RequestParam("colorVisitante2") String colorVisitante2,
    @RequestParam(value = "escudoRuta", required = false) String escudoRuta
  ) {
    Equipo equipo = servicioEquipo.consultarEquipoPorId(idEquipo);

    if (equipo == null) {
      return new ModelAndView("redirect:/editar-equipo?idTorneo=" + idTorneo);
    }

    equipo.setNombre(nombre);
    equipo.setSiglas(siglas);
    equipo.setColorLocal1(colorLocal1);
    equipo.setColorLocal2(colorLocal2);
    equipo.setColorVisitante1(colorVisitante1);
    equipo.setColorVisitante2(colorVisitante2);

    if (escudoRuta != null && !escudoRuta.isBlank()) {
      if (!escudoRuta.startsWith("/img/escudos/")) {
        ModelAndView mav = new ModelAndView(VISTA_FORMULARIO_EDITAR_EQUIPO);
        mav.addObject(ATRIBUTO_EQUIPO, equipo);
        mav.addObject(ID_TORNEO, idTorneo);
        mav.addObject(ERROR, "La ruta del escudo no es válida.");
        return mav;
      }

      equipo.setEscudo(escudoRuta);
    }

    try {
      servicioEquipo.actualizarEquipo(equipo);
    } catch (EquipoExistente e) {
      ModelAndView mav = new ModelAndView(VISTA_FORMULARIO_EDITAR_EQUIPO);
      mav.addObject(ATRIBUTO_EQUIPO, equipo);
      mav.addObject(ID_TORNEO, idTorneo);
      mav.addObject(ERROR, "Ya existe un equipo con ese nombre.");
      return mav;
    } catch (SiglasExistentes e) {
      ModelAndView mav = new ModelAndView("formulario-editar-equipo");
      mav.addObject(ATRIBUTO_EQUIPO, equipo);
      mav.addObject(ID_TORNEO, idTorneo);
      mav.addObject(ERROR, "Ya existen equipos con esas siglas.");
      return mav;
    }

    return new ModelAndView("redirect:/administrarTorneoAdmin/" + idTorneo);
  }

  private void validarModificacionTorneo(Long idTorneo) throws FechaLimiteEditarTorneo {
    Torneo torneo = servicioTorneo.consultarTorneoPorId(idTorneo);
    if (torneo != null && !torneo.permiteModificaciones()) {
      throw new FechaLimiteEditarTorneo(
        "La fecha actual es mayor a la permitida para hacer cambios en el torneo."
      );
    }
  }

  @RequestMapping(path = "/eliminarTorneo", method = RequestMethod.GET)
  public ModelAndView eliminarTorneoAdmin(
    @RequestParam(ID_TORNEO) Long idTorneo,
    RedirectAttributes redirectAttributes
  ) {
    try {
      validarModificacionTorneo(idTorneo);
      servicioTorneo.eliminarTorneo(idTorneo);
      redirectAttributes.addFlashAttribute("mensaje", "Torneo eliminado correctamente.");
      return new ModelAndView(REDIRECT_LOBBY_ADMIN);
    } catch (FechaLimiteEditarTorneo e) {
      redirectAttributes.addFlashAttribute(ERROR, e.getMessage());
      return new ModelAndView(REDIRECT_ADMINISTRAR_TORNEO_ADMIN + idTorneo);
    } catch (Exception e) {
      redirectAttributes.addFlashAttribute(
        ERROR,
        "No se pudo eliminar el torneo: " + e.getMessage()
      );
      return new ModelAndView(REDIRECT_ADMINISTRAR_TORNEO_ADMIN + idTorneo);
    }
  }

  @RequestMapping("/elegirEquipoSancion")
  public ModelAndView elegirEquipoSancion(@RequestParam(ID_TORNEO) Long idTorneo) {
    Map<String, Object> modelo = new HashMap<>();

    modelo.put(EQUIPOS, servicioEquipoTorneo.obtenerEquiposPorTorneo(idTorneo));

    modelo.put(ID_TORNEO, idTorneo);

    return new ModelAndView("elegirEquipoSancion", modelo);
  }
}
