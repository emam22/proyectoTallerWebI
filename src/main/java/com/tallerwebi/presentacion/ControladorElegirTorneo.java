package com.tallerwebi.presentacion;

import com.tallerwebi.dominio.ServicioEquipoTorneo;
import com.tallerwebi.dominio.ServicioTorneo;
import com.tallerwebi.dominio.Torneo;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class ControladorElegirTorneo {

  private ServicioTorneo servicioTorneo;

  public ControladorElegirTorneo(ServicioTorneo servicioTorneo) {
    this.servicioTorneo = servicioTorneo;
  }

  @RequestMapping("/elegirTorneo")
  public ModelAndView irAElegirTorneo() {
    Map<String, Object> model = new ModelMap();
    List<Torneo> torneos = servicioTorneo.obtenerTorneos();
    model.put("torneos", torneos);

    return new ModelAndView("elegirTorneo", model);
  }
}
