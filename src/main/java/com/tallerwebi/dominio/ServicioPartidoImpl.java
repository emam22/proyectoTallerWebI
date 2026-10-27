package com.tallerwebi.dominio;

import com.tallerwebi.presentacion.ResultadoPartidoDTO;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service("servicioPartido")
@Transactional
public class ServicioPartidoImpl implements ServicioPartido {

  private final RepositorioPartido repositorioPartido;
  private final ServicioEquipoTorneo servicioEquipoTorneo;
  private final ServicioEstadisticaJugador servicioEstadisticaJugador;

  @Autowired
  public ServicioPartidoImpl(
    RepositorioPartido repositorioPartido,
    ServicioEquipoTorneo servicioEquipoTorneo,
    ServicioEstadisticaJugador servicioEstadisticaJugador
  ) {
    this.repositorioPartido = repositorioPartido;
    this.servicioEquipoTorneo = servicioEquipoTorneo;
    this.servicioEstadisticaJugador = servicioEstadisticaJugador;
  }

  @Override
  public void registrarResultado(ResultadoPartidoDTO resultado) {}
}
