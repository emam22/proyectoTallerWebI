package com.tallerwebi.presentacion;

import java.util.Map;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class ControladorAdministrarTorneo {

  @RequestMapping("/administrarTorneo")
  public ModelAndView irALobbyAdmin() {
    Map<String, Object> model = new ModelMap();
    model.put("prueba", "prueba");
    return new ModelAndView("administrarTorneo", model);
  }
}
