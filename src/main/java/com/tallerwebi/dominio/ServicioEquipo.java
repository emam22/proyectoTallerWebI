package com.tallerwebi.dominio;

import com.tallerwebi.dominio.excepcion.EquipoExistente;
import com.tallerwebi.dominio.excepcion.SiglaExistente;
import com.tallerwebi.dominio.excepcion.SiglaInvalida;
import java.util.List;

public interface ServicioEquipo {
  Equipo consultarEquipoPorId(Long id);
  Equipo consultarEquipo(String nombre);
  void registrarEquipo(Equipo equipo) throws EquipoExistente, SiglaExistente, SiglaInvalida;
  List<Equipo> obtenerEquipos();
  Equipo consultarEquipoJugador(Long idJugador);
  void actualizarEquipo(Equipo equipo) throws EquipoExistente, SiglaExistente, SiglaInvalida;
}
