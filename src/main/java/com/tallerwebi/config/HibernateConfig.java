package com.tallerwebi.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import javax.sql.DataSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.orm.hibernate5.HibernateTransactionManager;
import org.springframework.orm.hibernate5.LocalSessionFactoryBean;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@EnableTransactionManagement
public class HibernateConfig {

  private static final Properties PROPIEDADES_LOCALES = cargarPropiedadesLocales();

  /**
   * Carga el archivo opcional db-local.properties, usado para configuracion local de la
   * maquina (que no debe compartirse por git).
   *
   * @return las propiedades locales, o vacias si el archivo no existe o no se puede leer
   */
  private static Properties cargarPropiedadesLocales() {
    Properties propiedades = new Properties();
    try (
      InputStream archivo = Thread
        .currentThread()
        .getContextClassLoader()
        .getResourceAsStream("db-local.properties")
    ) {
      if (archivo != null) {
        propiedades.load(archivo);
      }
    } catch (IOException e) {
      propiedades.clear();
    }
    return propiedades;
  }

  /**
   * Obtiene una variable de entorno, con fallback a db-local.properties y a un valor por
   * defecto. Prioridad: entorno &gt; archivo local &gt; defecto.
   *
   * @param nombre nombre de la variable (p. ej. DB_PORT)
   * @param valorPorDefecto valor a usar si no esta definida en ninguno de los dos lugares
   * @return el valor efectivo
   */
  private static String obtenerVariable(String nombre, String valorPorDefecto) {
    String valor = System.getenv(nombre);
    if (valor == null) {
      valor = PROPIEDADES_LOCALES.getProperty(nombre, valorPorDefecto);
    }
    return valor;
  }

  @Bean
  public DataSource dataSource() {
    DriverManagerDataSource dataSource = new DriverManagerDataSource();

    String dbHost = obtenerVariable("DB_HOST", "localhost");
    String dbPort = obtenerVariable("DB_PORT", "3306");
    String dbName = obtenerVariable("DB_NAME", "tallerwebi");
    String dbUser = obtenerVariable("DB_USER", "user");
    String dbPassword = obtenerVariable("DB_PASSWORD", "user");

    String url = String.format(
      "jdbc:mysql://%s:%s/%s?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true",
      dbHost,
      dbPort,
      dbName
    );

    dataSource.setUrl(url);
    dataSource.setUsername(dbUser);
    dataSource.setPassword(dbPassword);
    dataSource.setDriverClassName("com.mysql.cj.jdbc.Driver");
    return dataSource;
  }

  @Bean
  public LocalSessionFactoryBean sessionFactory(DataSource dataSource) {
    LocalSessionFactoryBean sessionFactory = new LocalSessionFactoryBean();
    sessionFactory.setDataSource(dataSource);
    sessionFactory.setPackagesToScan("com.tallerwebi.dominio");
    sessionFactory.setHibernateProperties(hibernateProperties());
    return sessionFactory;
  }

  @Bean
  public HibernateTransactionManager transactionManager() {
    return new HibernateTransactionManager(sessionFactory(dataSource()).getObject());
  }

  private Properties hibernateProperties() {
    Properties properties = new Properties();
    properties.setProperty("hibernate.dialect", "org.hibernate.dialect.MySQLDialect");
    properties.setProperty("hibernate.show_sql", "true");
    properties.setProperty("hibernate.format_sql", "true");
    properties.setProperty("hibernate.hbm2ddl.auto", "create");
    properties.setProperty("hibernate.connection.characterEncoding", "utf8");
    properties.setProperty("hibernate.connection.CharSet", "utf8");
    properties.setProperty("hibernate.connection.useUnicode", "true");
    return properties;
  }
}
