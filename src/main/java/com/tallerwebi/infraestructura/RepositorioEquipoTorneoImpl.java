package com.tallerwebi.infraestructura;

import com.tallerwebi.dominio.EquipoTorneo;
import com.tallerwebi.dominio.RepositorioEquipoTorneo;
import java.util.List;
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
  public List<EquipoTorneo> buscarPorTorneo(Long idTorneo) {
    return sessionFactory
      .getCurrentSession()
      .createQuery("from EquipoTorneo where torneo.id = :idTorneo", EquipoTorneo.class)
      .setParameter("idTorneo", idTorneo)
      .getResultList();
  }
}
