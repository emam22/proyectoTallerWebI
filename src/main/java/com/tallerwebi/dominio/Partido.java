package com.tallerwebi.dominio;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalTime;

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

  @ManyToOne
  private Jornada jornada;

  private Integer golesLocal;
  private Integer golesVisitante;
  private Boolean disputado;
  private LocalDate fecha;
  private LocalTime hora;

  public Partido() {
    this.disputado = false;
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
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

  public LocalDate getFecha() {
    return fecha;
  }

  public void setFecha(LocalDate fecha) {
    this.fecha = fecha;
  }

  public LocalTime getHora() {
    return hora;
  }

  public void setHora(LocalTime hora) {
    this.hora = hora;
  }

  public Jornada getJornada() {
    return jornada;
  }

  public void setJornada(Jornada jornada) {
    this.jornada = jornada;
  }
}