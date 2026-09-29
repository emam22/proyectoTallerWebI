package com.tallerwebi.presentacion;

import com.tallerwebi.dominio.ServicioTorneo;
import com.tallerwebi.dominio.Torneo;
import com.tallerwebi.dominio.excepcion.TorneoExistente;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class ControladorTorneo {

  private ServicioTorneo servicioTorneo;

  @Autowired
  public ControladorTorneo(ServicioTorneo servicioTorneo) {
    this.servicioTorneo = servicioTorneo;
  }

  @RequestMapping(path = "/crearTorneo", method = RequestMethod.POST)
  public ModelAndView crearTorneo(@ModelAttribute("torneo") Torneo torneo) {
    Map<String, Object> model = new ModelMap();
    try {
      servicioTorneo.registrarTorneo(torneo);
    } catch (TorneoExistente e) {
      model.put("error", "Ya existe un torneo con este nombre");
      model.put("torneo", torneo);
      return new ModelAndView("formulario-torneo", model);
    } catch (Exception e) {
      model.put("error", "Error al crear el torneo");
      model.put("torneo", torneo);
      return new ModelAndView("formulario-torneo", model);
    }
    return new ModelAndView("redirect:/creacion-equipo");
  }

  @RequestMapping(path = "/creacion-equipo", method = RequestMethod.GET)
  public ModelAndView crearEquipo() {
    Map<String, Object> model = new HashMap<>();
    Torneo torneoEnCurso = servicioTorneo
      .obtenerTorneos()
      .stream()
      .max(Comparator.comparing(Torneo::getId))
      .orElse(null);

    int minJugadores = 11;
    int maxJugadores = 23;
    if (torneoEnCurso != null) {
      if (torneoEnCurso.getCantidadMinJugadores() != null) {
        minJugadores = torneoEnCurso.getCantidadMinJugadores();
      }
      if (torneoEnCurso.getCantidadMaxJugadores() != null) {
        maxJugadores = torneoEnCurso.getCantidadMaxJugadores();
      }
    }
    model.put("minJugadores", minJugadores);
    model.put("maxJugadores", maxJugadores);
    return new ModelAndView("creacion-equipo", model);
  }
}
