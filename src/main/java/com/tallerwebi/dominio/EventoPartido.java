package com.tallerwebi.dominio;

import jakarta.persistence.*;

@Entity
public class EventoPartido {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Enumerated(EnumType.STRING)
  private TipoEventoPartido tipo;

  private Integer minuto;

  @ManyToOne
  private Partido partido;

  @ManyToOne
  private Jugador jugador;

  @ManyToOne
  private Jugador asistidor;

  public EventoPartido() {}

  public Long getId() {
    return id;
  }

  public TipoEventoPartido getTipo() {
    return tipo;
  }

  public void setTipo(TipoEventoPartido tipo) {
    this.tipo = tipo;
  }

  public Integer getMinuto() {
    return minuto;
  }

  public void setMinuto(Integer minuto) {
    this.minuto = minuto;
  }

  public Partido getPartido() {
    return partido;
  }

  public void setPartido(Partido partido) {
    this.partido = partido;
  }

  public Jugador getJugador() {
    return jugador;
  }

  public void setJugador(Jugador jugador) {
    this.jugador = jugador;
  }

  public Jugador getAsistidor() {
    return asistidor;
  }

  public void setAsistidor(Jugador asistidor) {
    this.asistidor = asistidor;
  }
}
