package com.tallerwebi.integracion;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
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

/** Pruebas de integración de la vista de administración de torneo. */
@ExtendWith(SpringExtension.class)
@WebAppConfiguration
@ContextConfiguration(classes = { SpringWebTestConfig.class, HibernateTestConfig.class })
public class ControladorAdministrarTorneoTest {

  @Autowired
  private WebApplicationContext wac;

  private MockMvc mockMvc;

  @BeforeEach
  public void init() {
    this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();
  }

  @Test
  public void debeRetornarLaVistaAdministrarTorneoCuandoSeNavegaAAdministrarTorneo()
    throws Exception {
    MvcResult result =
      this.mockMvc.perform(get("/administrarTorneo")).andExpect(status().isOk()).andReturn();

    ModelAndView modelAndView = result.getModelAndView();
    assert modelAndView != null;
    assertThat(modelAndView.getViewName(), equalToIgnoringCase("administrarTorneo"));
  }

  @Test
  public void laVistaAdministrarTorneoDebeContenerLaTablaDeTorneos() throws Exception {
    this.mockMvc.perform(get("/administrarTorneo"))
      .andExpect(status().isOk())
      .andExpect(view().name("administrarTorneo"))
      .andExpect(content().string(containsString("ADMINISTRAR TORNEO EN CURSO")))
      .andExpect(content().string(containsString("Ingrese nombre del torneo...")))
      .andExpect(content().string(containsString("Fecha creacion")))
      .andExpect(content().string(containsString("Cantidad Equipos")))
      .andExpect(content().string(containsString("Cantidad Jugadores")))
      .andExpect(content().string(containsString("Cantidad fechas")))
      .andExpect(content().string(not(containsString("TorneoMegaFutbol"))));
  }
}
