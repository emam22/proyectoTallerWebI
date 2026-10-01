package com.tallerwebi.dominio;

import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

public class Jugador {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;





}
