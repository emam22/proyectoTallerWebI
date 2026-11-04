package com.tallerwebi.dominio;

@FunctionalInterface
public interface ServicioSimulacionTorneo {
  Jornada avanzarJornada(Long idTorneo);
}
