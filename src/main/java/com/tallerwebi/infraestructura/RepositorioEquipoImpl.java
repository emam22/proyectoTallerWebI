package com.tallerwebi.infraestructura;

import com.tallerwebi.dominio.Equipo;
import com.tallerwebi.dominio.RepositorioEquipo;
import java.util.List;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository("repositorioEquipo")
public class RepositorioEquipoImpl implements RepositorioEquipo {

  private SessionFactory sessionFactory;

  @Autowired
  public RepositorioEquipoImpl(SessionFactory sessionFactory) {
    this.sessionFactory = sessionFactory;
  }

  @Override
  public Equipo buscarPorId(Long id) {
    return sessionFactory
      .getCurrentSession()
      .createQuery("from Equipo where id = :id", Equipo.class)
      .setParameter("id", id)
      .uniqueResult();
  }

  @Override
  public Equipo buscarEquipo(String nombre) {
    return sessionFactory
      .getCurrentSession()
      .createQuery("from Equipo where nombre = :nombre", Equipo.class)
      .setParameter("nombre", nombre)
      .uniqueResult();
  }

  @Override
  public void guardar(Equipo equipo) {
    sessionFactory.getCurrentSession().persist(equipo);
  }

  @Override
  public List<Equipo> listar() {
    return sessionFactory
      .getCurrentSession()
      .createQuery("from Equipo", Equipo.class)
      .getResultList();
  }

  @Override
  public void actualizar(Equipo equipo) {
    sessionFactory.getCurrentSession().merge(equipo);
  }

  @Override
  public Equipo buscarEquipoPorSiglas(String siglas) {
    String hql = "FROM Equipo e WHERE e.siglas = :siglas";

    List<Equipo> equipos = sessionFactory
      .getCurrentSession()
      .createQuery(hql, Equipo.class)
      .setParameter("siglas", siglas)
      .getResultList();

    return equipos.isEmpty() ? null : equipos.get(0);
  }
}
