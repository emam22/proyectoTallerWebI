package com.tallerwebi.presentacion;

import java.util.HashMap;
import java.util.Map;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class EstadisticasDeJugadorController {

  @RequestMapping("/estadisticasDeJugadores")
  public ModelAndView irAEstadisticasDeJugadores() {
    Map<String, Object> model = new HashMap<>();
    return new ModelAndView("estadisticasDeJugadores", model);
  }
}
