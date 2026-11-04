package com.tallerwebi.presentacion;

import java.math.BigDecimal;

public class ProcesoPagoDTO {

  private String token;
  private String issuerId;
  private String paymentMethodId;
  private BigDecimal transactionAmount;
  private Integer installments;
  private String email;
  private String password;
  private String identificationType;
  private String identificationNumber;

  public String getToken() {
    return token;
  }

  public void setToken(String token) {
    this.token = token;
  }

  public String getIssuerId() {
    return issuerId;
  }

  public void setIssuerId(String issuerId) {
    this.issuerId = issuerId;
  }

  public String getPaymentMethodId() {
    return paymentMethodId;
  }

  public void setPaymentMethodId(String paymentMethodId) {
    this.paymentMethodId = paymentMethodId;
  }

  public BigDecimal getTransactionAmount() {
    return transactionAmount;
  }

  public void setTransactionAmount(BigDecimal transactionAmount) {
    this.transactionAmount = transactionAmount;
  }

  public Integer getInstallments() {
    return installments;
  }

  public void setInstallments(Integer installments) {
    this.installments = installments;
  }

  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  public String getPassword() {
    return password;
  }

  public void setPassword(String password) {
    this.password = password;
  }

  public String getIdentificationType() {
    return identificationType;
  }

  public void setIdentificationType(String identificationType) {
    this.identificationType = identificationType;
  }

  public String getIdentificationNumber() {
    return identificationNumber;
  }

  public void setIdentificationNumber(String identificationNumber) {
    this.identificationNumber = identificationNumber;
  }
}
