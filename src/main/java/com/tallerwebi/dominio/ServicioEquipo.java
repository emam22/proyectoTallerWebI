package com.tallerwebi.dominio;

import com.tallerwebi.dominio.excepcion.EquipoExistente;
import com.tallerwebi.dominio.excepcion.SiglasExistentes;
import java.util.List;

public interface ServicioEquipo {
  Equipo consultarEquipoPorId(Long id);
  Equipo consultarEquipo(String nombre);
  void registrarEquipo(Equipo equipo) throws EquipoExistente, SiglasExistentes;
  List<Equipo> obtenerEquipos();
  void actualizarEquipo(Equipo equipo) throws EquipoExistente, SiglasExistentes;
  Equipo consultarEquipoJugador(Long idJugador);
}
