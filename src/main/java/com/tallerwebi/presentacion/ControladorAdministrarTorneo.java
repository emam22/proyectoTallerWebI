package com.tallerwebi.presentacion;

import com.tallerwebi.dominio.ServicioTorneo;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class ControladorAdministrarTorneo {

  private ServicioTorneo servicioTorneo;

  @Autowired
  public ControladorAdministrarTorneo(ServicioTorneo servicioTorneo) {
    this.servicioTorneo = servicioTorneo;
  }

  @RequestMapping("/administrarTorneo")
  public ModelAndView irAAdministrarTorneo() {
    Map<String, Object> model = new ModelMap();
    model.put("torneos", servicioTorneo.obtenerTorneos());
    return new ModelAndView("administrarTorneo", model);
  }
}
