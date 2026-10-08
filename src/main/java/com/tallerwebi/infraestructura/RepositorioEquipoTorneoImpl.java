package com.tallerwebi.infraestructura;

import com.tallerwebi.dominio.Equipo;
import com.tallerwebi.dominio.EquipoTorneo;
import com.tallerwebi.dominio.RepositorioEquipoTorneo;
import java.util.List;
import org.hibernate.SessionFactory;
import org.hibernate.query.Query;
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
  public List<EquipoTorneo> obtenerTablaPosiciones(Long idTorneo) {
    List<EquipoTorneo> estadisticas = sessionFactory
      .getCurrentSession()
      .createQuery(
        "select et from EquipoTorneo et " +
        "where et.torneo.id = :idTorneo " +
        "order by et.puntos desc, " +
        "(et.golesAFavor - et.golesEnContra) desc, " +
        "et.golesAFavor DESC",
        EquipoTorneo.class
      )
      .setParameter("idTorneo", idTorneo)
      .getResultList();
    return estadisticas;
  }
}
