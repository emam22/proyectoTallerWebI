package com.tallerwebi.dominio;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Transient;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
public class Fecha {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private Integer numero;
  private LocalDate fechaOrigen;

  @ManyToOne
  private Torneo torneo;

  @Transient
  private List<Partido> partidos = new ArrayList<>();

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public Integer getNumero() {
    return numero;
  }

  public void setNumero(Integer numero) {
    this.numero = numero;
  }

  public LocalDate getFechaOrigen() {
    return fechaOrigen;
  }

  public void setFechaOrigen(LocalDate fechaOrigen) {
    this.fechaOrigen = fechaOrigen;
  }

  public Torneo getTorneo() {
    return torneo;
  }

  public void setTorneo(Torneo torneo) {
    this.torneo = torneo;
  }

  public List<Partido> getPartidos() {
    return partidos;
  }

  public void setPartidos(List<Partido> partidos) {
    this.partidos = partidos;
  }
}
