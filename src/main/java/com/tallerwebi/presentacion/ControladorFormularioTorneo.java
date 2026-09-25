package com.tallerwebi.presentacion;

import com.tallerwebi.dominio.Torneo;
import java.util.HashMap;
import java.util.Map;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class ControladorFormularioTorneo {

  @RequestMapping(path = "/formulario-torneo", method = RequestMethod.GET)
  public ModelAndView FormularioTorneo() {
    Map<String, Object> model = new HashMap<>();
    model.put("torneo", new Torneo());
    return new ModelAndView("formulario-torneo", model);
  }
}