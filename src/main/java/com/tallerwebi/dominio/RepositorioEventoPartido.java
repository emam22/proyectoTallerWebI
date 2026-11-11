package com.tallerwebi.dominio;

import java.util.List;

public interface RepositorioEventoPartido {
  void guardar(EventoPartido evento);

  List<EventoPartido> obtenerEventosPorPartido(Long idPartido);
}
