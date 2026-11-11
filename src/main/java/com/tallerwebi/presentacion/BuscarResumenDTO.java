package com.tallerwebi.presentacion;

// DTO utilizado para recibir los identificadores del torneo
// y de la jornada al ingresar a la pantalla de resumen.
public class BuscarResumenDTO {

  private Long idTorneo;
  private Long idJornada;

  public Long getIdTorneo() {
    return idTorneo;
  }

  public void setIdTorneo(Long idTorneo) {
    this.idTorneo = idTorneo;
  }

  public Long getIdJornada() {
    return idJornada;
  }

  public void setIdJornada(Long idJornada) {
    this.idJornada = idJornada;
  }
}
