package com.tallerwebi.dominio;

import java.util.List;

public interface RepositorioJornada {
  void guardar(Jornada jornada);
  void guardarListaFixture(List<Jornada> jornadas); // Para guardar la lista completa del fixture
  List<Jornada> obtenerJornadasPorTorneo(Long torneoId);
}
