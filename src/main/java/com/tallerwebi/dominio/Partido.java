package com.tallerwebi.dominio;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;

@Entity
public class Partido {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private Long fechaId;

  @ManyToOne
  private Equipo equipoLocal;

  @ManyToOne
  private Equipo equipoVisitante;

  private String codigoLocal;
  private String codigoVisitante;
  private Integer golesLocal;
  private Integer golesVisitante;
  private Boolean disputado;

  public Partido() {
    this.disputado = false;
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public Long getFechaId() {
    return fechaId;
  }

  public void setFechaId(Long fechaId) {
    this.fechaId = fechaId;
  }

  public Equipo getEquipoLocal() {
    return equipoLocal;
  }

  public void setEquipoLocal(Equipo equipoLocal) {
    this.equipoLocal = equipoLocal;
  }

  public Equipo getEquipoVisitante() {
    return equipoVisitante;
  }

  public void setEquipoVisitante(Equipo equipoVisitante) {
    this.equipoVisitante = equipoVisitante;
  }

  public String getCodigoLocal() {
    return codigoLocal;
  }

  public void setCodigoLocal(String codigoLocal) {
    this.codigoLocal = codigoLocal;
  }

  public String getCodigoVisitante() {
    return codigoVisitante;
  }

  public void setCodigoVisitante(String codigoVisitante) {
    this.codigoVisitante = codigoVisitante;
  }

  public Integer getGolesLocal() {
    return golesLocal;
  }

  public void setGolesLocal(Integer golesLocal) {
    this.golesLocal = golesLocal;
  }

  public Integer getGolesVisitante() {
    return golesVisitante;
  }

  public void setGolesVisitante(Integer golesVisitante) {
    this.golesVisitante = golesVisitante;
  }

  public Boolean getDisputado() {
    return disputado;
  }

  public void setDisputado(Boolean disputado) {
    this.disputado = disputado;
  }
}
