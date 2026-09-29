package com.tallerwebi.integracion;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.text.IsEqualIgnoringCase.equalToIgnoringCase;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import com.tallerwebi.dominio.ServicioTorneo;
import com.tallerwebi.dominio.Torneo;
import com.tallerwebi.integracion.config.HibernateTestConfig;
import com.tallerwebi.integracion.config.SpringWebTestConfig;
import javax.sql.DataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.servlet.ModelAndView;

/** Pruebas de integración de la vista de creación de equipo. */
@ExtendWith(SpringExtension.class)
@WebAppConfiguration
@ContextConfiguration(classes = { SpringWebTestConfig.class, HibernateTestConfig.class })
public class ControladorCreacionEquipoTest {

  @Autowired
  private WebApplicationContext wac;

  @Autowired
  private DataSource dataSource;

  @Autowired
  private ServicioTorneo servicioTorneo;

  private MockMvc mockMvc;

  @BeforeEach
  public void init() {
    this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();
    new JdbcTemplate(this.dataSource).execute("DELETE FROM Torneo");
  }

  @Test
  public void debeRetornarLaVistaCreacionEquipoCuandoSeNavegaACreacionEquipo() throws Exception {
    MvcResult result =
      this.mockMvc.perform(get("/creacion-equipo")).andExpect(status().isOk()).andReturn();

    ModelAndView modelAndView = result.getModelAndView();
    assert modelAndView != null;
    assertThat(modelAndView.getViewName(), equalToIgnoringCase("creacion-equipo"));
  }

  @Test
  public void laVistaCreacionEquipoDebeEnviarLosDatosALaAccionGuardarEquipo() throws Exception {
    this.mockMvc.perform(get("/creacion-equipo"))
      .andExpect(status().isOk())
      .andExpect(view().name("creacion-equipo"))
      .andExpect(content().string(containsString("CREACIÓN EQUIPO")))
      .andExpect(content().string(containsString("/guardarEquipo")))
      .andExpect(content().string(containsString("name=\"nombreEquipo\"")))
      .andExpect(content().string(containsString("name=\"escudoEquipo\"")))
      .andExpect(content().string(containsString("name=\"colorTitular1\"")))
      .andExpect(content().string(containsString("name=\"colorTitular2\"")))
      .andExpect(content().string(containsString("name=\"colorSuplente1\"")))
      .andExpect(content().string(containsString("name=\"colorSuplente2\"")))
      .andExpect(content().string(containsString("INGRESAR JUGADORES")))
      .andExpect(content().string(containsString("Mín. 11")))
      .andExpect(content().string(containsString("Nro. Dorsal")))
      .andExpect(content().string(containsString("Agregar Equipo")))
      .andExpect(content().string(containsString("id=\"formAgregarJugador\"")))
      .andExpect(content().string(containsString("id=\"btnAgregarEquipo\"")))
      .andExpect(content().string(containsString("btnAgregarEquipo\" disabled")));
  }

  @Test
  public void laVistaCreacionEquipoDebeMostrarMinYMaxDelTorneoEnCurso() throws Exception {
    // preparacion
    Torneo torneo = new Torneo();
    torneo.setNombre("Torneo Min Max");
    torneo.setCantidadDeEquipos(4);
    torneo.setCantidadMinJugadores(10);
    torneo.setCantidadMaxJugadores(15);
    torneo.setCantidadAmaSusp(3);
    torneo.setIdaYVuelta(true);
    this.servicioTorneo.registrarTorneo(torneo);

    // ejecucion y validacion
    this.mockMvc.perform(get("/creacion-equipo"))
      .andExpect(status().isOk())
      .andExpect(view().name("creacion-equipo"))
      .andExpect(content().string(containsString("Mín. 10")))
      .andExpect(content().string(containsString("Máx. 15")));
  }
}
