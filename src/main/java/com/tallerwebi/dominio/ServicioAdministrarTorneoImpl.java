package com.tallerwebi.dominio;

import com.tallerwebi.dominio.excepcion.TorneoInvalidoException;
import jakarta.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service("ServicioAdministrarTorneo")
@Transactional
public class ServicioAdministrarTorneoImpl implements ServicioAdministrarTorneo {

  private static final String SIN_DATO = "sin dato";

  private RepositorioTorneo repositorioTorneo;
  private RepositorioCambioTorneo repositorioCambioTorneo;

  @Autowired
  public ServicioAdministrarTorneoImpl(
    RepositorioTorneo repositorioTorneo,
    RepositorioCambioTorneo repositorioCambioTorneo
  ) {
    this.repositorioTorneo = repositorioTorneo;
    this.repositorioCambioTorneo = repositorioCambioTorneo;
  }

  @Override
  public Torneo obtenerTorneo(Long idTorneo) {
    return repositorioTorneo.buscarTorneoPorId(idTorneo);
  }

  @Override
  public List<CambioTorneo> modificarParametros(Long idTorneo, Torneo datos)
    throws TorneoInvalidoException {
    ValidadorTorneo.validar(datos);

    Torneo torneo = repositorioTorneo.buscarTorneoPorId(idTorneo);
    if (torneo == null) {
      throw new IllegalArgumentException("No existe el torneo indicado");
    }

    List<CambioTorneo> cambios = new ArrayList<>();
    cambios.add(registrarSiCambio(torneo, "nombre", torneo.getNombre(), datos.getNombre()));
    cambios.add(
      registrarSiCambio(
        torneo,
        "cantidad de equipos",
        torneo.getCantidadDeEquipos(),
        datos.getCantidadDeEquipos()
      )
    );
    cambios.add(
      registrarSiCambio(
        torneo,
        "minimo de jugadores",
        torneo.getCantidadMinJugadores(),
        datos.getCantidadMinJugadores()
      )
    );
    cambios.add(
      registrarSiCambio(
        torneo,
        "maximo de jugadores",
        torneo.getCantidadMaxJugadores(),
        datos.getCantidadMaxJugadores()
      )
    );
    cambios.add(
      registrarSiCambio(
        torneo,
        "amarillas por suspension",
        torneo.getCantidadAmaSusp(),
        datos.getCantidadAmaSusp()
      )
    );
    cambios.add(
      registrarSiCambio(torneo, "ida y vuelta", torneo.getIdaYVuelta(), datos.getIdaYVuelta())
    );

    torneo.setNombre(datos.getNombre());
    torneo.setCantidadDeEquipos(datos.getCantidadDeEquipos());
    torneo.setCantidadMinJugadores(datos.getCantidadMinJugadores());
    torneo.setCantidadMaxJugadores(datos.getCantidadMaxJugadores());
    torneo.setCantidadAmaSusp(datos.getCantidadAmaSusp());
    torneo.setIdaYVuelta(datos.getIdaYVuelta());
    repositorioTorneo.guardar(torneo);

    cambios.removeIf(cambio -> cambio == null);
    return cambios;
  }

  @Override
  public List<CambioTorneo> historial(Long idTorneo) {
    return repositorioCambioTorneo.buscarPorTorneo(idTorneo);
  }

  private CambioTorneo registrarSiCambio(
    Torneo torneo,
    String campo,
    Object valorViejo,
    Object valorNuevo
  ) {
    String anterior = aTexto(valorViejo);
    String nuevo = aTexto(valorNuevo);
    if (anterior.equals(nuevo)) {
      return null;
    }
    CambioTorneo cambio = new CambioTorneo();
    cambio.setTorneo(torneo);
    cambio.setCampo(campo);
    cambio.setValorAnterior(anterior);
    cambio.setValorNuevo(nuevo);
    cambio.setFechaHora(LocalDateTime.now());
    repositorioCambioTorneo.guardar(cambio);
    return cambio;
  }

  private String aTexto(Object valor) {
    if (valor == null) {
      return SIN_DATO;
    }
    if (valor instanceof Boolean booleanValor) {
      return booleanValor ? "Si" : "No";
    }
    return String.valueOf(valor);
  }
}
