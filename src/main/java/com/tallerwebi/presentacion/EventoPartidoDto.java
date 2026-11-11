package com.tallerwebi.presentacion;

import com.tallerwebi.dominio.EventoPartido;
import com.tallerwebi.dominio.Jugador;
import com.tallerwebi.dominio.TipoEventoPartido;

public class EventoPartidoDto {

  private final TipoEventoPartido tipo;
  private final Integer minuto;
  private final String nombreJugador;
  private final String nombreAsistidor;
  private final String nombreEquipo;

  public EventoPartidoDto(EventoPartido evento, String nombreEquipo) {
    this.tipo = evento.getTipo();
    this.minuto = evento.getMinuto();
    this.nombreJugador = obtenerNombreCompleto(evento.getJugador());
    this.nombreAsistidor = obtenerNombreCompleto(evento.getAsistidor());
    this.nombreEquipo = nombreEquipo;
  }

  private String obtenerNombreCompleto(Jugador jugador) {
    if (jugador == null) {
      return null;
    }

    return jugador.getNombre() + " " + jugador.getApellido();
  }

  public TipoEventoPartido getTipo() {
    return tipo;
  }

  public Integer getMinuto() {
    return minuto;
  }

  public String getNombreJugador() {
    return nombreJugador;
  }

  public String getNombreAsistidor() {
    return nombreAsistidor;
  }

  public String getNombreEquipo() {
    return nombreEquipo;
  }
}
