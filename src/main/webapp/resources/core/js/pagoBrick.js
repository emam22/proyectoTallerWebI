/* global MercadoPago */
// El email y la contrase\u00f1a del administrador los lee el servidor de la sesi\u00f3n.
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
              identificationNumber: formData.payer.identification ? formData.payer.identification.number : "12345678"
            })
          })
            .then(async (response) => {
              const data = await response.json();

              if (!response.ok || data.status === "error") {
                alert("No se pudo completar el registro: " + (data.mensaje || "Datos inv\u00e1lidos"));
                reject();
                window.location.href = "/spring/nuevo-usuario";
                return;
              }

              if (data.status === "approved") {
                resolve();
                window.MensajesProyecto.mostrar(
                  "\u00a1Cuenta de Administrador creada y pago aprobado con \u00e9xito!",
                  "ok"
                );
                window.location.href = data.redirect || "/spring/lobbyAdmin";
              } else {
                alert("El pago no fue aprobado: " + (data.status_detail || data.status));
                resolve();
                window.location.href = "/spring/nuevo-usuario";
              }
            })
            .catch((err) => {
              alert("Ocurri\u00f3 un error al comunicarse con el servidor.");
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