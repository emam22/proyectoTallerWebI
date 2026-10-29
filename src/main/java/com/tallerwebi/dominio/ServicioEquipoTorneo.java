package com.tallerwebi.dominio;

import com.tallerwebi.dominio.excepcion.EquipoExistente;
import com.tallerwebi.dominio.excepcion.EquipoYaInscripto;
import com.tallerwebi.dominio.excepcion.SiglaExistente;
import com.tallerwebi.dominio.excepcion.SiglaInvalida;
import java.util.List;

public interface ServicioEquipoTorneo {
  void crearEquipoEInscribirlo(Equipo equipo, Torneo torneo)
    throws EquipoExistente, SiglaExistente, SiglaInvalida;
  int contarEquiposPorTorneo(Long idTorneo);
  void inscribirEquipoExistente(Equipo equipo, Torneo torneo) throws EquipoYaInscripto;
  List<EquipoTorneo> obtenerEstadisticas(Long idTorneo);

  void desvincularEquipo(Long idEquipo, Long idTorneo);
  EquipoTorneo consultarEquipoTorneoPorId(Long idEquipo, Long idTorneo);

  List<EquipoTorneo> obtenerEquiposPorTorneo(Long idTorneo);

  void actualizarTablaPosiciones(
    Long idTorneo,
    Long idEquipoLocal,
    Long idEquipoVisitante,
    int golesLocal,
    int golesVisitante
  );
}
