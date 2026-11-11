/* global MercadoPago */
const urlParams = new URLSearchParams(window.location.search);
const adminEmail = urlParams.get("email");
const adminPassword = urlParams.get("password");

const emailTestComprador = "TESTUSER1664296236612020723@testuser.com";

const mp = new MercadoPago("TEST-e2192b44-67af-42b5-81e8-8379b2da8959", {
  locale: "es-AR"
});

const bricksBuilder = mp.bricks();

const renderPaymentBrick = async (bricksBuilderInstance) => {
  const settings = {
    initialization: {
      amount: 5000.0,
      preferenceId: null,
      payer: {
        email: emailTestComprador
      }
    },
    customization: {
      paymentMethods: {
        ticket: "all",
        bankTransfer: "all",
        creditCard: "all",
        debitCard: "all",
        mercadoPago: "none"
      }
    },
    callbacks: {
      onReady: () => {},
      onSubmit: ({ formData }) => {
        return new Promise((resolve, reject) => {
          fetch("/spring/api/pagos/procesar", {
            method: "POST",
            headers: {
              "Content-Type": "application/json"
            },
            body: JSON.stringify({
              token: formData.token,
              issuerId: formData.issuer_id ? String(formData.issuer_id) : null,
              paymentMethodId: formData.payment_method_id,
              transactionAmount: formData.transaction_amount,
              installments: formData.installments,
              identificationType: formData.payer.identification ? formData.payer.identification.type : "DNI",
              identificationNumber: formData.payer.identification ? formData.payer.identification.number : "12345678",
              email: adminEmail, // El email real que se guardará en la BD para el admin
              password: adminPassword
            })
          })
              .then(async (response) => {
                const data = await response.json();

                if (!response.ok || data.status === "error") {
                  alert("No se pudo completar el registro: " + (data.mensaje || "Datos inválidos"));
                  reject();
                  window.location.href = "/spring/nuevo-usuario";
                  return;
                }

                if (data.status === "approved") {
                  resolve();
                  alert("¡Cuenta de Administrador creada y pago aprobado con éxito!");
                  window.location.href = data.redirect || "/spring/lobbyAdmin";
                } else {
                  alert("El pago no fue aprobado: " + (data.status_detail || data.status));
                  resolve();
                  window.location.href = "/spring/nuevo-usuario";
                }
              })
              .catch((err) => {
                alert("Ocurrió un error al comunicarse con el servidor.");
                reject(err);
                window.location.href = "/spring/nuevo-usuario";
              });
        });
      },
      onError: () => {
        alert("Error al cargar la pasarela de pagos.");
        window.location.href = "/spring/nuevo-usuario";
      }
    }
  };

  window.paymentBrickController = await bricksBuilderInstance.create(
      "payment",
      "paymentBrick_container",
      settings
  );
};

renderPaymentBrick(bricksBuilder);