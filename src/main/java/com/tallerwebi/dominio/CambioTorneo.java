package com.tallerwebi.dominio;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import java.time.LocalDateTime;

@Entity
public class CambioTorneo {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne
  private Torneo torneo;

  private String campo;

  private String valorAnterior;

  private String valorNuevo;

  private LocalDateTime fechaHora;

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public Torneo getTorneo() {
    return torneo;
  }

  public void setTorneo(Torneo torneo) {
    this.torneo = torneo;
  }

  public String getCampo() {
    return campo;
  }

  public void setCampo(String campo) {
    this.campo = campo;
  }

  public String getValorAnterior() {
    return valorAnterior;
  }

  public void setValorAnterior(String valorAnterior) {
    this.valorAnterior = valorAnterior;
  }

  public String getValorNuevo() {
    return valorNuevo;
  }

  public void setValorNuevo(String valorNuevo) {
    this.valorNuevo = valorNuevo;
  }

  public LocalDateTime getFechaHora() {
    return fechaHora;
  }

  public void setFechaHora(LocalDateTime fechaHora) {
    this.fechaHora = fechaHora;
  }
}
