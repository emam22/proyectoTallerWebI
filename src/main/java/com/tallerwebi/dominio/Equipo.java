package com.tallerwebi.dominio;

import jakarta.persistence.*;
import java.util.List;

@Entity
public class Equipo {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private String nombre;
  private String escudo;
  private String colorLocal1;
  private String colorLocal2;
  private String colorVisitante1;
  private String colorVisitante2;

  @OneToMany
  private List<Jugador> jugadores;

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
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

  public String getColorLocal1() {
    return colorLocal1;
  }

  public void setColorLocal1(String colorLocal1) {
    this.colorLocal1 = colorLocal1;
  }

  public String getColorLocal2() {
    return colorLocal2;
  }

  public void setColorLocal2(String colorLocal2) {
    this.colorLocal2 = colorLocal2;
  }

  public String getColorVisitante1() {
    return colorVisitante1;
  }

  public void setColorVisitante1(String colorVisitante1) {
    this.colorVisitante1 = colorVisitante1;
  }

  public String getColorVisitante2() {
    return colorVisitante2;
  }

  public void setColorVisitante2(String colorVisitante2) {
    this.colorVisitante2 = colorVisitante2;
  }

  public List<Jugador> getJugadores() {
    return jugadores;
  }

  public void setJugadores(List<Jugador> jugadores) {
    this.jugadores = jugadores;
  }
}
