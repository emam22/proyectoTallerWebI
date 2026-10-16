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

  @BeforeEach
  public void init() {
    servicioJugadorMock = mock(ServicioJugador.class);
    repoEstadisticasMock = mock(RepositorioEstadisticaJugador.class);

    servicioEstadistica =
      new ServicioEstadisticaJugadorImpl(servicioJugadorMock, repoEstadisticasMock);
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
}
