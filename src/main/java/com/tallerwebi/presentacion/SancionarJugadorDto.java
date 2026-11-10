package com.tallerwebi.presentacion;

public class SancionarJugadorDto {

  private Long idTorneo;
  private Long idEquipo;
  private Long idJugador;
  private Integer fechas;

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

  public Integer getFechas() {
    return fechas;
  }

  public void setFechas(Integer fechas) {
    this.fechas = fechas;
  }
}
