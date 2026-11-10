package com.tallerwebi.presentacion;

public class ActualizarEquipoDto {

  private Long idEquipo;
  private Long idTorneo;
  private String nombre;
  private String sigla;
  private String colorLocal1;
  private String colorLocal2;
  private String colorVisitante1;
  private String colorVisitante2;
  private String escudoRuta;

  public Long getIdEquipo() {
    return idEquipo;
  }

  public void setIdEquipo(Long idEquipo) {
    this.idEquipo = idEquipo;
  }

  public Long getIdTorneo() {
    return idTorneo;
  }

  public void setIdTorneo(Long idTorneo) {
    this.idTorneo = idTorneo;
  }

  public String getNombre() {
    return nombre;
  }

  public void setNombre(String nombre) {
    this.nombre = nombre;
  }

  public String getSigla() {
    return sigla;
  }

  public void setSigla(String sigla) {
    this.sigla = sigla;
  }

  public String getColorLocal1() {
    return colorLocal1;
  }

  public void setColorLocal1(String colorLocal1) {
    this.colorLocal1 = colorLocal1;
  }

  public String getColorLocal2() {
    return colorLocal2;
  }

  public void setColorLocal2(String colorLocal2) {
    this.colorLocal2 = colorLocal2;
  }

  public String getColorVisitante1() {
    return colorVisitante1;
  }

  public void setColorVisitante1(String colorVisitante1) {
    this.colorVisitante1 = colorVisitante1;
  }

  public String getColorVisitante2() {
    return colorVisitante2;
  }

  public void setColorVisitante2(String colorVisitante2) {
    this.colorVisitante2 = colorVisitante2;
  }

  public String getEscudoRuta() {
    return escudoRuta;
  }

  public void setEscudoRuta(String escudoRuta) {
    this.escudoRuta = escudoRuta;
  }
}
