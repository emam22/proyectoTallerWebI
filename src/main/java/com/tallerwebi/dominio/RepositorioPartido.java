package com.tallerwebi.dominio;

public interface RepositorioPartido {
    void guardarPartido(Partido partido);
    Partido buscarPorId(Long idPartido);
}