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
  private Integer cantindadDeEquipos;
  private Integer cantindadMinJugadores;
  private Integer cantindadMaxJugadores;
  private Integer cantindadAmaSusp;
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

  public Integer getCantindadDeEquipos() {
    return cantindadDeEquipos;
  }

  public void setCantindadDeEquipos(Integer cantindadDeEquipos) {
    this.cantindadDeEquipos = cantindadDeEquipos;
  }

  public Integer getCantindadMaxJugadores() {
    return cantindadMaxJugadores;
  }

  public void setCantindadMaxJugadores(Integer cantindadMaxJugadores) {
    this.cantindadMaxJugadores = cantindadMaxJugadores;
  }

  public Integer getCantindadMinJugadores() {
    return cantindadMinJugadores;
  }

  public void setCantindadMinJugadores(Integer cantindadMinJugadores) {
    this.cantindadMinJugadores = cantindadMinJugadores;
  }

  public Integer getCantindadAmaSusp() {
    return cantindadAmaSusp;
  }

  public void setCantindadAmaSusp(Integer cantindadAmaSusp) {
    this.cantindadAmaSusp = cantindadAmaSusp;
  }

  public Boolean getIdaYVuelta() {
    return idaYVuelta;
  }

  public void setIdaYVuelta(Boolean idaYVuelta) {
    this.idaYVuelta = idaYVuelta;
  }
}
