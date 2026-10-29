package com.tallerwebi.dominio;

import java.util.List;

public interface RepositorioEquipo {
  Equipo buscarPorId(Long id);
  Equipo buscarEquipo(String nombre);
  void guardar(Equipo equipo);
  List<Equipo> listar();
  void actualizar(Equipo equipo);
  Equipo buscarEquipoPorSiglas(String siglas);
  Equipo buscarEquipoPorIdJugador(Long idJugador);
}
