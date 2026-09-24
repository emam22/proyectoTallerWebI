package com.tallerwebi.dominio;

import com.tallerwebi.dominio.excepcion.TorneoExistente;

public interface ServicioTorneo {
  Torneo consultarTorneo(String nombre);
  void registrarTorneo(Torneo torneo) throws TorneoExistente, Exception;
}
