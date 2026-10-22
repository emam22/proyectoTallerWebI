package com.tallerwebi.dominio;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class Jugador {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private Integer dorsal;
  private String puesto;
  private String nombre;
  private String apellido;
  private Integer categoria;
  private Boolean habilitado = false;

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public Integer getDorsal() {
    return dorsal;
  }

  public void setDorsal(Integer dorsal) {
    this.dorsal = dorsal;
  }

  public String getPuesto() {
    return puesto;
  }

  public void setPuesto(String puesto) {
    this.puesto = puesto;
  }

  public String getNombre() {
    return nombre;
  }

  public void setNombre(String nombre) {
    this.nombre = nombre;
  }

  public String getApellido() {
    return apellido;
  }

  public void setApellido(String apellido) {
    this.apellido = apellido;
  }

  public Integer getCategoria() {
    return categoria;
  }

  public void setCategoria(Integer categoria) {
    this.categoria = categoria;
  }

  public Boolean getHabilitado() {
    return habilitado;
  }

  public void setHabilitado(Boolean habilitado) {
    this.habilitado = habilitado;
  }
}
