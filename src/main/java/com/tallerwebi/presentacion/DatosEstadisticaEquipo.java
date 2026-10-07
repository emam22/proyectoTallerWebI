package com.tallerwebi.presentacion;

public class DatosEstadisticaEquipo {

  private int posicion;
  private String nombreEquipo;
  private int partidosJugados;
  private int valor;

  public DatosEstadisticaEquipo(int posicion, String nombreEquipo, int partidosJugados, int valor) {
    this.posicion = posicion;
    this.nombreEquipo = nombreEquipo;
    this.partidosJugados = partidosJugados;
    this.valor = valor;
  }

  public int getPosicion() {
    return posicion;
  }

  public String getNombreEquipo() {
    return nombreEquipo;
  }

  public int getPartidosJugados() {
    return partidosJugados;
  }

  public int getValor() {
    return valor;
  }
}
