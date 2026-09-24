package com.tallerwebi.dominio;

import com.tallerwebi.dominio.excepcion.TorneoExistente;
import com.tallerwebi.dominio.excepcion.UsuarioExistente;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service("ServicioTorneo")
@Transactional
public class ServicioTorneoImpl implements ServicioTorneo {

  private RepositorioTorneo repositorioTorneo;

  @Autowired
  public ServicioTorneoImpl(RepositorioTorneo repositorioTorneo) {
    this.repositorioTorneo = repositorioTorneo;
  }

  @Override
  public Torneo consultarTorneo(String nombre) {
    return repositorioTorneo.buscarTorneo(nombre);
  }

  @Override
  public void registrarTorneo(Torneo torneo) throws TorneoExistente, Exception {
    Torneo torneoEncontrado = repositorioTorneo.buscarTorneo(torneo.getNombre());

    if (torneoEncontrado != null) {
      throw new TorneoExistente();
    }
    repositorioTorneo.guardar(torneo);
  }
}
