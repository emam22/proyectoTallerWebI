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
  private static final String ID_TORNEO = "idTorneo";

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
      .setParameter(ID_TORNEO, idTorneo)
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
      .setParameter(ID_TORNEO, idTorneo)
      .uniqueResult();

    return cantidad > 0;
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
      .setParameter(ID_TORNEO, idTorneo)
      .getResultList();
    return estadisticas;
  }

  @Override
  public List<EquipoTorneo> obtenerEquiposPorTorneo(Long idTorneo) {
    return sessionFactory
      .getCurrentSession()
      .createQuery(
        "SELECT et FROM EquipoTorneo et WHERE et.torneo.id = :idTorneo",
        EquipoTorneo.class
      )
      .setParameter(ID_TORNEO, idTorneo)
      .getResultList();
  }

  @Override
  public EquipoTorneo buscarEquipoTorneoPorId(Long idEquipo, Long idTorneo) {
    String hql =
      "FROM EquipoTorneo et " + "WHERE et.equipo.id = :idEquipo " + "AND et.torneo.id = :idTorneo";

    List<EquipoTorneo> resultados = sessionFactory
      .getCurrentSession()
      .createQuery(hql, EquipoTorneo.class)
      .setParameter("idEquipo", idEquipo)
      .setParameter("idTorneo", idTorneo)
      .getResultList();

    return resultados.isEmpty() ? null : resultados.get(0);
  }

  @Override
  public void eliminar(EquipoTorneo equipoTorneo) {
    sessionFactory.getCurrentSession().delete(equipoTorneo);
  }

  @Override
  public Equipo buscarEquipoPorId(Long idEquipoDelTorneo) {
    Equipo equipoBuscado;

    equipoBuscado =
      sessionFactory
        .getCurrentSession()
        .createQuery(
          "Select equipo From EquipoTorneo et where et.equipo.id = :idEquipoDelTorneo",
          Equipo.class
        )
        .setParameter("idEquipoDelTorneo", idEquipoDelTorneo)
        .uniqueResult();
    return equipoBuscado;
  }

  @Override
  public void eliminarPorTorneo(Long idTorneo) {
    String hql = "DELETE FROM EquipoTorneo et WHERE et.torneo.id = :idTorneo";
    sessionFactory
      .getCurrentSession()
      .createQuery(hql)
      .setParameter("idTorneo", idTorneo)
      .executeUpdate();
  }
}
