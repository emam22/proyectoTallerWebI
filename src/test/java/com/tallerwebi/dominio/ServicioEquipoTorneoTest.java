package com.tallerwebi.dominio;

import com.tallerwebi.dominio.excepcion.EquipoExistente;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;


public class ServicioEquipoTorneoTest {

    private ServicioEquipoTorneo servicioEquipoTorneo;

    private RepositorioEquipoTorneo repositorioEquipoTorneoMock;

    private ServicioEquipo servicioEquipoMock;

    @BeforeEach
    public void init() {
        this.repositorioEquipoTorneoMock = mock(RepositorioEquipoTorneo.class);
        this.servicioEquipoMock = mock(ServicioEquipo.class);
        this.servicioEquipoTorneo = new ServicioEquipoTorneoImpl(this.repositorioEquipoTorneoMock, this.servicioEquipoMock);
    }

    @Test
    public void deberiaCrearEquipoEInscribirloEnTorneo() throws EquipoExistente {

        // preparacion
        Equipo equipo = new Equipo();
        equipo.setNombre("Los Pumas");

        Torneo torneo = new Torneo();

        when(this.servicioEquipoMock.consultarEquipo("Los Pumas"))
                .thenReturn(null);

        // ejecucion
        this.servicioEquipoTorneo.crearEquipoEInscribirlo(equipo, torneo);

        // verificacion
        verify(this.servicioEquipoMock)
                .consultarEquipo("Los Pumas");

        verify(this.servicioEquipoMock)
                .registrarEquipo(equipo);

        verify(this.repositorioEquipoTorneoMock)
                .guardar(any(EquipoTorneo.class));
    }


    @Test
    public void deberiaLanzarExcepcionSiElEquipoYaExiste() throws EquipoExistente {

        // preparacion
        Equipo equipo = new Equipo();
        equipo.setNombre("Los Pumas");

        Torneo torneo = new Torneo();

        when(this.servicioEquipoMock.consultarEquipo("Los Pumas"))
                .thenReturn(equipo);

        // ejecucion y verificacion
        assertThrows(
                EquipoExistente.class,
                () -> this.servicioEquipoTorneo
                        .crearEquipoEInscribirlo(equipo, torneo)
        );


        verify(this.servicioEquipoMock, times(0))
                .registrarEquipo(equipo);

        verify(this.repositorioEquipoTorneoMock, times(0))
                .guardar(any(EquipoTorneo.class));
    }

    @Test
    public void deberiaContarEquiposPorTorneo() {

        // preparacion
        Long idTorneo = 5L;

        when(this.repositorioEquipoTorneoMock
                .contarEquiposPorTorneo(idTorneo))
                .thenReturn(8);

        // ejecucion
        int cantidad = this.servicioEquipoTorneo
                .contarEquiposPorTorneo(idTorneo);

        // verificacion
        assertThat(cantidad, is(8));

        verify(this.repositorioEquipoTorneoMock, times(1))
                .contarEquiposPorTorneo(idTorneo);
    }










}

