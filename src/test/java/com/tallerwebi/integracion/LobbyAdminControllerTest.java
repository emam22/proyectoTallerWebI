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

/** Pruebas de integración de la vista del lobby de administrador. */
@ExtendWith(SpringExtension.class)
@WebAppConfiguration
@ContextConfiguration(classes = { SpringWebTestConfig.class, HibernateTestConfig.class })
public class LobbyAdminControllerTest {

  @Autowired
  private WebApplicationContext wac;

  private MockMvc mockMvc;

  @BeforeEach
  public void init() {
    this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();
  }

  @Test
  public void debeRetornarLaVistaLobbyAdminCuandoSeNavegaALobbyAdmin() throws Exception {
    MvcResult result =
      this.mockMvc.perform(get("/lobbyAdmin")).andExpect(status().isOk()).andReturn();

    ModelAndView modelAndView = result.getModelAndView();
    assert modelAndView != null;
    assertThat(modelAndView.getViewName(), equalToIgnoringCase("lobbyAdmin"));
  }

  @Test
  public void laVistaLobbyAdminDebeContenerLasOpcionesDeAdministracion() throws Exception {
    this.mockMvc.perform(get("/lobbyAdmin"))
      .andExpect(status().isOk())
      .andExpect(view().name("lobbyAdmin"))
      .andExpect(content().string(containsString("SIMULADOR")))
      .andExpect(content().string(containsString("Bienvenido Administrador")))
      .andExpect(content().string(containsString("CREAR NUEVO TORNEO")))
      .andExpect(content().string(containsString("ADMINISTRAR TORNEO EN CURSO")))
      .andExpect(content().string(containsString("/formulario-torneo")))
      .andExpect(content().string(containsString("/administrarTorneo")));
  }
}
