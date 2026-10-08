package com.tallerwebi.presentacion;

import com.tallerwebi.dominio.EquipoTorneo;
import com.tallerwebi.dominio.ServicioEquipoTorneo;
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

  public ControladorTablaEquipos(ServicioEquipoTorneo servicioEquipoTorneo) {
    this.servicioEquipoTorneo = servicioEquipoTorneo;
  }

  @RequestMapping("/tablaEquipos")
  public ModelAndView irATablaEquipos(
    @RequestParam(value = "idTorneo", required = false) Long idTorneo
  ) {
    Map<String, Object> model = new ModelMap();

    Long idTorneoABuscar = 1L;
    if (idTorneo != null) {
      idTorneoABuscar = idTorneo;
    }

    List<EquipoTorneo> tabla = servicioEquipoTorneo.obtenerEstadisticas(idTorneoABuscar);
    model.put("tablaPosiciones", tabla);

    return new ModelAndView("tablaEquipos", model);
  }
}
