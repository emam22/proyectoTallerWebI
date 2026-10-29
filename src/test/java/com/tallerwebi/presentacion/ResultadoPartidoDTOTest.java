package com.tallerwebi.presentacion;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

import org.junit.jupiter.api.Test;

public class ResultadoPartidoDTOTest {

  @Test
  public void testGettersSettersYConstructores() {
    ResultadoPartidoDTO dto = new ResultadoPartidoDTO();

    // Ajustá los nombres de atributos según los campos de tu ResultadoPartidoDTO
    // Por ejemplo:
    dto.setIdPartido(1L);
    dto.setGolesLocal(2);
    dto.setGolesVisitante(1);

    assertThat(dto, notNullValue());
    assertThat(dto.getIdPartido(), equalTo(1L));
    assertThat(dto.getGolesLocal(), equalTo(2));
    assertThat(dto.getGolesVisitante(), equalTo(1));
  }
}
