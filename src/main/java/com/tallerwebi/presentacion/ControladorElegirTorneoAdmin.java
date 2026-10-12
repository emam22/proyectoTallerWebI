package com.tallerwebi.presentacion;

import com.tallerwebi.dominio.ServicioTorneo;
import com.tallerwebi.dominio.Torneo;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class ControladorElegirTorneoAdmin {

  private ServicioTorneo servicioTorneo;

  public ControladorElegirTorneoAdmin(ServicioTorneo servicioTorneo) {
    this.servicioTorneo = servicioTorneo;
  }

  @RequestMapping(path = "/elegirTorneoAdmin", method = RequestMethod.GET)
  public ModelAndView irAAdministrarTorneo() {
    Map<String, Object> model = new HashMap<>();
    List<Torneo> torneos = servicioTorneo.obtenerTorneos();
    model.put("torneos", torneos);

    return new ModelAndView("elegirTorneoAdmin", model);
  }
}
