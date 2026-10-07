package com.tallerwebi.dominio;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.tallerwebi.dominio.excepcion.TorneoInvalidoException;
import org.junit.jupiter.api.Test;

public class ValidadorTorneoTest {

  private Torneo torneoValido() {
    Torneo torneo = new Torneo();
    torneo.setNombre("Torneo Demo");
    torneo.setCantidadDeEquipos(4);
    torneo.setCantidadMinJugadores(10);
    torneo.setCantidadMaxJugadores(15);
    torneo.setCantidadAmaSusp(3);
    torneo.setIdaYVuelta(true);
    return torneo;
  }

  @Test
  public void unTorneoValidoNoLanzaError() {
    assertDoesNotThrow(() -> ValidadorTorneo.validar(torneoValido()));
  }

  @Test
  public void unTorneoNuloLanzaError() {
    assertThrows(TorneoInvalidoException.class, () -> ValidadorTorneo.validar(null));
  }

  @Test
  public void unNombreVacioLanzaError() {
    Torneo torneo = torneoValido();
    torneo.setNombre("   ");
    assertThrows(TorneoInvalidoException.class, () -> ValidadorTorneo.validar(torneo));
  }

  @Test
  public void unCampoNumericoSinValorLanzaError() {
    Torneo torneo = torneoValido();
    torneo.setCantidadDeEquipos(null);
    assertThrows(TorneoInvalidoException.class, () -> ValidadorTorneo.validar(torneo));
  }

  @Test
  public void menosDeDosEquiposLanzaError() {
    Torneo torneo = torneoValido();
    torneo.setCantidadDeEquipos(1);
    assertThrows(TorneoInvalidoException.class, () -> ValidadorTorneo.validar(torneo));
  }

  @Test
  public void menosDeDosJugadoresLanzaError() {
    Torneo torneo = torneoValido();
    torneo.setCantidadMinJugadores(1);
    assertThrows(TorneoInvalidoException.class, () -> ValidadorTorneo.validar(torneo));
  }

  @Test
  public void amarillasNegativasLanzanError() {
    Torneo torneo = torneoValido();
    torneo.setCantidadAmaSusp(-1);
    assertThrows(TorneoInvalidoException.class, () -> ValidadorTorneo.validar(torneo));
  }

  @Test
  public void minimoMayorQueMaximoLanzaError() {
    Torneo torneo = torneoValido();
    torneo.setCantidadMinJugadores(20);
    torneo.setCantidadMaxJugadores(15);
    assertThrows(TorneoInvalidoException.class, () -> ValidadorTorneo.validar(torneo));
  }
}
