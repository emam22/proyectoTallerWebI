package com.tallerwebi.dominio;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

import com.tallerwebi.dominio.excepcion.UsuarioExistente;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ServicioLoginTest {

  private ServicioLogin servicioLogin;
  private RepositorioUsuario repositorioUsuarioMock;

  @BeforeEach
  public void init() {
    this.repositorioUsuarioMock = mock(RepositorioUsuario.class);
    this.servicioLogin = new ServicioLoginImpl(this.repositorioUsuarioMock);
  }

  @Test
  public void consultarUsuarioDeberiaLlamarAlRepositorio() {
    // preparacion
    String email = "test@test.com";
    String password = "password";
    Usuario usuarioEsperado = new Usuario();
    when(this.repositorioUsuarioMock.buscarUsuario(email, password)).thenReturn(usuarioEsperado);

    // ejecucion
    Usuario usuarioObtenido = this.servicioLogin.consultarUsuario(email, password);

    // validacion
    assertThat(usuarioObtenido, equalTo(usuarioEsperado));
    verify(this.repositorioUsuarioMock, times(1)).buscarUsuario(email, password);
  }

  @Test
  public void registrarUsuarioSiNoExisteDeberiaGuardarlo() throws UsuarioExistente, Exception {
    // preparacion
    Usuario usuario = new Usuario();
    usuario.setEmail("nuevo@test.com");
    usuario.setPassword("Abcd.1234!");
    when(this.repositorioUsuarioMock.buscarUsuario(usuario.getEmail(), usuario.getPassword()))
      .thenReturn(null);

    // ejecucion
    this.servicioLogin.registrar(usuario);

    // validacion
    verify(this.repositorioUsuarioMock, times(1)).guardar(usuario);
  }

  @Test
  public void registrarUsuarioSiNoExisteDeberiaAsignarRolUsuarioPorDefecto()
    throws UsuarioExistente, Exception {
    // preparacion
    Usuario usuario = new Usuario();
    usuario.setEmail("nuevo@test.com");
    usuario.setPassword("Abcd.1234!");
    when(this.repositorioUsuarioMock.buscarUsuario(usuario.getEmail(), usuario.getPassword()))
      .thenReturn(null);

    // ejecucion
    this.servicioLogin.registrar(usuario);

    // validacion
    assertThat(usuario.getRol(), equalTo("usuario"));
    verify(this.repositorioUsuarioMock, times(1)).guardar(usuario);
  }

  @Test
  public void registrarUsuarioSiExisteDeberiaLanzarExcepcion() {
    // preparacion
    Usuario usuario = new Usuario();
    usuario.setEmail("existe@test.com");
    usuario.setPassword("123");
    when(this.repositorioUsuarioMock.buscarUsuario(usuario.getEmail(), usuario.getPassword()))
      .thenReturn(new Usuario());

    // ejecucion y validacion
    assertThrows(UsuarioExistente.class, () -> this.servicioLogin.registrar(usuario));
    verify(this.repositorioUsuarioMock, times(0)).guardar(usuario);
  }

  @Test
  public void registrarUsuarioConContrasenaDebilDeberiaLanzarExcepcion() {
    Usuario usuario = new Usuario();
    usuario.setEmail("nuevo@test.com");
    usuario.setPassword("123");

    when(this.repositorioUsuarioMock.buscarUsuario(usuario.getEmail(), usuario.getPassword()))
      .thenReturn(null);

    Exception excepcion = assertThrows(
      Exception.class,
      () -> this.servicioLogin.registrar(usuario)
    );

    assertThat(excepcion.getMessage(), equalTo("La contraseña es demasiado debil"));
    verify(this.repositorioUsuarioMock, times(0)).guardar(usuario);
  }

  @Test
  public void registrarUsuarioConRolExistenteNoDeberiaSobreescribirlo() throws Exception {
    Usuario admin = new Usuario();
    admin.setEmail("admin@test.com");
    admin.setPassword("Abcd.1234!");
    admin.setRol("admin");

    when(this.repositorioUsuarioMock.buscarUsuario(admin.getEmail(), admin.getPassword()))
      .thenReturn(null);

    this.servicioLogin.registrar(admin);

    assertThat(admin.getRol(), equalTo("admin"));
    verify(this.repositorioUsuarioMock, times(1)).guardar(admin);
  }

  @Test
  public void registrarUsuarioConContrasenaVaciaDeberiaLanzarExcepcion() {
    Usuario usuario = new Usuario();
    usuario.setEmail("nuevo@test.com");
    usuario.setPassword("   ");

    when(this.repositorioUsuarioMock.buscarUsuario(usuario.getEmail(), usuario.getPassword()))
      .thenReturn(null);

    Exception excepcion = assertThrows(
      Exception.class,
      () -> this.servicioLogin.registrar(usuario)
    );

    assertThat(excepcion.getMessage(), equalTo("La contraseña no puede estar vacía."));
    verify(this.repositorioUsuarioMock, never()).guardar(usuario);
  }

  @Test
  public void validarFortalezaDeberiaRetornarFuerteCuandoCumpleTodosLosRequisitos() {
    // Al menos 8 caracteres, >= 4 letras, >= 2 números, >= 2 especiales
    String resultado = this.servicioLogin.validarFortaleza("Abcd12-_");
    assertThat(resultado, equalTo("FUERTE"));
  }

  @Test
  public void validarFortalezaDeberiaRetornarMedianaCuandoTieneOchoCaracteresUnNumeroYUnEspecial() {
    // 8 caracteres, pero no llega a 4 letras o no llega a 2 números/especiales
    String resultado = this.servicioLogin.validarFortaleza("Abcde1-x");
    assertThat(resultado, equalTo("MEDIANA"));
  }

  @Test
  public void validarFortalezaDeberiaRetornarDebilCuandoNoAlcanzaLaLongitudMinima() {
    String resultado = this.servicioLogin.validarFortaleza("Ab1!");
    assertThat(resultado, equalTo("DEBIL"));
  }

  @Test
  public void validarFortalezaDeberiaRetornarDebilCuandoTieneOchoCaracteresPeroFaltanNumerosOEspeciales() {
    String soloLetras = this.servicioLogin.validarFortaleza("Abcdefghij");
    String sinEspeciales = this.servicioLogin.validarFortaleza("Abcdefgh12");

    assertThat(soloLetras, equalTo("DEBIL"));
    assertThat(sinEspeciales, equalTo("DEBIL"));
  }

  @Test
  public void validarFortalezaDeberiaRetornarInvalidaCuandoElPasswordEsNuloOBlanco() {
    String resultadoNulo = this.servicioLogin.validarFortaleza(null);
    String resultadoEspacios = this.servicioLogin.validarFortaleza("     ");

    assertThat(resultadoNulo, equalTo("INVALIDA"));
    assertThat(resultadoEspacios, equalTo("INVALIDA"));
  }
}
