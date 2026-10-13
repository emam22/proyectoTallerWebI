package com.tallerwebi.dominio;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
public class Jornada {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private Integer numero;
  private String estado; // finalizado o pendiente

  @ManyToOne
  private Torneo torneo;

  @OneToMany(mappedBy = "jornada", cascade = CascadeType.PERSIST)
  private List<Partido> partidos = new ArrayList<>();

  @ManyToOne
  private Equipo equipoFechaLibre;

  public Jornada() {
    this.estado = "Pendiente";
  }

  public void setId(Long id) {
    this.id = id;
  }

  public Long getId() {
    return id;
  }

  public Integer getNumero() {
    return numero;
  }

  public void setNumero(Integer numero) {
    this.numero = numero;
  }

  public String getEstado() {
    return estado;
  }

  public void setEstado(String estado) {
    this.estado = estado;
  }

  public Torneo getTorneo() {
    return torneo;
  }

  public List<Partido> getPartidos() {
    return partidos;
  }

  public void setPartidos(List<Partido> partidos) {
    this.partidos = partidos;
  }

  public void setTorneo(Torneo torneo) {
    this.torneo = torneo;
  }

  public void agregarPartido(Partido partido) {
    partidos.add(partido);
    partido.setJornada(this);
  }

  public Equipo getEquipoFechaLibre() {
    return equipoFechaLibre;
  }

  public void setEquipoFechaLibre(Equipo equipoFechaLibre) {
    this.equipoFechaLibre = equipoFechaLibre;
  }
}
