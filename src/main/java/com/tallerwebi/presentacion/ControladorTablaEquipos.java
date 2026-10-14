package com.tallerwebi.presentacion;

import com.tallerwebi.dominio.EquipoTorneo;
import com.tallerwebi.dominio.ServicioEquipoTorneo;
import com.tallerwebi.dominio.ServicioTorneo;
import com.tallerwebi.dominio.Torneo;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class ControladorTablaEquipos {

  private final ServicioEquipoTorneo servicioEquipoTorneo;
  private final ServicioTorneo servicioTorneo;

  public ControladorTablaEquipos(
    ServicioEquipoTorneo servicioEquipoTorneo,
    ServicioTorneo servicioTorneo
  ) {
    this.servicioEquipoTorneo = servicioEquipoTorneo;
    this.servicioTorneo = servicioTorneo;
  }

  @RequestMapping("/tablaEquipos")
  public ModelAndView irATablaEquipos(
    @RequestParam(value = "idTorneo", required = false) Long idTorneo
  ) {
    Map<String, Object> model = new ModelMap();

    Torneo torneo = servicioTorneo.consultarTorneoPorId(idTorneo);

    List<EquipoTorneo> tabla = servicioEquipoTorneo.obtenerEstadisticas(idTorneo);

    model.put("torneo", torneo);
    model.put("tablaPosiciones", tabla);

    return new ModelAndView("tablaEquipos", model);
  }
}
