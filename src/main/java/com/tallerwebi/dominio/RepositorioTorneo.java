package com.tallerwebi.dominio;

import java.util.List;

public interface RepositorioTorneo {
  Torneo buscarTorneoPorId(Long id);
  Torneo buscarTorneo(String nombre);
  void guardar(Torneo torneo);
  List<Torneo> listar();
}
