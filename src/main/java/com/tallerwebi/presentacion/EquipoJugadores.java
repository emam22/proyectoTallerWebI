package com.tallerwebi.presentacion;

import com.tallerwebi.dominio.Equipo;
import com.tallerwebi.dominio.Jugador;
import java.util.List;

public class EquipoJugadores {

  private final Equipo equipo;
  private final List<Jugador> jugadores;

  public EquipoJugadores(Equipo equipo, List<Jugador> jugadores) {
    this.equipo = equipo;
    this.jugadores = jugadores;
  }

  public Equipo getEquipo() {
    return equipo;
  }

  public List<Jugador> getJugadores() {
    return jugadores;
  }
}
