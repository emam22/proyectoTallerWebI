package com.tallerwebi.presentacion;

import java.util.HashMap;
import java.util.Map;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class ResumenController {

  @RequestMapping("/resumen")
  public ModelAndView irAResumen() {
    Map<String, Object> model = new ModelMap();
    return new ModelAndView("resumen", model);
  }
}
