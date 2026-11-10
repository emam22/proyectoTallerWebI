package com.tallerwebi.presentacion;

import com.tallerwebi.dominio.Jornada;
import com.tallerwebi.dominio.ServicioSimulacionTorneo;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class ControladorSimulacion {

  private static final String ERROR = "error";

  private final ServicioSimulacionTorneo servicioSimulacionTorneo;

  public ControladorSimulacion(ServicioSimulacionTorneo servicioSimulacionTorneo) {
    this.servicioSimulacionTorneo = servicioSimulacionTorneo;
  }

  @PostMapping("/avanzarFecha")
  public ModelAndView avanzarFecha(
    @ModelAttribute("avanzarFechaDto") AvanzarFechaDto avanzarFechaDto,
    RedirectAttributes redirectAttributes
  ) {
    Long idTorneo = avanzarFechaDto.getIdTorneo();

    try {
      Jornada jornada = servicioSimulacionTorneo.avanzarJornada(idTorneo);

      if (jornada == null) {
        redirectAttributes.addFlashAttribute(
          ERROR,
          "No se pudo obtener una jornada pendiente para simular."
        );
        return redirigirAlLobby(idTorneo);
      }

      if (jornada.getId() == null) {
        redirectAttributes.addFlashAttribute(ERROR, "La jornada simulada no tiene un ID válido.");
        return redirigirAlLobby(idTorneo);
      }

      // Redirigimos al resumen enviando el ID del torneo
      // y el ID de la jornada que acabamos de simular.
      return new ModelAndView(
        "redirect:/resumen?idTorneo=" + idTorneo + "&idJornada=" + jornada.getId()
      );
    } catch (IllegalStateException e) {
      redirectAttributes.addFlashAttribute(ERROR, e.getMessage());
      return redirigirAlLobby(idTorneo);
    }
  }

  private ModelAndView redirigirAlLobby(Long idTorneo) {
    return new ModelAndView("redirect:/lobby?idTorneo=" + idTorneo);
  }
}
