package com.tallerwebi.dominio;

import com.tallerwebi.dominio.excepcion.JugadorNoExiste;
import jakarta.transaction.Transactional;
import java.util.List;
import org.springframework.stereotype.Service;

@Service("ServicioEstadisticasJugador")
@Transactional
public class ServicioEstadisticaJugadorImpl implements ServicioEstadisticaJugador {

  private final ServicioJugador servicioJugador;
  private RepositorioEstadisticaJugador repoEstadisticas;

  public ServicioEstadisticaJugadorImpl(
    ServicioJugador servicioJugador,
    RepositorioEstadisticaJugador repoEstadisticas
  ) {
    this.servicioJugador = servicioJugador;
    this.repoEstadisticas = repoEstadisticas;
  }

  @Override
  public void iniciarEstadisticas(Jugador jugador) throws JugadorNoExiste {
    Jugador jugadorCreado = servicioJugador.consultarJugador(jugador.getId());
    if (jugadorCreado == null) {
      throw new JugadorNoExiste();
    }

    EstadisticaJugador estadisticas = new EstadisticaJugador();

    estadisticas.setJugador(jugador);
    estadisticas.setGoles(0);
    estadisticas.setAsistencias(0);
    estadisticas.setParticipacionesGol(0);
    estadisticas.setAmarillas(0);
    estadisticas.setRojas(0);

    repoEstadisticas.guardar(estadisticas);
  }

  @Override
  public List<EstadisticaJugador> obtenerEstadisticas() {
    List<EstadisticaJugador> estadisticas = repoEstadisticas.obtenerEstadisticas();
    return estadisticas;
  }

  @Override
  public void eliminarEstadisticas(Long idJugador) {
    repoEstadisticas.eliminarFilaEstadistica(idJugador);
  }
}
