package com.tallerwebi.integracion;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.text.IsEqualIgnoringCase.equalToIgnoringCase;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.tallerwebi.dominio.Torneo;
import com.tallerwebi.integracion.config.HibernateTestConfig;
import com.tallerwebi.integracion.config.SpringWebTestConfig;
import java.time.LocalDateTime;
import org.hibernate.SessionFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.servlet.ModelAndView;

/** Pruebas de integración de la vista del lobby de administrador. */
@ExtendWith(SpringExtension.class)
@WebAppConfiguration
@ContextConfiguration(classes = { SpringWebTestConfig.class, HibernateTestConfig.class })
public class LobbyAdminControllerTest {

  @Autowired
  private WebApplicationContext wac;

  @Autowired
  private SessionFactory sessionFactory;

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
  @Transactional
  @Rollback
  public void administrarTorneoDeberiaMostrarLaVistaYElIdDelTorneo() throws Exception {
    // 1. Preparación: Creamos y guardamos un torneo real en la BD de test
    Torneo torneo = new Torneo();
    torneo.setNombre("Torneo Clausura");
    torneo.setFechaInicio(LocalDateTime.now().plusDays(5));

    this.sessionFactory.getCurrentSession().save(torneo);

    Long idTorneo = torneo.getId();

    // 2. Ejecución: Petición HTTP al endpoint con el ID real generado
    MvcResult result =
      this.mockMvc.perform(get("/administrarTorneoAdmin/" + idTorneo))
        .andExpect(status().isOk())
        .andReturn();

    ModelAndView modelAndView = result.getModelAndView();

    // 3. Validación
    assert modelAndView != null;
    assertThat(modelAndView.getModel().get("idTorneoActual"), equalTo(idTorneo));
    assertThat(modelAndView.getViewName(), equalToIgnoringCase("administrarTorneoAdmin"));
  }
}
