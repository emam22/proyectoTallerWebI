package com.tallerwebi.dominio;

import com.tallerwebi.dominio.excepcion.TorneoExistente;
import java.util.List;

public interface ServicioTorneo {
  Torneo consultarTorneoPorId(Long id);
  Torneo consultarTorneo(String nombre);
  void registrarTorneo(Torneo torneo) throws TorneoExistente, Exception;
  List<Torneo> obtenerTorneos();
  List<Jornada> obtenerJornadasPorTorneo(Long torneoId);
  void crearTorneoConFixture(Torneo torneo, List<Long> idsEquipos)
    throws TorneoExistente, Exception;
}
