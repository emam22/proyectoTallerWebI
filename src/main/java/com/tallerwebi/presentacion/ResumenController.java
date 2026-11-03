package com.tallerwebi.presentacion;

import com.tallerwebi.dominio.ServicioTorneo;
import com.tallerwebi.dominio.Torneo;
import java.util.HashMap;
import java.util.Map;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class ResumenController {

  private final ServicioTorneo servicioTorneo;

  public ResumenController(ServicioTorneo servicioTorneo) {
    this.servicioTorneo = servicioTorneo;
  }

  @ModelAttribute("torneo")
  public Torneo cargarTorneo(@RequestParam(value = "idTorneo", required = false) Long idTorneo) {
    return idTorneo == null ? null : servicioTorneo.consultarTorneoPorId(idTorneo);
  }

  @RequestMapping("/resumen")
  public ModelAndView irAResumen(@ModelAttribute("torneo") Torneo torneo) {
    Map<String, Object> model = new HashMap<>();

    Long idTorneoActual = torneo == null ? null : torneo.getId();
    model.put("idTorneoActual", idTorneoActual);

    return new ModelAndView("resumen", model);
  }
}
