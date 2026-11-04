package com.tallerwebi.presentacion;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.comparesEqualTo;
import static org.hamcrest.Matchers.equalTo;

import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

public class ProcesoPagoDTOTest {

  @Test
  public void testGettersYSetters() {
    BigDecimal montoEsperado = BigDecimal.valueOf(5000.0);

    ProcesoPagoDTO dto = new ProcesoPagoDTO();
    dto.setToken("tok_123");
    dto.setIssuerId("issuer_1");
    dto.setPaymentMethodId("visa");
    dto.setTransactionAmount(montoEsperado);
    dto.setInstallments(1);
    dto.setIdentificationType("DNI");
    dto.setIdentificationNumber("12345678");
    dto.setEmail("test@unlam.edu.ar");
    dto.setPassword("Secret123!");

    assertThat(dto.getToken(), equalTo("tok_123"));
    assertThat(dto.getIssuerId(), equalTo("issuer_1"));
    assertThat(dto.getPaymentMethodId(), equalTo("visa"));
    assertThat(dto.getTransactionAmount(), comparesEqualTo(montoEsperado));
    assertThat(dto.getInstallments(), equalTo(1));
    assertThat(dto.getIdentificationType(), equalTo("DNI"));
    assertThat(dto.getIdentificationNumber(), equalTo("12345678"));
    assertThat(dto.getEmail(), equalTo("test@unlam.edu.ar"));
    assertThat(dto.getPassword(), equalTo("Secret123!"));
  }
}
