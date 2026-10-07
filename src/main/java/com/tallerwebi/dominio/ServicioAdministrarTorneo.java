package com.tallerwebi.dominio;

import com.tallerwebi.dominio.excepcion.TorneoInvalidoException;
import java.util.List;

public interface ServicioAdministrarTorneo {
  Torneo obtenerTorneo(Long idTorneo);

  List<CambioTorneo> modificarParametros(Long idTorneo, Torneo datos)
    throws TorneoInvalidoException;

  List<CambioTorneo> historial(Long idTorneo);
}
