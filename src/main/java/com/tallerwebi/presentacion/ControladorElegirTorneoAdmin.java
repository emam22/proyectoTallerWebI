package com.tallerwebi.presentacion;

import com.tallerwebi.dominio.ServicioTorneo;
import com.tallerwebi.dominio.Torneo;
import com.tallerwebi.dominio.Usuario;
import jakarta.servlet.http.HttpServletRequest;
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
  public ModelAndView irAAdministrarTorneo(HttpServletRequest request) {
    Usuario usuario = (Usuario) request.getSession().getAttribute("USUARIO");

    Map<String, Object> model = new HashMap<>();

    List<Torneo> torneos = servicioTorneo.buscarTorneosPorUsuario(usuario.getId());

    model.put("torneos", torneos);

    return new ModelAndView("elegirTorneoAdmin", model);
  }
}
