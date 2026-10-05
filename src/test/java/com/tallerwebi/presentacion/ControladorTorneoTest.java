package com.tallerwebi.presentacion;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.text.IsEqualIgnoringCase.equalToIgnoringCase;
import static org.hamcrest.text.IsEqualIgnoringCase.equalToIgnoringCase;
import static org.mockito.Mockito.*;

import com.tallerwebi.dominio.ServicioTorneo;
import com.tallerwebi.dominio.Torneo;
import com.tallerwebi.dominio.excepcion.TorneoExistente;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.servlet.ModelAndView;

public class ControladorTorneoTest {

  private ControladorTorneo controladorTorneo;
  private Torneo torneoMock;
  private ServicioTorneo servicioTorneoMock;

  @BeforeEach
  public void init() {
    torneoMock = mock(Torneo.class);
    servicioTorneoMock = mock(ServicioTorneo.class);
    controladorTorneo = new ControladorTorneo(servicioTorneoMock);
  }

  @Test
  public void alCrearTorneoSeDebeCrearTorneoYRedirigirACreacionDeEquipo() throws Exception {
    // preparación
    when(torneoMock.getId()).thenReturn(5L);

    // ejecución
    ModelAndView mav = controladorTorneo.crearTorneo(torneoMock);

    // validación
    assertThat(mav.getViewName(), equalToIgnoringCase("redirect:/creacion-equipo?idTorneo=5"));

    verify(servicioTorneoMock, times(1)).registrarTorneo(torneoMock);
  }

  @Test
  public void siElTorneoYaExisteSeDebeVolverALFormulario() throws Exception {
    //preparacion
    doThrow(TorneoExistente.class).when(servicioTorneoMock).registrarTorneo(torneoMock);

    //ejecucion
    ModelAndView mav = controladorTorneo.crearTorneo(torneoMock);

    //validacion
    assertThat(mav.getViewName(), equalToIgnoringCase("formulario-torneo"));
    assertThat(
      mav.getModel().get("error").toString(),
      equalToIgnoringCase("Ya existe un torneo con este nombre")
    );
  }

  @Test
  public void errorEnRegistrarTorneoDeberiaVolverAFormularioTorneoYMostrarError() throws Exception {
    // preparacion
    doThrow(RuntimeException.class).when(servicioTorneoMock).registrarTorneo(torneoMock);

    // ejecucion
    ModelAndView modelAndView = controladorTorneo.crearTorneo(torneoMock);

    // validacion
    assertThat(modelAndView.getViewName(), equalToIgnoringCase("formulario-torneo"));
    assertThat(
      modelAndView.getModel().get("error").toString(),
      equalToIgnoringCase("Error al crear el torneo")
    );
  }

  @Test
  public void crearEquipoDebePasarMinYMaxDelTorneoEnCurso() {
    // preparacion
    Torneo torneo = new Torneo();
    torneo.setId(1L);
    torneo.setCantidadMinJugadores(10);
    torneo.setCantidadMaxJugadores(15);
    when(servicioTorneoMock.consultarTorneoPorId(1L)).thenReturn(torneo);

    // ejecucion
    ModelAndView mav = controladorTorneo.crearEquipo(1L);

    // validacion
    assertThat(mav.getViewName(), equalToIgnoringCase("creacion-equipo"));
    assertThat(mav.getModel().get("minJugadores").toString(), equalTo("10"));
    assertThat(mav.getModel().get("maxJugadores").toString(), equalTo("15"));
  }

  @Test
  public void crearEquipoSinTorneosCargadosDeberiaUsarLosValoresPorDefecto() {
    // preparacion
    when(servicioTorneoMock.consultarTorneoPorId(1L)).thenReturn(null);

    // ejecucion
    ModelAndView mav = controladorTorneo.crearEquipo(1L);

    // validacion
    assertThat(mav.getViewName(), equalToIgnoringCase("creacion-equipo"));
    assertThat(mav.getModel().get("minJugadores").toString(), equalTo("11"));
    assertThat(mav.getModel().get("maxJugadores").toString(), equalTo("23"));
  }

  @Test
  public void crearTorneoDeberiaRedirigirAcreacionEquipoConIdDelTorneo() {
    //preparacion
    Torneo torneo = new Torneo();
    torneo.setId(5L);
    torneo.setNombre("MegaFutbol");

    //ejecucion
    ModelAndView mav = controladorTorneo.crearTorneo(torneo);

    // validacion
    assertThat(mav.getViewName(), equalTo("redirect:/creacion-equipo?idTorneo=5"));
  }

  @Test
  public void crearEquipoDeberiaPasarElIdDelTorneoAlModelo() {
    // preparacion
    Torneo torneo = new Torneo();
    torneo.setId(5L);

    when(servicioTorneoMock.consultarTorneoPorId(5L)).thenReturn(torneo);

    // ejecucion
    ModelAndView mav = controladorTorneo.crearEquipo(5L);

    // validacion
    assertThat(mav.getModel().get("idTorneo"), equalTo(5L));
  }

  @Test
  public void crearEquipoDeberiaConsultarElTorneoConElIdRecibido() {
    // preparacion
    Torneo torneo = new Torneo();
    torneo.setId(8L);

    when(servicioTorneoMock.consultarTorneoPorId(8L)).thenReturn(torneo);

    // ejecucion
    controladorTorneo.crearEquipo(8L);

    // validacion
    verify(servicioTorneoMock, times(1)).consultarTorneoPorId(8L);
  }
}
