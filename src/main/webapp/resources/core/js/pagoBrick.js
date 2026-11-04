// 1. Obtener email y password pasados por parámetro desde la pantalla de registro
const urlParams = new URLSearchParams(window.location.search);
const adminEmail = urlParams.get('email') || "alumno_unlam_pago@gmail.com";
const adminPassword = urlParams.get('password') || "";

// 2. Inicializar SDK de Mercado Pago
const mp = new MercadoPago('TEST-8198ecc1-8632-48e8-b2ca-5939a2d9bb40', {
    locale: 'es-AR'
});
const bricksBuilder = mp.bricks();

const renderPaymentBrick = async (bricksBuilder) => {
    const settings = {
        initialization: {
            amount: 5000,
            payer: {
                email: adminEmail
            }
        },
        customization: {
            paymentMethods: {
                creditCard: "all",
                debitCard: "all"
            }
        },
        callbacks: {
            onReady: () => {
                console.log("Payment Brick montado exitosamente");
            },
            onSubmit: ({ selectedPaymentMethod, formData }) => {
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
                            email: adminEmail,
                            password: adminPassword
                        })
                    })
                        .then(async response => {
                            const data = await response.json();

                            // 1. Error del backend (ej: contraseña débil o vacía)
                            if (!response.ok || data.status === "error") {
                                alert("No se pudo completar el registro: " + (data.mensaje || "Datos inválidos"));
                                reject();
                                // Redirigir al registro (podés pasar el email para conservarlo si querés)
                                window.location.href = "/spring/nuevo-usuario";
                                return;
                            }

                            // 2. Pago aprobado con éxito
                            if (data.status === "approved") {
                                resolve();
                                alert("¡Cuenta de Administrador creada y pago aprobado con éxito!");
                                window.location.href = data.redirect || "/spring/lobbyAdmin";
                            } else {
                                // 3. Tarjeta rechazada o error en la transacción de Mercado Pago
                                alert("El pago no fue aprobado: " + (data.status_detail || data.status));
                                resolve();
                                window.location.href = "/spring/nuevo-usuario";
                            }
                        })
                        .catch(err => {
                            console.error("Error en la petición:", err);
                            alert("Ocurrió un error al comunicarse con el servidor.");
                            reject();
                            window.location.href = "/spring/nuevo-usuario";
                        });
                });
            },
            onError: (error) => {
                console.error("Error devuelto por el Brick:", error);
            }
        }
    };

    window.paymentBrickController = await bricksBuilder.create(
        'payment',
        'paymentBrick_container',
        settings
    );
};

renderPaymentBrick(bricksBuilder);