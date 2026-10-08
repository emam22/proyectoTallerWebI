package com.tallerwebi.infraestructura;

import com.tallerwebi.dominio.EquipoTorneo;
import com.tallerwebi.dominio.RepositorioEquipoTorneo;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository("repositorioEquipoTorneo")
public class RepositorioEquipoTorneoImpl implements RepositorioEquipoTorneo {

  private SessionFactory sessionFactory;

  @Autowired
  public RepositorioEquipoTorneoImpl(SessionFactory sessionFactory) {
    this.sessionFactory = sessionFactory;
  }

  @Override
  public void guardar(EquipoTorneo equipoTorneo) {
    sessionFactory.getCurrentSession().persist(equipoTorneo);
  }

  @Override
  public int contarEquiposPorTorneo(Long idTorneo) {
    Long cantidad = sessionFactory
      .getCurrentSession()
      .createQuery(
        "select count(et) " + "from EquipoTorneo et " + "where et.torneo.id = :idTorneo",
        Long.class
      )
      .setParameter("idTorneo", idTorneo)
      .uniqueResult();

    return cantidad.intValue();
  }

  @Override
  public boolean existeEquipoEnTorneo(Long idEquipo, Long idTorneo) {
    String hql =
      """
      SELECT COUNT(et)
      FROM EquipoTorneo et
      WHERE et.equipo.id = :idEquipo
      AND et.torneo.id = :idTorneo
      """;

    Long cantidad = (Long) this.sessionFactory.getCurrentSession()
      .createQuery(hql)
      .setParameter("idEquipo", idEquipo)
      .setParameter("idTorneo", idTorneo)
      .uniqueResult();

    return cantidad > 0;
  }
}
