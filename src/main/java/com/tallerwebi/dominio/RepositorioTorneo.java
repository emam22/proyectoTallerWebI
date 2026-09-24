package com.tallerwebi.dominio;


public interface RepositorioTorneo {
    Torneo buscarTorneo(String nombre);
    void guardar(Torneo torneo);
}
