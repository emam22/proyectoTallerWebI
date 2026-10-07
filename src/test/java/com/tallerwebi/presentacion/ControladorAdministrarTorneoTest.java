package com.tallerwebi.presentacion;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.equalToIgnoringCase;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.servlet.ModelAndView;

public class ControladorAdministrarTorneoTest {

  private ControladorAdministrarTorneo controlador;
  private ServicioTorneo servicioTorneoMock;
  private ServicioAdministrarTorneo servicioAdministrarMock;
  private ServicioFixture servicioFixtureMock;

  @BeforeEach
  public void init() {
    servicioTorneoMock = mock(ServicioTorneo.class);
    servicioAdministrarMock = mock(ServicioAdministrarTorneo.class);
    servicioFixtureMock = mock(ServicioFixture.class);
    controlador =
      new ControladorAdministrarTorneo(
        servicioTorneoMock,
        servicioAdministrarMock,
        servicioFixtureMock
      );
  }

  private Torneo torneo() {
    Torneo torneo = new Torneo();
    torneo.setId(1L);
    torneo.setNombre("Torneo Demo");
    torneo.setCantidadDeEquipos(4);
    torneo.setCantidadMinJugadores(11);
    torneo.setCantidadMaxJugadores(15);
    torneo.setCantidadAmaSusp(3);
    torneo.setIdaYVuelta(true);
    return torneo;
  }

  @Test
  public void seMuestranLosTorneosDisponibles() {
    // preparacion
    when(servicioTorneoMock.obtenerTorneos()).thenReturn(List.of(torneo()));

    // ejecucion
    ModelAndView mav = controlador.irAAdministrarTorneo();

    // validacion
    assertThat(mav.getViewName(), equalToIgnoringCase("administrarTorneo"));
    assertThat(((List<?>) modelo(mav).get("torneos")).size(), is(1));
  }

  @Test
  public void alSeleccionarTorneoSeMuestranLasOpcionesDeGestion() {
    // preparacion
    when(servicioAdministrarMock.obtenerTorneo(1L)).thenReturn(torneo());
    when(servicioAdministrarMock.historial(1L)).thenReturn(new ArrayList<>());
    when(servicioFixtureMock.hayFixture(1L)).thenReturn(false);

    // ejecucion
    ModelAndView mav = controlador.gestionarTorneo(1L);

    // validacion
    assertThat(mav.getViewName(), equalToIgnoringCase("gestionTorneo"));
    Torneo torneoPanel = (Torneo) modelo(mav).get("torneo");
    assertThat(torneoPanel.getNombre(), equalTo("Torneo Demo"));
    assertThat(((List<?>) modelo(mav).get("historial")).size(), is(0));
    assertThat(modelo(mav).get("hayFixture"), is(false));
    verify(servicioFixtureMock, never()).obtenerFechas(anyLong());
  }

  @Test
  public void elPanelDeUnTorneoInexistenteRedirigeAListaDeTorneos() {
    // preparacion
    when(servicioAdministrarMock.obtenerTorneo(404L)).thenReturn(null);

    // ejecucion
    ModelAndView mav = controlador.gestionarTorneo(404L);

    // validacion
    assertThat(mav.getViewName(), equalTo("redirect:/administrarTorneo"));
  }

  @Test
  public void editarMuestraElFormularioConLosParametrosActuales() {
    // preparacion
    when(servicioAdministrarMock.obtenerTorneo(1L)).thenReturn(torneo());

    // ejecucion
    ModelAndView mav = controlador.irAEditarTorneo(1L);

    // validacion
    assertThat(mav.getViewName(), equalToIgnoringCase("editarTorneo"));
    Torneo torneoForm = (Torneo) modelo(mav).get("torneo");
    assertThat(torneoForm.getNombre(), equalTo("Torneo Demo"));
    assertThat(torneoForm.getCantidadMinJugadores(), is(11));
  }

  @Test
  public void guardarCambiosValidosRegistraYVuelveAlPanel() throws TorneoInvalidoException {
    // preparacion
    CambioTorneo cambio = new CambioTorneo();
    cambio.setCampo("nombre");
    when(servicioAdministrarMock.modificarParametros(anyLong(), any(Torneo.class)))
      .thenReturn(List.of(cambio));
    when(servicioAdministrarMock.obtenerTorneo(1L)).thenReturn(torneo());
    when(servicioAdministrarMock.historial(1L)).thenReturn(List.of(cambio));
    when(servicioFixtureMock.hayFixture(1L)).thenReturn(false);

    // ejecucion
    ModelAndView mav = controlador.guardarCambios(1L, torneo());

    // validacion
    assertThat(mav.getViewName(), equalToIgnoringCase("gestionTorneo"));
    assertThat(modelo(mav).get("ok").toString(), containsString("1"));
    verify(servicioAdministrarMock).modificarParametros(anyLong(), any(Torneo.class));
  }

  @Test
  public void nombreVacioMuestraErrorSinGuardarNada() throws TorneoInvalidoException {
    // preparacion
    Torneo torneoSinNombre = torneo();
    torneoSinNombre.setNombre("   ");
    when(servicioAdministrarMock.modificarParametros(anyLong(), any(Torneo.class)))
      .thenThrow(new TorneoInvalidoException("El nombre del torneo no puede estar vacio"));

    // ejecucion
    ModelAndView mav = controlador.guardarCambios(1L, torneoSinNombre);

    // validacion
    assertThat(mav.getViewName(), equalToIgnoringCase("editarTorneo"));
    assertThat(modelo(mav).get("error").toString(), containsString("nombre"));
    assertThat(modelo(mav).containsKey("ok"), is(false));
  }

  @Test
  public void minimoMayorQueMaximoMuestraError() throws TorneoInvalidoException {
    // preparacion
    Torneo torneoInvalido = torneo();
    torneoInvalido.setCantidadMinJugadores(20);
    torneoInvalido.setCantidadMaxJugadores(15);
    when(servicioAdministrarMock.modificarParametros(anyLong(), any(Torneo.class)))
      .thenThrow(
        new TorneoInvalidoException("El minimo de jugadores no puede ser mayor que el maximo")
      );

    // ejecucion
    ModelAndView mav = controlador.guardarCambios(1L, torneoInvalido);

    // validacion
    assertThat(mav.getViewName(), equalToIgnoringCase("editarTorneo"));
    assertThat(modelo(mav).get("error").toString(), containsString("minimo"));
    assertThat(modelo(mav).containsKey("ok"), is(false));
  }

  @Test
  public void equiposMenosDeDosMuestraErrorDevolvidoPorElServicio() throws TorneoInvalidoException {
    // preparacion
    Torneo torneoInvalido = torneo();
    torneoInvalido.setCantidadDeEquipos(1);
    when(servicioAdministrarMock.modificarParametros(anyLong(), any(Torneo.class)))
      .thenThrow(new TorneoInvalidoException("La cantidad de equipos debe ser al menos 2"));

    // ejecucion
    ModelAndView mav = controlador.guardarCambios(1L, torneoInvalido);

    // validacion
    assertThat(mav.getViewName(), equalToIgnoringCase("editarTorneo"));
    assertThat(modelo(mav).get("error").toString(), containsString("al menos 2"));
    assertThat(modelo(mav).containsKey("ok"), is(false));
  }

  @Test
  public void generarFixtureMuestraConfirmacionConLaCantidadDeFechas() throws Exception {
    // preparacion
    when(servicioFixtureMock.hayFixture(1L)).thenReturn(false, true);
    when(servicioFixtureMock.generarFixture(1L)).thenReturn(List.of(new Fecha(), new Fecha()));
    when(servicioAdministrarMock.obtenerTorneo(1L)).thenReturn(torneo());
    when(servicioAdministrarMock.historial(1L)).thenReturn(new ArrayList<>());
    when(servicioFixtureMock.obtenerFechas(1L)).thenReturn(new ArrayList<>());

    // ejecucion
    ModelAndView mav = controlador.generarFixture(1L);

    // validacion
    assertThat(mav.getViewName(), equalToIgnoringCase("gestionTorneo"));
    assertThat(modelo(mav).get("ok").toString(), containsString("2"));
    verify(servicioFixtureMock).generarFixture(1L);
  }

  @Test
  public void generarFixtureCuandoYaExisteMuestraError() throws Exception {
    // preparacion
    when(servicioFixtureMock.hayFixture(1L)).thenReturn(true);
    when(servicioAdministrarMock.obtenerTorneo(1L)).thenReturn(torneo());
    when(servicioAdministrarMock.historial(1L)).thenReturn(new ArrayList<>());
    when(servicioFixtureMock.obtenerFechas(1L)).thenReturn(new ArrayList<>());

    // ejecucion
    ModelAndView mav = controlador.generarFixture(1L);

    // validacion
    assertThat(modelo(mav).get("error").toString(), containsString("ya tiene"));
    verify(servicioFixtureMock, never()).generarFixture(anyLong());
  }

  @Test
  public void fixtureInvalidoSeMuestraComoErrorDeNegocio() throws Exception {
    // preparacion
    when(servicioFixtureMock.hayFixture(1L)).thenReturn(false);
    when(servicioFixtureMock.generarFixture(1L))
      .thenThrow(new FixtureInvalidoException("necesita al menos 2 equipos"));
    when(servicioAdministrarMock.obtenerTorneo(1L)).thenReturn(torneo());
    when(servicioAdministrarMock.historial(1L)).thenReturn(new ArrayList<>());

    // ejecucion
    ModelAndView mav = controlador.generarFixture(1L);

    // validacion
    assertThat(mav.getViewName(), equalToIgnoringCase("gestionTorneo"));
    assertThat(modelo(mav).get("error").toString(), containsString("2 equipos"));
  }

  private Map<String, Object> modelo(ModelAndView mav) {
    return mav.getModel();
  }
}
