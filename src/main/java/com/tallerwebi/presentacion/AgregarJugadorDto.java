package com.tallerwebi.presentacion;

public class AgregarJugadorDto {

  private Long idTorneo;
  private Long idEquipo;
  private String nombre;
  private String apellido;
  private Integer dorsal;
  private String puesto;

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

  public String getNombre() {
    return nombre;
  }

  public void setNombre(String nombre) {
    this.nombre = nombre;
  }

  public String getApellido() {
    return apellido;
  }

  public void setApellido(String apellido) {
    this.apellido = apellido;
  }

  public Integer getDorsal() {
    return dorsal;
  }

  public void setDorsal(Integer dorsal) {
    this.dorsal = dorsal;
  }

  public String getPuesto() {
    return puesto;
  }

  public void setPuesto(String puesto) {
    this.puesto = puesto;
  }
}
