package com.tallerwebi.dominio;

import java.util.List;

public interface RepositorioTorneo {
  Torneo buscarTorneo(String nombre);
  void guardar(Torneo torneo);
  List<Torneo> listar();
}
