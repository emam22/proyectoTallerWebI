package com.tallerwebi.dominio;

import com.tallerwebi.dominio.excepcion.TorneoExistente;
import com.tallerwebi.dominio.excepcion.UsuarioExistente;
import jakarta.transaction.Transactional;
import java.util.List;
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
  public Torneo consultarTorneoPorId(Long id) {
    return repositorioTorneo.buscarTorneoPorId(id);
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
    ValidadorTorneo.validar(torneo);

    repositorioTorneo.guardar(torneo);
  }

  @Override
  public List<Torneo> obtenerTorneos() {
    return repositorioTorneo.listar();
  }
}
