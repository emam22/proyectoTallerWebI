package com.tallerwebi.dominio;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class Torneo {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private String nombre;
  private Integer cantidadDeEquipos;
  private Integer cantidadMinJugadores;
  private Integer cantidadMaxJugadores;
  private Integer cantidadAmaSusp;
  private Boolean idaYVuelta;

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

  public Integer getCantidadDeEquipos() {
    return cantidadDeEquipos;
  }

  public void setCantidadDeEquipos(Integer cantindadDeEquipos) {
    this.cantidadDeEquipos = cantindadDeEquipos;
  }

  public Integer getCantidadMaxJugadores() {
    return cantidadMaxJugadores;
  }

  public void setCantidadMaxJugadores(Integer cantidadMaxJugadores) {
    this.cantidadMaxJugadores = cantidadMaxJugadores;
  }

  public Integer getCantidadMinJugadores() {
    return cantidadMinJugadores;
  }

  public void setCantidadMinJugadores(Integer cantidadMinJugadores) {
    this.cantidadMinJugadores = cantidadMinJugadores;
  }

  public Integer getCantidadAmaSusp() {
    return cantidadAmaSusp;
  }

  public void setCantidadAmaSusp(Integer cantidadAmaSusp) {
    this.cantidadAmaSusp = cantidadAmaSusp;
  }

  public Boolean getIdaYVuelta() {
    return idaYVuelta;
  }

  public void setIdaYVuelta(Boolean idaYVuelta) {
    this.idaYVuelta = idaYVuelta;
  }
}
