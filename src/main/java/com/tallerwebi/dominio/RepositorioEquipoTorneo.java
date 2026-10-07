package com.tallerwebi.dominio;

import java.util.List;

@SuppressWarnings("PMD.ImplicitFunctionalInterface") // Es un repositorio, no una lambda
public interface RepositorioEquipoTorneo {
  List<EquipoTorneo> buscarPorTorneo(Long idTorneo);
}
