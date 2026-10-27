package com.tallerwebi.presentacion;

import java.util.List;

public class ResultadoPartidoDTO {
    private Long idPartido;
    private Integer golesLocal;
    private Integer golesVisitante;
    private List<Long> idsJugadoresTitulares;

    // Eventos individuales
    private List<Long> idsGoleadores;
    private List<Long> idsAsistidores;
    private List<Long> idsTarjetasAmarillas;
    private List<Long> idsTarjetasRojas;

    public Long getIdPartido() {
        return idPartido;
    }

    public void setIdPartido(Long idPartido) {
        this.idPartido = idPartido;
    }

    public Integer getGolesLocal() {
        return golesLocal;
    }

    public void setGolesLocal(Integer golesLocal) {
        this.golesLocal = golesLocal;
    }

    public Integer getGolesVisitante() {
        return golesVisitante;
    }

    public void setGolesVisitante(Integer golesVisitante) {
        this.golesVisitante = golesVisitante;
    }

    public List<Long> getIdsJugadoresTitulares() {
        return idsJugadoresTitulares;
    }

    public void setIdsJugadoresTitulares(List<Long> idsJugadoresTitulares) {
        this.idsJugadoresTitulares = idsJugadoresTitulares;
    }

    public List<Long> getIdsGoleadores() {
        return idsGoleadores;
    }

    public void setIdsGoleadores(List<Long> idsGoleadores) {
        this.idsGoleadores = idsGoleadores;
    }

    public List<Long> getIdsAsistidores() {
        return idsAsistidores;
    }

    public void setIdsAsistidores(List<Long> idsAsistidores) {
        this.idsAsistidores = idsAsistidores;
    }

    public List<Long> getIdsTarjetasAmarillas() {
        return idsTarjetasAmarillas;
    }

    public void setIdsTarjetasAmarillas(List<Long> idsTarjetasAmarillas) {
        this.idsTarjetasAmarillas = idsTarjetasAmarillas;
    }

    public List<Long> getIdsTarjetasRojas() {
        return idsTarjetasRojas;
    }

    public void setIdsTarjetasRojas(List<Long> idsTarjetasRojas) {
        this.idsTarjetasRojas = idsTarjetasRojas;
    }
}