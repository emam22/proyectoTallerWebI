package com.tallerwebi.dominio;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.format.annotation.DateTimeFormat;

@Entity
public class Torneo {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private String nombre;

  @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
  private LocalDateTime fechaInicio;

  private Integer cantidadDeEquipos;
  private Integer cantidadMinJugadores;
  private Integer cantidadMaxJugadores;
  private Integer cantidadAmaSusp;
  private Boolean idaYVuelta;

  @OneToMany(mappedBy = "torneo", cascade = CascadeType.ALL)
  private List<Jornada> jornadas = new ArrayList<>();

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getNombre() {
    return nombre;
  }

  public void setNombre(String nombre) {
    this.nombre = nombre;
  }

  public LocalDateTime getFechaInicio() {
    return this.fechaInicio;
  }

  public void setFechaInicio(LocalDateTime fecha) {
    this.fechaInicio = fecha;
  }

  @Transient
  public LocalDateTime getFechaLimite() {
    return this.fechaInicio.minusHours(24);
  }

  public Integer getCantidadDeEquipos() {
    return cantidadDeEquipos;
  }

  public void setCantidadDeEquipos(Integer cantindadDeEquipos) {
    this.cantidadDeEquipos = cantindadDeEquipos;
  }

  public Integer getCantidadMaxJugadores() {
    return cantidadMaxJugadores;
  }

  public void setCantidadMaxJugadores(Integer cantidadMaxJugadores) {
    this.cantidadMaxJugadores = cantidadMaxJugadores;
  }

  public Integer getCantidadMinJugadores() {
    return cantidadMinJugadores;
  }

  public void setCantidadMinJugadores(Integer cantidadMinJugadores) {
    this.cantidadMinJugadores = cantidadMinJugadores;
  }

  public Integer getCantidadAmaSusp() {
    return cantidadAmaSusp;
  }

  public void setCantidadAmaSusp(Integer cantidadAmaSusp) {
    this.cantidadAmaSusp = cantidadAmaSusp;
  }

  public Boolean getIdaYVuelta() {
    return idaYVuelta;
  }

  public void setIdaYVuelta(Boolean idaYVuelta) {
    this.idaYVuelta = idaYVuelta;
  }

  public void agregarJornada(Jornada jornada) {
    jornadas.add(jornada);
    jornada.setTorneo(this);
  }

  public List<Jornada> getJornadas() {
    return jornadas;
  }

  public List<Partido> getPartidosEnTorneo() {
    return jornadas
      .stream()
      .flatMap(jornada -> jornada.getPartidos().stream())
      .collect(Collectors.toList());
  }

  public int contarEnfrentamientosUnicos() {
    List<String> enfrentamientos = new ArrayList<>();

    for (Partido partido : getPartidosEnTorneo()) {
      String local = partido.getEquipoLocal().getNombre();
      String visitante = partido.getEquipoVisitante().getNombre();

      String enfrentamiento;

      if (local.compareTo(visitante) < 0) {
        enfrentamiento = local + " - " + visitante;
      } else {
        enfrentamiento = visitante + " - " + local;
      }

      if (!enfrentamientos.contains(enfrentamiento)) {
        enfrentamientos.add(enfrentamiento);
      }
    }

    return enfrentamientos.size();
  }

  @Transient
  public boolean permiteModificaciones() {
    LocalDateTime fechaLimite = fechaInicio.minusHours(24);
    return LocalDateTime.now().isBefore(fechaLimite);
  }
}
