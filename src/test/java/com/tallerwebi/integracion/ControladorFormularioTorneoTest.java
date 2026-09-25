package com.tallerwebi.integracion;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.text.IsEqualIgnoringCase.equalToIgnoringCase;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import com.tallerwebi.integracion.config.HibernateTestConfig;
import com.tallerwebi.integracion.config.SpringWebTestConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.servlet.ModelAndView;

/** Pruebas de integración de la vista del formulario de creación de torneo. */
@ExtendWith(SpringExtension.class)
@WebAppConfiguration
@ContextConfiguration(classes = { SpringWebTestConfig.class, HibernateTestConfig.class })
public class ControladorFormularioTorneoTest {

  @Autowired
  private WebApplicationContext wac;

  private MockMvc mockMvc;

  @BeforeEach
  public void init() {
    this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();
  }

  @Test
  public void debeRetornarLaVistaFormularioTorneoCuandoSeNavegaAFormularioTorneo()
    throws Exception {
    MvcResult result =
      this.mockMvc.perform(get("/formulario-torneo")).andExpect(status().isOk()).andReturn();

    ModelAndView modelAndView = result.getModelAndView();
    assert modelAndView != null;
    assertThat(modelAndView.getViewName(), equalToIgnoringCase("formulario-torneo"));
  }

  @Test
  public void elFormularioDebeEnviarLosDatosDelTorneoALaAccionCrearTorneo() throws Exception {
    this.mockMvc.perform(get("/formulario-torneo"))
      .andExpect(status().isOk())
      .andExpect(view().name("formulario-torneo"))
      .andExpect(content().string(containsString("FORMULARIO TORNEO")))
      .andExpect(content().string(containsString("/crearTorneo")))
      .andExpect(content().string(containsString("name=\"nombre\"")))
      .andExpect(content().string(containsString("name=\"cantidadDeEquipos\"")))
      .andExpect(content().string(containsString("name=\"cantidadMaxJugadores\"")))
      .andExpect(content().string(containsString("name=\"cantidadMinJugadores\"")))
      .andExpect(content().string(containsString("name=\"idaYVuelta\"")))
      .andExpect(content().string(containsString("name=\"cantidadAmaSusp\"")))
      .andExpect(content().string(containsString("Finalizar Creación")));
  }
}
