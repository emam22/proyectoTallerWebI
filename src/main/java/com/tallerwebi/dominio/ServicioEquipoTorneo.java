package com.tallerwebi.dominio;

import com.tallerwebi.dominio.excepcion.EquipoExistente;
import java.util.List;

public interface ServicioEquipoTorneo {
  void crearEquipoEInscribirlo(Equipo equipo, Torneo torneo) throws EquipoExistente;
  int contarEquiposPorTorneo(Long idTorneo);
  List<EquipoTorneo> obtenerEstadisticas(Long idTorneo);
}
