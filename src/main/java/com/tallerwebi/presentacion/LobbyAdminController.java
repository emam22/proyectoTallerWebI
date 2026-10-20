package com.tallerwebi.presentacion;

import com.tallerwebi.dominio.*;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class LobbyAdminController {

  private final ServicioEquipoTorneo servicioEquipoTorneo;
  private final ServicioTorneo servicioTorneo;

  public LobbyAdminController(
    ServicioEquipoTorneo servicioEquipoTorneo,
    ServicioTorneo servicioTorneo
  ) {
    this.servicioEquipoTorneo = servicioEquipoTorneo;
    this.servicioTorneo = servicioTorneo;
  }

  @GetMapping("/lobbyAdmin")
  public ModelAndView irALobbyAdmin(
    @RequestParam(value = "idTorneo", required = false) Long idTorneo
  ) {
    Map<String, Object> model = new ModelMap();
    Long idTorneoABuscar = (idTorneo != null) ? idTorneo : 1L;

    List<EquipoTorneo> tabla = servicioEquipoTorneo.obtenerEstadisticas(idTorneoABuscar);
    model.put("tablaPosiciones", tabla);
    model.put("idTorneoActual", idTorneoABuscar);

    return new ModelAndView("lobbyAdmin", model);
  }

  @GetMapping("/administrarTorneoAdmin/{idTorneo}")
  public ModelAndView administrarTorneo(@PathVariable("idTorneo") Long idTorneo) {
    Map<String, Object> model = new ModelMap();

    Torneo torneo = servicioTorneo.consultarTorneoPorId(idTorneo);

    if (torneo == null) {
      return new ModelAndView("redirect:/lobbyAdmin");
    }

    model.put("torneo", torneo);

    List<EquipoTorneo> tabla = servicioEquipoTorneo.obtenerEstadisticas(idTorneo);
    model.put("tablaPosiciones", tabla);
    model.put("idTorneoActual", idTorneo);

    return new ModelAndView("administrarTorneoAdmin", model);
  }
}
