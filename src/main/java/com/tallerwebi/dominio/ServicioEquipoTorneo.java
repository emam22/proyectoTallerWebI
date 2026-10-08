package com.tallerwebi.dominio;

import com.tallerwebi.dominio.excepcion.EquipoExistente;
import com.tallerwebi.dominio.excepcion.EquipoYaInscripto;
import java.util.List;

public interface ServicioEquipoTorneo {
  void crearEquipoEInscribirlo(Equipo equipo, Torneo torneo) throws EquipoExistente;
  int contarEquiposPorTorneo(Long idTorneo);
  void inscribirEquipoExistente(Equipo equipo, Torneo torneo) throws EquipoYaInscripto;
  List<EquipoTorneo> obtenerEstadisticas(Long idTorneo);
}
