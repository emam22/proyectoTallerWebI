package com.tallerwebi.dominio;

import com.tallerwebi.dominio.excepcion.EquipoExistente;

public interface ServicioEquipoTorneo {
  void crearEquipoEInscribirlo(Equipo equipo, Torneo torneo) throws EquipoExistente;
  int contarEquiposPorTorneo(Long idTorneo);
}
