package com.tallerwebi.dominio;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.tallerwebi.dominio.excepcion.TorneoInvalidoException;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ServicioAdministrarTorneoTest {

  private RepositorioTorneo repositorioTorneoMock;
  private RepositorioCambioTorneo repositorioCambioTorneoMock;
  private ServicioAdministrarTorneo servicio;

  @BeforeEach
  public void init() {
    repositorioTorneoMock = mock(RepositorioTorneo.class);
    repositorioCambioTorneoMock = mock(RepositorioCambioTorneo.class);
    servicio =
      new ServicioAdministrarTorneoImpl(repositorioTorneoMock, repositorioCambioTorneoMock);
  }

  private Torneo torneoGuardado() {
    Torneo torneo = new Torneo();
    torneo.setId(1L);
    torneo.setNombre("Torneo Viejo");
    torneo.setCantidadDeEquipos(8);
    torneo.setCantidadMinJugadores(10);
    torneo.setCantidadMaxJugadores(15);
    torneo.setCantidadAmaSusp(3);
    torneo.setIdaYVuelta(true);
    return torneo;
  }

  @Test
  public void alCambiarParametrosSeRegistraUnCambioPorCampoModificado()
    throws TorneoInvalidoException {
    // preparacion
    when(repositorioTorneoMock.buscarTorneoPorId(1L)).thenReturn(torneoGuardado());
    Torneo datos = torneoGuardado();
    datos.setNombre("Torneo Nuevo");
    datos.setCantidadDeEquipos(10);

    // ejecucion
    List<CambioTorneo> cambios = servicio.modificarParametros(1L, datos);

    // validacion
    assertThat(cambios, hasSize(2));
    assertThat(cambios.get(0).getCampo(), equalTo("nombre"));
    assertThat(cambios.get(0).getValorAnterior(), equalTo("Torneo Viejo"));
    assertThat(cambios.get(0).getValorNuevo(), equalTo("Torneo Nuevo"));
    assertThat(cambios.get(1).getCampo(), equalTo("cantidad de equipos"));
    assertThat(cambios.get(1).getValorAnterior(), equalTo("8"));
    assertThat(cambios.get(1).getValorNuevo(), equalTo("10"));
    verify(repositorioCambioTorneoMock, times(2)).guardar(any());
    verify(repositorioTorneoMock).guardar(any(Torneo.class));
  }

  @Test
  public void siNadaCambiaNoSeRegistraNingunCambio() throws TorneoInvalidoException {
    // preparacion
    when(repositorioTorneoMock.buscarTorneoPorId(1L)).thenReturn(torneoGuardado());
    Torneo datos = torneoGuardado();

    // ejecucion
    List<CambioTorneo> cambios = servicio.modificarParametros(1L, datos);

    // validacion
    assertThat(cambios, hasSize(0));
    verify(repositorioCambioTorneoMock, never()).guardar(any());
  }

  @Test
  public void elHistorialDevuelveLosCambiosDelTorneo() {
    // preparacion
    CambioTorneo cambio = new CambioTorneo();
    cambio.setCampo("nombre");
    when(repositorioCambioTorneoMock.buscarPorTorneo(1L)).thenReturn(List.of(cambio));

    // ejecucion
    List<CambioTorneo> historial = servicio.historial(1L);

    // validacion
    assertThat(historial, hasSize(1));
    assertThat(historial.get(0).getCampo(), equalTo("nombre"));
  }

  @Test
  public void cambiarIdaYVueltaSeRegistraConTextoSiNo() throws TorneoInvalidoException {
    // preparacion
    when(repositorioTorneoMock.buscarTorneoPorId(1L)).thenReturn(torneoGuardado());
    Torneo datos = torneoGuardado();
    datos.setIdaYVuelta(false);

    // ejecucion
    List<CambioTorneo> cambios = servicio.modificarParametros(1L, datos);

    // validacion
    assertThat(cambios, hasSize(1));
    assertThat(cambios.get(0).getValorAnterior(), equalTo("Si"));
    assertThat(cambios.get(0).getValorNuevo(), equalTo("No"));
  }

  @Test
  public void modificarUnTorneoInexistenteLanzaError() throws TorneoInvalidoException {
    // preparacion
    when(repositorioTorneoMock.buscarTorneoPorId(99L)).thenReturn(null);

    // ejecucion y validacion
    boolean lanzoError = false;
    try {
      servicio.modificarParametros(99L, torneoGuardado());
    } catch (IllegalArgumentException e) {
      lanzoError = true;
    }
    assertThat(lanzoError, is(true));
    verify(repositorioTorneoMock, never()).guardar(any());
  }

  @Test
  public void obtenerTorneoDelegaEnElRepositorio() {
    // preparacion
    Torneo torneo = torneoGuardado();
    when(repositorioTorneoMock.buscarTorneoPorId(1L)).thenReturn(torneo);

    // ejecucion
    Torneo resultado = servicio.obtenerTorneo(1L);

    // validacion
    assertThat(resultado.getNombre(), equalTo("Torneo Viejo"));
  }

  @Test
  public void modificarConMenosDeDosEquiposLanzaErrorSinGuardar() {
    // preparacion
    Torneo datos = torneoGuardado();
    datos.setCantidadDeEquipos(1);

    // ejecucion y validacion
    assertThrows(TorneoInvalidoException.class, () -> servicio.modificarParametros(1L, datos));
    verify(repositorioTorneoMock, never()).guardar(any(Torneo.class));
  }

  @Test
  public void modificarConMenosDeDosJugadoresLanzaErrorSinGuardar() {
    // preparacion
    Torneo datos = torneoGuardado();
    datos.setCantidadMinJugadores(1);

    // ejecucion y validacion
    assertThrows(TorneoInvalidoException.class, () -> servicio.modificarParametros(1L, datos));
    verify(repositorioTorneoMock, never()).guardar(any(Torneo.class));
  }

  @Test
  public void modificarConAmarillasNegativasLanzaErrorSinGuardar() {
    // preparacion
    Torneo datos = torneoGuardado();
    datos.setCantidadAmaSusp(-1);

    // ejecucion y validacion
    assertThrows(TorneoInvalidoException.class, () -> servicio.modificarParametros(1L, datos));
    verify(repositorioTorneoMock, never()).guardar(any(Torneo.class));
  }

  @Test
  public void modificarConNombreVacioLanzaErrorSinGuardar() {
    // preparacion
    Torneo datos = torneoGuardado();
    datos.setNombre("  ");

    // ejecucion y validacion
    assertThrows(TorneoInvalidoException.class, () -> servicio.modificarParametros(1L, datos));
    verify(repositorioTorneoMock, never()).guardar(any(Torneo.class));
  }
}
