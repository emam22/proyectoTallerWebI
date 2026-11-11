package com.tallerwebi.dominio;

import jakarta.persistence.*;

@Entity
public class EquipoTorneo {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private Integer puntos;
  private Integer partidosJugados;
  private Integer partidosGanados;
  private Integer partidoPerdidos;
  private Integer partidosEmpatados;
  private Integer golesAFavor;
  private Integer golesEnContra;

  @ManyToOne
  private Equipo equipo;

  @ManyToOne
  private Torneo torneo;

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public Integer getPuntos() {
    return puntos;
  }

  public void setPuntos(Integer puntos) {
    this.puntos = puntos;
  }

  public Integer getPartidosJugados() {
    return partidosJugados;
  }

  public void setPartidosJugados(Integer partidosJugados) {
    this.partidosJugados = partidosJugados;
  }

  public Integer getPartidosGanados() {
    return partidosGanados;
  }

  public void setPartidosGanados(Integer partidosGanados) {
    this.partidosGanados = partidosGanados;
  }

  public Integer getPartidoPerdidos() {
    return partidoPerdidos;
  }

  public void setPartidoPerdidos(Integer partidoPerdidos) {
    this.partidoPerdidos = partidoPerdidos;
  }

  public Integer getPartidosEmpatados() {
    return partidosEmpatados;
  }

  public void setPartidosEmpatados(Integer partidosEmpatados) {
    this.partidosEmpatados = partidosEmpatados;
  }

  public Integer getGolesAFavor() {
    return golesAFavor;
  }

  public void setGolesAFavor(Integer golesAFavor) {
    this.golesAFavor = golesAFavor;
  }

  public Integer getGolesEnContra() {
    return golesEnContra;
  }

  public void setGolesEnContra(Integer golesEnContra) {
    this.golesEnContra = golesEnContra;
  }

  public Equipo getEquipo() {
    return equipo;
  }

  public void setEquipo(Equipo equipo) {
    this.equipo = equipo;
  }

  public Torneo getTorneo() {
    return torneo;
  }

  public void setTorneo(Torneo torneo) {
    this.torneo = torneo;
  }

  public void registrarPartido(int golesFavor, int golesContra) {
    this.partidosJugados = valorOZero(this.partidosJugados) + 1;
    this.golesAFavor = valorOZero(this.golesAFavor) + golesFavor;
    this.golesEnContra = valorOZero(this.golesEnContra) + golesContra;

    if (golesFavor > golesContra) {
      this.puntos = valorOZero(this.puntos) + 3;
      this.partidosGanados = valorOZero(this.partidosGanados) + 1;
    } else if (golesFavor == golesContra) {
      this.puntos = valorOZero(this.puntos) + 1;
      this.partidosEmpatados = valorOZero(this.partidosEmpatados) + 1;
    } else {
      this.partidoPerdidos = valorOZero(this.partidoPerdidos) + 1;
    }
  }

  private int valorOZero(Integer valor) {
    return valor == null ? 0 : valor;
  }
}
