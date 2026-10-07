package com.tallerwebi.dominio;

import com.tallerwebi.dominio.excepcion.FixtureInvalidoException;
import java.util.List;

public interface ServicioFixture {
  List<Fecha> generarFixture(Long idTorneo) throws FixtureInvalidoException;
  boolean hayFixture(Long idTorneo);
  List<Fecha> obtenerFechas(Long idTorneo);
}
