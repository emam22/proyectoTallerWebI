package com.tallerwebi.presentacion;

public class HabilitarJugadorDto {

  private Long idTorneo;
  private Long idEquipo;
  private Long idJugador;
  private Boolean habilitado;

  public Long getIdTorneo() {
    return idTorneo;
  }

  public void setIdTorneo(Long idTorneo) {
    this.idTorneo = idTorneo;
  }

  public Long getIdEquipo() {
    return idEquipo;
  }

  public void setIdEquipo(Long idEquipo) {
    this.idEquipo = idEquipo;
  }

  public Long getIdJugador() {
    return idJugador;
  }

  public void setIdJugador(Long idJugador) {
    this.idJugador = idJugador;
  }

  public Boolean getHabilitado() {
    return habilitado;
  }

  public void setHabilitado(Boolean habilitado) {
    this.habilitado = habilitado;
  }
}
