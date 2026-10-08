package com.tallerwebi.dominio;

import com.tallerwebi.dominio.excepcion.EquipoExistente;
import java.util.List;

public interface ServicioEquipo {
  Equipo consultarEquipoPorId(Long id);
  Equipo consultarEquipo(String nombre);
  void registrarEquipo(Equipo equipo) throws EquipoExistente;
  List<Equipo> obtenerEquipos();
}
