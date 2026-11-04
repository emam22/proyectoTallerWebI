package com.tallerwebi.dominio;

import com.tallerwebi.dominio.excepcion.FechaLimiteEditarTorneo;
import java.time.LocalDateTime;
import org.springframework.stereotype.Component;

@Component
public class ValidadorTorneo {

  public boolean tieneCantidadJugadoresValida(Torneo torneo) {
    return torneo.getCantidadMaxJugadores() > torneo.getCantidadMinJugadores();
  }

  public boolean tieneFechaInicioValida(Torneo torneo) {
    return (
      torneo.getFechaInicio() != null && !torneo.getFechaInicio().isBefore(LocalDateTime.now())
    );
  }

  public boolean permiteModificaciones(Torneo torneo) {
    return torneo != null && torneo.permiteModificaciones();
  }

  public void validarModificacion(Torneo torneo) throws FechaLimiteEditarTorneo {
    if (!permiteModificaciones(torneo)) {
      throw new FechaLimiteEditarTorneo(
        "La fecha actual es mayor a la permitida para hacer cambios en el torneo."
      );
    }
  }
}
