package com.tallerwebi.infraestructura;

import com.tallerwebi.dominio.Jornada;
import com.tallerwebi.dominio.RepositorioJornada;
import java.util.List;
import org.hibernate.SessionFactory;
import org.springframework.stereotype.Repository;

@Repository("repositorioJornada")
public class RepositorioJornadaImpl implements RepositorioJornada {

  private final SessionFactory sessionFactory;

  public RepositorioJornadaImpl(SessionFactory sessionFactory) {
    this.sessionFactory = sessionFactory;
  }

  @Override
  public void guardar(Jornada jornada) {
    if (jornada == null) {
      throw new IllegalArgumentException("La jornada no puede ser null");
    }

    if (jornada.getTorneo() == null) {
      throw new IllegalArgumentException("La jornada no tiene torneo asociado");
    }

    sessionFactory.getCurrentSession().persist(jornada);
  }

  @Override
  public void guardarListaFixture(List<Jornada> jornadas) {
    for (Jornada jornada : jornadas) {
      sessionFactory.getCurrentSession().persist(jornada);
    }
  }

  @Override
  public List<Jornada> obtenerJornadasPorTorneo(Long torneoId) {
    return sessionFactory
      .getCurrentSession()
      .createQuery(
        "select distinct j " +
        "from Jornada j " +
        "left join fetch j.partidos p " +
        "left join fetch p.equipoLocal " +
        "left join fetch p.equipoVisitante " +
        "where j.torneo.id = :torneoId " +
        "order by j.numero",
        Jornada.class
      )
      .setParameter("torneoId", torneoId)
      .getResultList();
  }
}
