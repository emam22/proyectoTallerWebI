package com.tallerwebi.dominio;

import com.tallerwebi.dominio.excepcion.TorneoInvalidoException;

/**
 * Reglas de negocio de un torneo, un unico lugar de verdad.
 * Se invoca desde los servicios antes de persistir.
 */
public final class ValidadorTorneo {

  public static final int CANTIDAD_MINIMA = 2;

  private ValidadorTorneo() {}

  public static void validar(Torneo torneo) throws TorneoInvalidoException {
    if (torneo == null) {
      throw new TorneoInvalidoException("Falta el torneo a validar");
    }
    validarNombre(torneo);
    validarCantidades(torneo);
  }

  private static void validarNombre(Torneo torneo) throws TorneoInvalidoException {
    if (torneo.getNombre() == null || torneo.getNombre().trim().isEmpty()) {
      throw new TorneoInvalidoException("El nombre del torneo no puede estar vacio");
    }
  }

  private static void validarCantidades(Torneo torneo) throws TorneoInvalidoException {
    exigirPresentes(torneo);
    exigirMinimos(torneo);
    exigirCoherencia(torneo);
  }

  private static void exigirPresentes(Torneo torneo) throws TorneoInvalidoException {
    if (
      torneo.getCantidadDeEquipos() == null ||
      torneo.getCantidadMinJugadores() == null ||
      torneo.getCantidadMaxJugadores() == null ||
      torneo.getCantidadAmaSusp() == null
    ) {
      throw new TorneoInvalidoException("Todos los campos numericos deben tener un valor");
    }
  }

  private static void exigirMinimos(Torneo torneo) throws TorneoInvalidoException {
    if (torneo.getCantidadDeEquipos() < CANTIDAD_MINIMA) {
      throw new TorneoInvalidoException(
        "La cantidad de equipos debe ser al menos " + CANTIDAD_MINIMA
      );
    }
    if (
      torneo.getCantidadMinJugadores() < CANTIDAD_MINIMA ||
      torneo.getCantidadMaxJugadores() < CANTIDAD_MINIMA
    ) {
      throw new TorneoInvalidoException(
        "La cantidad de jugadores por equipo debe ser al menos " + CANTIDAD_MINIMA
      );
    }
    if (torneo.getCantidadAmaSusp() < 0) {
      throw new TorneoInvalidoException(
        "La cantidad de amarillas por suspension no puede ser negativa"
      );
    }
  }

  private static void exigirCoherencia(Torneo torneo) throws TorneoInvalidoException {
    if (torneo.getCantidadMinJugadores() > torneo.getCantidadMaxJugadores()) {
      throw new TorneoInvalidoException("El minimo de jugadores no puede ser mayor que el maximo");
    }
  }
}
