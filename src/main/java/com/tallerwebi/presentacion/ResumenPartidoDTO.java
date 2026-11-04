package com.tallerwebi.presentacion;

import com.tallerwebi.dominio.EstadisticaJugador;
import com.tallerwebi.dominio.Partido;
import java.util.List;

// Agrupa toda la información necesaria para mostrar un partido
// y las estadísticas acumuladas de los jugadores de ambos equipos.
public class ResumenPartidoDTO {

  private final Partido partido;
  private final List<EstadisticaJugador> estadisticasLocal;
  private final List<EstadisticaJugador> estadisticasVisitante;

  public ResumenPartidoDTO(
    Partido partido,
    List<EstadisticaJugador> estadisticasLocal,
    List<EstadisticaJugador> estadisticasVisitante
  ) {
    this.partido = partido;
    this.estadisticasLocal = estadisticasLocal;
    this.estadisticasVisitante = estadisticasVisitante;
  }

  public Partido getPartido() {
    return partido;
  }

  public List<EstadisticaJugador> getEstadisticasLocal() {
    return estadisticasLocal;
  }

  public List<EstadisticaJugador> getEstadisticasVisitante() {
    return estadisticasVisitante;
  }
}
