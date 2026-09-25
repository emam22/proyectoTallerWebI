package com.tallerwebi.presentacion;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.text.IsEqualIgnoringCase.equalToIgnoringCase;
import static org.mockito.Mockito.*;

import com.tallerwebi.dominio.ServicioTorneo;
import com.tallerwebi.dominio.Torneo;
import com.tallerwebi.dominio.excepcion.TorneoExistente;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.servlet.ModelAndView;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.text.IsEqualIgnoringCase.equalToIgnoringCase;

public class ControladorTorneoTest {

    private ControladorTorneo controladorTorneo;
    private Torneo torneoMock;
    private ServicioTorneo servicioTorneoMock;

    @BeforeEach
    public void init() {
        torneoMock = mock(Torneo.class);
        servicioTorneoMock = mock(ServicioTorneo.class);
        controladorTorneo = new ControladorTorneo(servicioTorneoMock);
    }

    @Test
    public void alCrearTorneoSeDebeCrearTorneoYRedirigirACreacionDeEquipo() throws Exception {

        // preparacion

        // ejecucion
        ModelAndView mav = controladorTorneo.crearTorneo(torneoMock);

        // validacion
        assertThat(mav.getViewName(), equalToIgnoringCase("redirect:/creacion-equipo"));
        verify(servicioTorneoMock, times(1)).registrarTorneo(torneoMock);
    }

    @Test
    public void siElTorneoYaExisteSeDebeVolverALFormulario() throws Exception {
        //preparacion
        doThrow(TorneoExistente.class).when(servicioTorneoMock).registrarTorneo(torneoMock);

        //ejecucion
        ModelAndView mav = controladorTorneo.crearTorneo(torneoMock);

        //validacion
        assertThat(mav.getViewName(), equalToIgnoringCase("formulario-torneo"));
        assertThat(mav.getModel().get("error").toString(), equalToIgnoringCase("Ya existe el torneo con ese nombre"));
    }


    @Test
    public void errorEnRegistrarTorneoDeberiaVolverAFormularioTorneoYMostrarError() throws Exception {
        // preparacion
        doThrow(RuntimeException.class).when(servicioTorneoMock).registrarTorneo(torneoMock);

        // ejecucion
        ModelAndView modelAndView = controladorTorneo.crearTorneo(torneoMock);

        // validacion
        assertThat(modelAndView.getViewName(), equalToIgnoringCase("formulario-torneo"));
        assertThat(modelAndView.getModel().get("error").toString(), equalToIgnoringCase("Error al crear el torneo"));
    }

    //agregar boton de volver al lobby admin en hmtl y generar test de validacion

}