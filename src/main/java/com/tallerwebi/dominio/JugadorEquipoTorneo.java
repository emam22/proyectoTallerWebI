package com.tallerwebi.dominio;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(
  uniqueConstraints = @UniqueConstraint(columnNames = { "jugador_id", "equipo_id", "torneo_id" })
)
public class JugadorEquipoTorneo {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne
  @JoinColumn(name = "jugador_id")
  private Jugador jugador;

  @ManyToOne
  @JoinColumn(name = "equipo_id")
  private Equipo equipo;

  @ManyToOne
  @JoinColumn(name = "torneo_id")
  private Torneo torneo;

  private Integer fechasSuspensionPendientes = 0;
  private String estadoSancion = "SIN_SANCION";

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public Jugador getJugador() {
    return jugador;
  }

  public void setJugador(Jugador jugador) {
    this.jugador = jugador;
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

  public Integer getFechasSuspensionPendientes() {
    return fechasSuspensionPendientes;
  }

  public void setFechasSuspensionPendientes(Integer fechasSuspensionPendientes) {
    this.fechasSuspensionPendientes = fechasSuspensionPendientes;
  }

  public String getEstadoSancion() {
    return estadoSancion;
  }

  public void setEstadoSancion(String estadoSancion) {
    this.estadoSancion = estadoSancion;
  }
}
