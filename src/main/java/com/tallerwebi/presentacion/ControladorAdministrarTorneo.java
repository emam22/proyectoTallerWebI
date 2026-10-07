package com.tallerwebi.presentacion;

import com.tallerwebi.dominio.CambioTorneo;
import com.tallerwebi.dominio.Fecha;
import com.tallerwebi.dominio.ServicioAdministrarTorneo;
import com.tallerwebi.dominio.ServicioFixture;
import com.tallerwebi.dominio.ServicioTorneo;
import com.tallerwebi.dominio.Torneo;
import com.tallerwebi.dominio.excepcion.FixtureInvalidoException;
import com.tallerwebi.dominio.excepcion.TorneoInvalidoException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class ControladorAdministrarTorneo {

  private static final String ID_TORNEO = "idTorneo";
  private static final String TORNEO = "torneo";
  private static final String ERROR = "error";

  private ServicioTorneo servicioTorneo;
  private ServicioAdministrarTorneo servicioAdministrar;
  private ServicioFixture servicioFixture;

  @Autowired
  public ControladorAdministrarTorneo(
    ServicioTorneo servicioTorneo,
    ServicioAdministrarTorneo servicioAdministrar,
    ServicioFixture servicioFixture
  ) {
    this.servicioTorneo = servicioTorneo;
    this.servicioAdministrar = servicioAdministrar;
    this.servicioFixture = servicioFixture;
  }

  @RequestMapping("/administrarTorneo")
  public ModelAndView irAAdministrarTorneo() {
    Map<String, Object> model = new ModelMap();
    model.put("torneos", servicioTorneo.obtenerTorneos());
    return new ModelAndView("administrarTorneo", model);
  }

  @RequestMapping(path = "/administrarTorneo/{idTorneo}", method = RequestMethod.GET)
  public ModelAndView gestionarTorneo(@PathVariable(ID_TORNEO) Long idTorneo) {
    return vistaGestion(idTorneo, new ModelMap());
  }

  @RequestMapping(path = "/administrarTorneo/{idTorneo}/editar", method = RequestMethod.GET)
  public ModelAndView irAEditarTorneo(@PathVariable(ID_TORNEO) Long idTorneo) {
    Torneo torneo = servicioAdministrar.obtenerTorneo(idTorneo);
    if (torneo == null) {
      return new ModelAndView("redirect:/administrarTorneo");
    }
    Map<String, Object> model = new ModelMap();
    model.put(TORNEO, torneo);
    return new ModelAndView("editarTorneo", model);
  }

  @RequestMapping(path = "/administrarTorneo/{idTorneo}/editar", method = RequestMethod.POST)
  public ModelAndView guardarCambios(
    @PathVariable(ID_TORNEO) Long idTorneo,
    @ModelAttribute(TORNEO) Torneo torneo
  ) {
    if (torneo.getIdaYVuelta() == null) {
      torneo.setIdaYVuelta(false);
    }
    torneo.setId(idTorneo);

    Map<String, Object> model = new ModelMap();
    try {
      List<CambioTorneo> cambios = servicioAdministrar.modificarParametros(idTorneo, torneo);
      if (cambios.isEmpty()) {
        model.put("ok", "No se detectaron cambios para guardar.");
      } else {
        model.put("ok", "Cambios registrados: " + cambios.size() + ".");
      }
    } catch (TorneoInvalidoException e) {
      model.put(TORNEO, torneo);
      model.put(ERROR, e.getMessage());
      return new ModelAndView("editarTorneo", model);
    } catch (Exception e) {
      model.put(TORNEO, torneo);
      model.put(ERROR, "No se pudieron guardar los cambios: " + e.getMessage());
      return new ModelAndView("editarTorneo", model);
    }
    return vistaGestion(idTorneo, model);
  }

  @RequestMapping(path = "/administrarTorneo/{idTorneo}/fixture", method = RequestMethod.POST)
  public ModelAndView generarFixture(@PathVariable(ID_TORNEO) Long idTorneo) {
    Map<String, Object> model = new ModelMap();
    try {
      if (servicioFixture.hayFixture(idTorneo)) {
        model.put(ERROR, "El torneo ya tiene un fixture generado.");
      } else {
        List<Fecha> fechas = servicioFixture.generarFixture(idTorneo);
        model.put("ok", "Fixture generado con " + fechas.size() + " fechas.");
      }
    } catch (FixtureInvalidoException e) {
      model.put(ERROR, e.getMessage());
    } catch (Exception e) {
      model.put(ERROR, "No se pudo generar el fixture: " + e.getMessage());
    }
    return vistaGestion(idTorneo, model);
  }

  private ModelAndView vistaGestion(Long idTorneo, Map<String, Object> model) {
    Torneo torneo = servicioAdministrar.obtenerTorneo(idTorneo);
    if (torneo == null) {
      return new ModelAndView("redirect:/administrarTorneo");
    }
    boolean hayFixture = servicioFixture.hayFixture(idTorneo);
    model.put(TORNEO, torneo);
    model.put("historial", servicioAdministrar.historial(idTorneo));
    model.put("hayFixture", hayFixture);
    model.put("fechas", hayFixture ? servicioFixture.obtenerFechas(idTorneo) : new ArrayList<>());
    return new ModelAndView("gestionTorneo", model);
  }
}
