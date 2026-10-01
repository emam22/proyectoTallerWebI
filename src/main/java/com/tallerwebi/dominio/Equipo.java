package com.tallerwebi.dominio;

import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

public class Equipo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nombre;
    private String escudo;
    private String ColorLocal;
    private String ColorVisitanate;
    private Integer puntos;
    private Integer partidosJugados;
    private Integer partidosGanados;
    private Integer partidoPerdidos;
    private Integer partidosEmpatados;
    private Integer golesAFavor;
    private Integer golesEnContra;
    private List <Jugador> jugadores;






}
