package com.tallerwebi.integracion;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
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

/** Pruebas de integración de la vista de administración de torneo. */
@ExtendWith(SpringExtension.class)
@WebAppConfiguration
@ContextConfiguration(classes = { SpringWebTestConfig.class, HibernateTestConfig.class })
public class ControladorAdministrarTorneoTest {

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
  public void debeRetornarLaVistaAdministrarTorneoCuandoSeNavegaAAdministrarTorneo()
    throws Exception {
    MvcResult result =
      this.mockMvc.perform(get("/administrarTorneo")).andExpect(status().isOk()).andReturn();

    ModelAndView modelAndView = result.getModelAndView();
    assert modelAndView != null;
    assertThat(modelAndView.getViewName(), equalToIgnoringCase("administrarTorneo"));
  }

  @Test
  public void laVistaAdministrarTorneoDebeMostrarUnAvisoCuandoNoHayTorneos() throws Exception {
    this.mockMvc.perform(get("/administrarTorneo"))
      .andExpect(status().isOk())
      .andExpect(view().name("administrarTorneo"))
      .andExpect(content().string(containsString("ADMINISTRAR TORNEO EN CURSO")))
      .andExpect(content().string(containsString("Ingrese nombre del torneo...")))
      .andExpect(content().string(containsString("Cantidad Equipos")))
      .andExpect(content().string(containsString("Jugadores por equipo")))
      .andExpect(content().string(containsString("Amarillas/Susp.")))
      .andExpect(content().string(containsString("Ida y vuelta")))
      .andExpect(content().string(containsString("No hay torneos cargados todavia")))
      .andExpect(content().string(not(containsString("TorneoMegaFutbol"))));
  }

  @Test
  public void laVistaAdministrarTorneoDebeMostrarElTorneoCargadoYOcultarLaFilaDeEjemplo()
    throws Exception {
    // preparacion
    Torneo torneo = new Torneo();
    torneo.setNombre("Torneo de Prueba");
    torneo.setCantidadDeEquipos(8);
    torneo.setCantidadMinJugadores(10);
    torneo.setCantidadMaxJugadores(15);
    torneo.setCantidadAmaSusp(3);
    torneo.setIdaYVuelta(false);
    this.servicioTorneo.registrarTorneo(torneo);

    // ejecucion y validacion
    this.mockMvc.perform(get("/administrarTorneo"))
      .andExpect(status().isOk())
      .andExpect(view().name("administrarTorneo"))
      .andExpect(content().string(containsString("Torneo de Prueba")))
      .andExpect(content().string(containsString("10 - 15")))
      .andExpect(content().string(containsString("No")))
      .andExpect(content().string(not(containsString("TorneoFutbolTotal"))));
  }
}
