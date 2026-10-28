package com.tallerwebi.dominio;

import com.tallerwebi.dominio.excepcion.EquipoExistente;
import com.tallerwebi.dominio.excepcion.SiglasExistentes;
import com.tallerwebi.dominio.excepcion.TorneoExistente;
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
  public void registrarEquipo(Equipo equipo) throws EquipoExistente, SiglasExistentes {
    Equipo equipoEncontrado = repositorioEquipo.buscarEquipo(equipo.getNombre());

    if (equipoEncontrado != null) {
      throw new EquipoExistente();
    }

    Equipo equipoConSiglas = repositorioEquipo.buscarEquipoPorSiglas(equipo.getSiglas());

    if (equipoConSiglas != null) {
      throw new SiglasExistentes();
    }

    repositorioEquipo.guardar(equipo);
  }

  @Override
  public List<Equipo> obtenerEquipos() {
    return repositorioEquipo.listar();
  }

  @Override
  public void actualizarEquipo(Equipo equipo) throws EquipoExistente, SiglasExistentes {
    Equipo equipoActual = repositorioEquipo.buscarPorId(equipo.getId());

    Equipo equipoConMismoNombre = repositorioEquipo.buscarEquipo(equipo.getNombre());

    if (equipoConMismoNombre != null && !equipoConMismoNombre.getId().equals(equipo.getId())) {
      throw new EquipoExistente();
    }

    Equipo equipoConMismasSiglas = repositorioEquipo.buscarEquipoPorSiglas(equipo.getSiglas());

    if (equipoConMismasSiglas != null && !equipoConMismasSiglas.getId().equals(equipo.getId())) {
      throw new SiglasExistentes();
    }

    if (equipo.getEscudo() != null && !equipo.getEscudo().isBlank()) {
      equipoActual.setEscudo(equipo.getEscudo());
    }

    repositorioEquipo.actualizar(equipo);
  }
}
