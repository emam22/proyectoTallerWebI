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
public class LobbyController {

  private final ServicioEquipoTorneo servicioEquipoTorneo;
  private final ServicioTorneo servicioTorneo;

  public LobbyController(ServicioEquipoTorneo servicioEquipoTorneo, ServicioTorneo servicioTorneo) {
    this.servicioEquipoTorneo = servicioEquipoTorneo;
    this.servicioTorneo = servicioTorneo;
  }

  @RequestMapping("/lobby")
  public ModelAndView irALobby(@RequestParam(value = "idTorneo", required = false) Long idTorneo) {
    Map<String, Object> model = new ModelMap();

    Long idTorneoABuscar = idTorneo != null ? idTorneo : 1L;

    List<EquipoTorneo> tabla = servicioEquipoTorneo.obtenerEstadisticas(idTorneoABuscar);
    model.put("tablaPosiciones", tabla);
    model.put("idTorneoActual", idTorneoABuscar);

    //cant real de equipos agregados
    int totalEquipos = servicioEquipoTorneo.contarEquiposPorTorneo(idTorneoABuscar);
    model.put("totalEquipos", totalEquipos);

    //cant de fechas finalizadas

    long fechasJugadas = servicioTorneo.contarFechasFinalizadas(idTorneoABuscar);
    model.put("fechasJugadas", fechasJugadas);

    return new ModelAndView("lobby", model);
  }
}
