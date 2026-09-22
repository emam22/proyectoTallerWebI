package com.tallerwebi.presentacion;

import java.util.HashMap;
import java.util.Map;
import org.springframework.stereotype.Controller;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class ControladorCreacionEquipo {

  @RequestMapping("/creacion-equipo")
  public ModelAndView irALobbyAdmin() {
    Map<String, Object> model = new HashMap<>();
    return new ModelAndView("creacion-equipo", model);
  }
}
