package com.tallerwebi.dominio;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

import com.tallerwebi.dominio.*;
import com.tallerwebi.dominio.excepcion.JugadorNoExiste;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ServicioEstadisticasJugadorTest {

  private ServicioEstadisticaJugadorImpl servicioEstadistica;
  private ServicioJugador servicioJugadorMock;
  private RepositorioEstadisticaJugador repoEstadisticasMock;
  private ServicioTorneo servicioTorneoMock;

  @BeforeEach
  public void init() {
    servicioJugadorMock = mock(ServicioJugador.class);
    repoEstadisticasMock = mock(RepositorioEstadisticaJugador.class);
    servicioTorneoMock = mock(ServicioTorneo.class);

    servicioEstadistica =
      new ServicioEstadisticaJugadorImpl(
        servicioJugadorMock,
        repoEstadisticasMock,
        servicioTorneoMock
      );
  }

  @Test
  public void alIniciarEstadisticasDeJugadorExistenteDebeGuardarlasEnCero() throws Exception {
    // Preparación
    Jugador jugador = mock(Jugador.class);
    when(jugador.getId()).thenReturn(1L);
    when(servicioJugadorMock.consultarJugador(1L)).thenReturn(jugador);

    // Ejecución
    servicioEstadistica.iniciarEstadisticas(jugador);

    // Validación
    verify(repoEstadisticasMock, times(1))
      .guardar(
        argThat(estadistica ->
          estadistica.getJugador().equals(jugador) &&
          estadistica.getGoles().equals(0) &&
          estadistica.getAsistencias().equals(0) &&
          estadistica.getParticipacionesGol().equals(0) &&
          estadistica.getAmarillas().equals(0) &&
          estadistica.getRojas().equals(0)
        )
      );
  }

  @Test
  public void alIniciarEstadisticasDeJugadorInexistenteDebeLanzarExcepcion() throws Exception {
    // Preparación
    Jugador jugador = mock(Jugador.class);
    when(jugador.getId()).thenReturn(1L);
    when(servicioJugadorMock.consultarJugador(1L)).thenReturn(null);

    // Ejecución y validación
    assertThrows(JugadorNoExiste.class, () -> servicioEstadistica.iniciarEstadisticas(jugador));

    verify(repoEstadisticasMock, never()).guardar(any(EstadisticaJugador.class));
  }

  @Test
  public void alObtenerEstadisticasDebeRetornarLasDelRepositorio() {
    // Preparación
    List<EstadisticaJugador> estadisticas = new ArrayList<>();
    estadisticas.add(new EstadisticaJugador());

    when(repoEstadisticasMock.obtenerEstadisticas()).thenReturn(estadisticas);

    // Ejecución
    List<EstadisticaJugador> resultado = servicioEstadistica.obtenerEstadisticas();

    // Validación
    assertThat(resultado, equalTo(estadisticas));
    verify(repoEstadisticasMock, times(1)).obtenerEstadisticas();
  }

  @Test
  public void alRegistrarTarjetaAmarillaDebeActualizarEstadisticas() {
    Jugador jugador = new Jugador();
    jugador.setId(1L);

    Torneo torneo = new Torneo();
    torneo.setCantidadAmaSusp(3);

    EstadisticaJugador estadisticas = new EstadisticaJugador();
    estadisticas.setJugador(jugador);
    estadisticas.setAmarillas(0);

    when(servicioJugadorMock.consultarJugador(1L)).thenReturn(jugador);
    when(servicioTorneoMock.consultarTorneoPorId(10L)).thenReturn(torneo);
    when(repoEstadisticasMock.buscarEstadisticasPorIdJugador(1L)).thenReturn(estadisticas);

    servicioEstadistica.registrarTarjetaAmarilla(1L, 10L);

    assertThat(estadisticas.getAmarillas(), equalTo(1));
    verify(repoEstadisticasMock).actualizarEstadisticasJugador(estadisticas);
    verify(servicioJugadorMock, never()).agregarFechasSuspension(1L, 1);
  }

  @Test
  public void alRegistrarTarjetaRojaDebeActualizarEstadisticasYAgregarSuspension() {
    Jugador jugador = new Jugador();
    jugador.setId(1L);

    Torneo torneo = new Torneo();

    EstadisticaJugador estadisticas = new EstadisticaJugador();
    estadisticas.setJugador(jugador);
    estadisticas.setRojas(0);

    when(servicioJugadorMock.consultarJugador(1L)).thenReturn(jugador);
    when(servicioTorneoMock.consultarTorneoPorId(10L)).thenReturn(torneo);
    when(repoEstadisticasMock.buscarEstadisticasPorIdJugador(1L)).thenReturn(estadisticas);

    servicioEstadistica.registrarTarjetaRoja(1L, 10L);

    assertThat(estadisticas.getRojas(), equalTo(1));
    verify(repoEstadisticasMock).actualizarEstadisticasJugador(estadisticas);
    verify(servicioJugadorMock).agregarFechasSuspension(1L, 1);
  }
}
