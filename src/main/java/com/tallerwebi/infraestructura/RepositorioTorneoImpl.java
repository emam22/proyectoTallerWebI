package com.tallerwebi.infraestructura;

import com.tallerwebi.dominio.RepositorioTorneo;
import com.tallerwebi.dominio.Torneo;
import com.tallerwebi.dominio.Usuario;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository("repositorioTorneo")
public class RepositorioTorneoImpl implements RepositorioTorneo {

    private SessionFactory sessionFactory;

    @Autowired
    public RepositorioTorneoImpl(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }


    @Override
    public Torneo buscarTorneo(String nombre) {
        return sessionFactory
                .getCurrentSession()
                .createQuery("from Torneo where nombre = :nombre", Torneo.class)
                .setParameter("nombre", nombre)
                .uniqueResult();
    }

    @Override
    public void guardar(Torneo torneo) {
        sessionFactory.getCurrentSession().persist(torneo);
    }
}
