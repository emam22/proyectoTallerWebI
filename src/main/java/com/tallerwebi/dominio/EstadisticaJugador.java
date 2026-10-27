package com.tallerwebi.dominio;

import jakarta.persistence.*;

@Entity
public class EstadisticaJugador {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private Integer partidosJugados;
  private Integer goles;
  private Integer asistencias;
  private Integer participacionesGol;
  private Integer amarillas;
  private Integer rojas;

  @ManyToOne
  private Jugador jugador;

  public Long getId() {
    return id;
  }

  public Integer getPartidosJugados() {
    return partidosJugados;
  }

  public void setPartidosJugados(Integer partidosJugados) {
    this.partidosJugados = partidosJugados;
  }

  public Integer getGoles() {
    return goles;
  }

  public void setGoles(Integer goles) {
    this.goles = goles;
  }

  public Integer getAsistencias() {
    return asistencias;
  }

  public void setAsistencias(Integer asistencias) {
    this.asistencias = asistencias;
  }

  public Integer getParticipacionesGol() {
    return participacionesGol;
  }

  public void setParticipacionesGol(Integer participacionesGol) {
    this.participacionesGol = participacionesGol;
  }

  public Integer getAmarillas() {
    return amarillas;
  }

  public void setAmarillas(Integer amarillas) {
    this.amarillas = amarillas;
  }

  public Integer getRojas() {
    return rojas;
  }

  public void setRojas(Integer rojas) {
    this.rojas = rojas;
  }

  public Jugador getJugador() {
    return jugador;
  }

  public void setJugador(Jugador jugador) {
    this.jugador = jugador;
  }
}
