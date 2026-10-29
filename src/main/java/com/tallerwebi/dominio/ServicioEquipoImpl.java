package com.tallerwebi.dominio;

import com.tallerwebi.dominio.excepcion.EquipoExistente;
import com.tallerwebi.dominio.excepcion.SiglaExistente;
import com.tallerwebi.dominio.excepcion.SiglaInvalida;
import jakarta.transaction.Transactional;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service("servicioEquipo")
@Transactional
public class ServicioEquipoImpl implements ServicioEquipo {

  private RepositorioEquipo repositorioEquipo;

  @Autowired
  public ServicioEquipoImpl(RepositorioEquipo repositorioEquipo) {
    this.repositorioEquipo = repositorioEquipo;
  }

  @Override
  public Equipo consultarEquipoPorId(Long id) {
    return repositorioEquipo.buscarPorId(id);
  }

  @Override
  public Equipo consultarEquipo(String nombre) {
    return repositorioEquipo.buscarEquipo(nombre);
  }

  @Override
  public void registrarEquipo(Equipo equipo) throws EquipoExistente, SiglaExistente, SiglaInvalida {
    Equipo equipoEncontrado = repositorioEquipo.buscarEquipo(equipo.getNombre());

    if (equipoEncontrado != null) {
      throw new EquipoExistente();
    }

    Equipo equipoConSigla = repositorioEquipo.buscarEquipoPorSigla(equipo.getSigla());

    if (equipoConSigla != null) {
      throw new SiglaExistente();
    }

    validarSigla(equipo.getSigla());

    repositorioEquipo.guardar(equipo);
  }

  @Override
  public List<Equipo> obtenerEquipos() {
    return repositorioEquipo.listar();
  }

  private void validarSigla(String sigla) throws SiglaInvalida {
    if (sigla == null || sigla.trim().length() < 3 || sigla.trim().length() > 5) {
      throw new SiglaInvalida();
    }
  }

  @Override
  public void actualizarEquipo(Equipo equipo)
    throws EquipoExistente, SiglaExistente, SiglaInvalida {
    Equipo equipoActual = repositorioEquipo.buscarPorId(equipo.getId());

    Equipo equipoConMismoNombre = repositorioEquipo.buscarEquipo(equipo.getNombre());

    if (equipoConMismoNombre != null && !equipoConMismoNombre.getId().equals(equipo.getId())) {
      throw new EquipoExistente();
    }

    Equipo equipoConMismaSigla = repositorioEquipo.buscarEquipoPorSigla(equipo.getSigla());

    if (equipoConMismaSigla != null && !equipoConMismaSigla.getId().equals(equipo.getId())) {
      throw new SiglaExistente();
    }

    if (equipo.getEscudo() != null && !equipo.getEscudo().isBlank()) {
      equipoActual.setEscudo(equipo.getEscudo());
    }

    validarSigla(equipo.getSigla());

    repositorioEquipo.actualizar(equipo);
  }

  @Override
  public Equipo consultarEquipoJugador(Long idJugador) {
    return repositorioEquipo.buscarEquipoPorIdJugador(idJugador);
  }
}
