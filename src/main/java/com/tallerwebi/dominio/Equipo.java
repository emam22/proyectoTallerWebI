package com.tallerwebi.dominio;

import jakarta.persistence.*;
import java.util.List;

@Entity
public class Equipo {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private String codigo;
  private String nombre;
  private String escudo;
  private String ColorLocal;
  private String ColorVisitanate;

  @OneToMany
  private List<Jugador> jugadores;

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getCodigo() {
    return codigo;
  }

  public void setCodigo(String codigo) {
    this.codigo = codigo;
  }

  public String getNombre() {
    return nombre;
  }

  public void setNombre(String nombre) {
    this.nombre = nombre;
  }

  public String getEscudo() {
    return escudo;
  }

  public void setEscudo(String escudo) {
    this.escudo = escudo;
  }

  public String getColorLocal() {
    return ColorLocal;
  }

  public void setColorLocal(String colorLocal) {
    ColorLocal = colorLocal;
  }

  public String getColorVisitanate() {
    return ColorVisitanate;
  }

  public void setColorVisitanate(String colorVisitanate) {
    ColorVisitanate = colorVisitanate;
  }

  public List<Jugador> getJugadores() {
    return jugadores;
  }

  public void setJugadores(List<Jugador> jugadores) {
    this.jugadores = jugadores;
  }
}
