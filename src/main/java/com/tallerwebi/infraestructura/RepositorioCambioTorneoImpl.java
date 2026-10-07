package com.tallerwebi.infraestructura;

import com.tallerwebi.dominio.CambioTorneo;
import com.tallerwebi.dominio.RepositorioCambioTorneo;
import java.util.List;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class RepositorioCambioTorneoImpl implements RepositorioCambioTorneo {

  private SessionFactory sessionFactory;

  @Autowired
  public RepositorioCambioTorneoImpl(SessionFactory sessionFactory) {
    this.sessionFactory = sessionFactory;
  }

  @Override
  public void guardar(CambioTorneo cambio) {
    sessionFactory.getCurrentSession().persist(cambio);
  }

  @Override
  @SuppressWarnings("unchecked")
  public List<CambioTorneo> buscarPorTorneo(Long idTorneo) {
    return sessionFactory
      .getCurrentSession()
      .createQuery("from CambioTorneo where torneo.id = :idTorneo order by fechaHora desc")
      .setParameter("idTorneo", idTorneo)
      .list();
  }
}
