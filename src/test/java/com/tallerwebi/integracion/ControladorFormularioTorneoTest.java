package com.tallerwebi.integracion;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.text.IsEqualIgnoringCase.equalToIgnoringCase;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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

/** Pruebas de integración de la vista del formulario de creación de torneo. */
@ExtendWith(SpringExtension.class)
@WebAppConfiguration
@ContextConfiguration(classes = { SpringWebTestConfig.class, HibernateTestConfig.class })
public class ControladorFormularioTorneoTest {

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
      .andExpect(content().string(containsString("Agregar Equipos")));
  }

  @Test
  public void crearTorneoConNombreRepetidoDebeMostrarUnMensajeDeError() throws Exception {
    // preparacion
    Torneo torneoExistente = new Torneo();
    torneoExistente.setNombre("Torneo Repetido");
    torneoExistente.setCantidadDeEquipos(4);
    torneoExistente.setCantidadMinJugadores(10);
    torneoExistente.setCantidadMaxJugadores(15);
    torneoExistente.setCantidadAmaSusp(3);
    torneoExistente.setIdaYVuelta(true);
    this.servicioTorneo.registrarTorneo(torneoExistente);

    // ejecucion y validacion
    this.mockMvc.perform(post("/crearTorneo").param("nombre", "Torneo Repetido"))
      .andExpect(status().isOk())
      .andExpect(view().name("formulario-torneo"))
      .andExpect(content().string(containsString("Ya existe un torneo con este nombre")));
  }
}
