package com.tallerwebi.presentacion;

import com.tallerwebi.dominio.*;
import com.tallerwebi.dominio.excepcion.*;
import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
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
  private static final String TORNEO_CARGADO = "torneoCargado";
  private static final String EQUIPO_CARGADO = "equipoCargado";
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

  @ModelAttribute(TORNEO_CARGADO)
  public Torneo cargarTorneo(@RequestParam(value = ID_TORNEO, required = false) Long idTorneo) {
    return idTorneo == null ? null : servicioTorneo.consultarTorneoPorId(idTorneo);
  }

  @ModelAttribute(EQUIPO_CARGADO)
  public Equipo cargarEquipo(@RequestParam(value = ID_EQUIPO, required = false) Long idEquipo) {
    return idEquipo == null ? null : servicioEquipo.consultarEquipoPorId(idEquipo);
  }

  @RequestMapping(path = "/formulario-torneo", method = RequestMethod.GET)
  public ModelAndView irAFormularioTorneo() {
    Map<String, Object> model = new HashMap<>();
    model.put(TORNEO, new Torneo());
    return new ModelAndView("formulario-torneo", model);
  }

  @RequestMapping(path = "/crearTorneo", method = RequestMethod.POST)
  public ModelAndView crearTorneo(
    @ModelAttribute(TORNEO) Torneo torneo,
    RedirectAttributes redirectAttributes
  ) {
    try {
      servicioTorneo.registrarTorneo(torneo);

      return new ModelAndView(REDIRECT_CREACION_EQUIPO + torneo.getId());
    } catch (TorneoExistente e) {
      redirectAttributes.addFlashAttribute(ERROR, "Ya existe un torneo con este nombre");
    } catch (IllegalArgumentException e) {
      redirectAttributes.addFlashAttribute(ERROR, e.getMessage());
    } catch (Exception e) {
      redirectAttributes.addFlashAttribute(ERROR, "Error al crear el torneo");
    }

    return new ModelAndView(REDIRECT_FORMULARIO_TORNEO);
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

  @RequestMapping(path = "/guardarEquipo", method = RequestMethod.POST)
  public ModelAndView guardarEquipo(
    @ModelAttribute(TORNEO_CARGADO) Torneo torneo,
    @ModelAttribute("equipoForm") EquipoForm equipoForm,
    RedirectAttributes redirectAttributes
  ) {
    return guardarEquipoComun(torneo, equipoForm, redirectAttributes, REDIRECT_CREACION_EQUIPO);
  }

  @RequestMapping(path = "/guardarEquipoAdmin", method = RequestMethod.POST)
  public ModelAndView guardarEquipoAdmin(
    @ModelAttribute(TORNEO_CARGADO) Torneo torneo,
    @ModelAttribute("equipoForm") EquipoForm equipoForm,
    RedirectAttributes redirectAttributes
  ) {
    return guardarEquipoComun(
      torneo,
      equipoForm,
      redirectAttributes,
      REDIRECT_CREACION_EQUIPO_ADMIN
    );
  }

  private ModelAndView guardarEquipoComun(
    Torneo torneo,
    EquipoForm equipoForm,
    RedirectAttributes redirectAttributes,
    String prefijoRedireccion
  ) {
    Long idTorneo = obtenerIdTorneo(torneo);
    String redireccion = prefijoRedireccion + idTorneo;

    if (torneo == null) {
      redirectAttributes.addFlashAttribute(ERROR, "No se encontró el torneo.");
      return new ModelAndView(redireccion);
    }

    int cantidadActual = servicioEquipoTorneo.contarEquiposPorTorneo(idTorneo);

    int cantidadNecesaria = torneo.getCantidadDeEquipos();

    if (cantidadActual >= cantidadNecesaria) {
      return new ModelAndView(redireccion);
    }

    Equipo equipo = new Equipo();
    equipo.setNombre(equipoForm.getNombreEquipo());
    equipo.setColorLocal1(equipoForm.getColorLocal1());
    equipo.setColorLocal2(equipoForm.getColorLocal2());
    equipo.setColorVisitante1(equipoForm.getColorVisitante1());
    equipo.setColorVisitante2(equipoForm.getColorVisitante2());
    equipo.setSigla(equipoForm.getSigla());

    String escudoRuta = equipoForm.getEscudoRuta();

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
    } catch (SiglaExistente e) {
      redirectAttributes.addFlashAttribute(ERROR, "Ya existen equipos con esa sigla");
    } catch (SiglaInvalida e) {
      redirectAttributes.addFlashAttribute(ERROR, "La cantidad de letras de la sigla no es válida");
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
    @ModelAttribute(EQUIPO_CARGADO) Equipo equipo,
    @ModelAttribute(TORNEO_CARGADO) Torneo torneo,
    RedirectAttributes redirectAttributes
  ) {
    Long idTorneo = obtenerIdTorneo(torneo);

    return cargarEquipoExistenteComun(
      equipo,
      torneo,
      redirectAttributes,
      REDIRECT_CREACION_EQUIPO + idTorneo
    );
  }

  @RequestMapping(path = "/cargar-equipo-existente-admin", method = RequestMethod.POST)
  public ModelAndView cargarEquipoExistenteAdmin(
    @ModelAttribute(EQUIPO_CARGADO) Equipo equipo,
    @ModelAttribute(TORNEO_CARGADO) Torneo torneo,
    RedirectAttributes redirectAttributes
  ) {
    Long idTorneo = obtenerIdTorneo(torneo);

    return cargarEquipoExistenteComun(
      equipo,
      torneo,
      redirectAttributes,
      REDIRECT_CREACION_EQUIPO_ADMIN + idTorneo
    );
  }

  private ModelAndView cargarEquipoExistenteComun(
    Equipo equipo,
    Torneo torneo,
    RedirectAttributes redirectAttributes,
    String redireccion
  ) {
    if (equipo == null || torneo == null) {
      redirectAttributes.addFlashAttribute(ERROR, "No se encontró el equipo o el torneo.");
      return new ModelAndView(redireccion);
    }

    try {
      servicioEquipoTorneo.inscribirEquipoExistente(equipo, torneo);
    } catch (EquipoYaInscripto e) {
      redirectAttributes.addFlashAttribute(ERROR, "El equipo ya está inscripto en este torneo");
    }

    return new ModelAndView(redireccion);
  }

  @RequestMapping(path = "/finalizarTorneo", method = RequestMethod.POST)
  public ModelAndView finalizarTorneo(
    @ModelAttribute(TORNEO_CARGADO) Torneo torneo,
    RedirectAttributes redirectAttributes
  ) {
    return finalizarTorneoComun(torneo, redirectAttributes, REDIRECT_LOBBY_ADMIN);
  }

  @RequestMapping(path = "/finalizarTorneoAdmin", method = RequestMethod.POST)
  public ModelAndView finalizarTorneoAdmin(
    @ModelAttribute(TORNEO_CARGADO) Torneo torneo,
    RedirectAttributes redirectAttributes
  ) {
    Long idTorneo = obtenerIdTorneo(torneo);

    return finalizarTorneoComun(
      torneo,
      redirectAttributes,
      REDIRECT_ADMINISTRAR_TORNEO_ADMIN + idTorneo
    );
  }

  private ModelAndView finalizarTorneoComun(
    Torneo torneo,
    RedirectAttributes redirectAttributes,
    String redireccion
  ) {
    if (torneo == null) {
      redirectAttributes.addFlashAttribute(ERROR, "No se encontró el torneo.");
      return new ModelAndView(redireccion);
    }

    try {
      servicioTorneo.finalizarTorneo(torneo.getId());

      return new ModelAndView(redireccion);
    } catch (IllegalArgumentException e) {
      redirectAttributes.addFlashAttribute(ERROR, e.getMessage());
    } catch (Exception e) {
      redirectAttributes.addFlashAttribute(
        ERROR,
        "No se pudo generar el fixture: " + e.getMessage()
      );
    }

    return new ModelAndView(redireccion);
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
    @ModelAttribute(EQUIPO_CARGADO) Equipo equipo,
    @ModelAttribute(TORNEO_CARGADO) Torneo torneo,
    RedirectAttributes redirectAttributes
  ) {
    Long idEquipo = obtenerIdEquipo(equipo);
    Long idTorneo = obtenerIdTorneo(torneo);

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
    @ModelAttribute(EQUIPO_CARGADO) Equipo equipo,
    @ModelAttribute(TORNEO_CARGADO) Torneo torneo,
    @RequestParam("nombre") String nombre,
    @RequestParam("sigla") String sigla,
    @RequestParam("colorLocal1") String colorLocal1,
    @RequestParam("colorLocal2") String colorLocal2,
    @RequestParam("colorVisitante1") String colorVisitante1,
    @RequestParam("colorVisitante2") String colorVisitante2,
    @RequestParam(value = "escudoRuta", required = false) String escudoRuta
  ) {
    Long idTorneo = obtenerIdTorneo(torneo);

    if (equipo == null) {
      return new ModelAndView("redirect:/editar-equipo?idTorneo=" + idTorneo);
    }

    equipo.setNombre(nombre);
    equipo.setSigla(sigla);
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
    } catch (SiglaExistente e) {
      ModelAndView mav = new ModelAndView("formulario-editar-equipo");
      mav.addObject(ATRIBUTO_EQUIPO, equipo);
      mav.addObject(ID_TORNEO, idTorneo);
      mav.addObject(ERROR, "Ya existen equipos con esa sigla.");
      return mav;
    } catch (SiglaInvalida e) {
      ModelAndView mav = new ModelAndView(VISTA_FORMULARIO_EDITAR_EQUIPO);
      mav.addObject(ATRIBUTO_EQUIPO, equipo);
      mav.addObject(ID_TORNEO, idTorneo);
      mav.addObject(ERROR, "La cantidad de letras de la sigla no es válida");
      return mav;
    }

    return new ModelAndView("redirect:/administrarTorneoAdmin/" + idTorneo);
  }

  private Long obtenerIdTorneo(Torneo torneo) {
    return torneo == null ? null : torneo.getId();
  }

  private Long obtenerIdEquipo(Equipo equipo) {
    return equipo == null ? null : equipo.getId();
  }

  private void validarModificacionTorneo(Long idTorneo) throws FechaLimiteEditarTorneo {
    validarModificacionTorneo(servicioTorneo.consultarTorneoPorId(idTorneo));
  }

  private void validarModificacionTorneo(Torneo torneo) throws FechaLimiteEditarTorneo {
    if (torneo != null && !torneo.permiteModificaciones()) {
      throw new FechaLimiteEditarTorneo(
        "La fecha actual es mayor a la permitida para hacer cambios en el torneo."
      );
    }
  }

  @RequestMapping(path = "/eliminarTorneo", method = RequestMethod.GET)
  public ModelAndView eliminarTorneoAdmin(
    @ModelAttribute(TORNEO_CARGADO) Torneo torneo,
    RedirectAttributes redirectAttributes
  ) {
    Long idTorneo = obtenerIdTorneo(torneo);

    try {
      validarModificacionTorneo(torneo);
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

  // Sube el escudo de un equipo.
  @ResponseBody
  @PostMapping(value = "/subirEscudo", consumes = "application/octet-stream")
  public ResponseEntity<String> subirEscudo(
    @RequestBody byte[] contenido,
    @RequestParam("nombre") String nombreOriginal,
    HttpServletRequest request
  ) {
    String rutaReal = request.getServletContext().getRealPath("/resources/core/img/escudos");

    // Verificamos que el servidor haya podido resolver el directorio.
    if (rutaReal == null) {
      return ResponseEntity
        .internalServerError()
        .body("No se encontró el directorio para guardar los escudos.");
    }

    try {
      // Delegamos la validación y el guardado al servicio.
      String ruta = servicioEquipo.subirEscudo(contenido, nombreOriginal, rutaReal);

      return ResponseEntity.ok(ruta);
    } catch (IllegalArgumentException e) {
      // Error de validación: imagen vacía, formato incorrecto, tamaño, etc.
      return ResponseEntity.badRequest().body(e.getMessage());
    } catch (IOException e) {
      // Error al crear el directorio o guardar el archivo.
      return ResponseEntity.internalServerError().body("No se pudo guardar el escudo.");
    }
  }
}
