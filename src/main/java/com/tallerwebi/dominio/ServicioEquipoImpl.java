package com.tallerwebi.dominio;

import com.tallerwebi.dominio.excepcion.EquipoExistente;
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
  public void registrarEquipo(Equipo equipo) {
    repositorioEquipo.guardar(equipo);
  }

  @Override
  public List<Equipo> obtenerEquipos() {
    return repositorioEquipo.listar();
  }
}
