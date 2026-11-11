package com.tallerwebi.presentacion;

import com.tallerwebi.dominio.Jornada;
import com.tallerwebi.dominio.ServicioTorneo;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class ControladorHistorialResultados {

  private final ServicioTorneo servicioTorneo;

  public ControladorHistorialResultados(ServicioTorneo servicioTorneo) {
    this.servicioTorneo = servicioTorneo;
  }

  @GetMapping("/historialResultados")
  public String verResumenResultados(@RequestParam("idTorneo") Long idTorneo, Model modelo) {
    List<Jornada> jornadas = servicioTorneo.obtenerJornadasPorTorneo(idTorneo);

    modelo.addAttribute("jornadas", jornadas != null ? jornadas : new ArrayList<>());

    modelo.addAttribute("idTorneoActual", idTorneo);

    return "historial-resultados";
  }
}
