package com.tallerwebi.dominio;

import com.mercadopago.MercadoPagoConfig;
import com.mercadopago.client.common.IdentificationRequest;
import com.mercadopago.client.payment.PaymentClient;
import com.mercadopago.client.payment.PaymentCreateRequest;
import com.mercadopago.client.payment.PaymentPayerRequest;
import com.mercadopago.exceptions.MPApiException;
import com.mercadopago.exceptions.MPException;
import com.mercadopago.resources.payment.Payment;
import com.tallerwebi.presentacion.ProcesoPagoDTO;
import jakarta.annotation.PostConstruct;
import jakarta.transaction.Transactional;
import java.math.BigDecimal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service("servicioMercadoPago")
@Transactional
public class ServicioMercadoPagoImpl implements ServicioMercadoPago {

  private static final Logger LOGGER = LoggerFactory.getLogger(ServicioMercadoPagoImpl.class);

  @Value("${mercadopago.access.token:}")
  private String mpAccessToken;

  @PostConstruct
  public void init() {
    String tokenDePrueba =
      "TEST-8199175975228693-110300-cc866b289b741a3c07b81e1103a07153-1310914258";

    MercadoPagoConfig.setAccessToken(tokenDePrueba);

    if (LOGGER.isInfoEnabled()) {
      LOGGER.info("MercadoPago SDK inicializado con token directo.");
    }
  }

  @Override
  public Payment crearPago(ProcesoPagoDTO dto) throws MPException, MPApiException {
    PaymentClient client = new PaymentClient();

    PaymentCreateRequest.PaymentCreateRequestBuilder builder = PaymentCreateRequest
      .builder()
      .token(dto.getToken())
      .transactionAmount(new BigDecimal(String.valueOf(dto.getTransactionAmount())))
      .installments(dto.getInstallments())
      .paymentMethodId(dto.getPaymentMethodId())
      .description("Inscripcion a Torneo");

    // Corrección para PMD: "null" a la izquierda en la comparación
    if (
      dto.getIssuerId() != null &&
      !dto.getIssuerId().trim().isEmpty() &&
      !"null".equalsIgnoreCase(dto.getIssuerId().trim())
    ) {
      builder.issuerId(dto.getIssuerId().trim());
    }

    // Construccion del pagador
    PaymentPayerRequest payer = PaymentPayerRequest
      .builder()
      .email("alumno_unlam_pago@gmail.com")
      .identification(
        IdentificationRequest
          .builder()
          .type(dto.getIdentificationType() != null ? dto.getIdentificationType() : "DNI")
          .number(
            dto.getIdentificationNumber() != null ? dto.getIdentificationNumber() : "12345678"
          )
          .build()
      )
      .build();

    builder.payer(payer);

    return client.create(builder.build());
  }
}
