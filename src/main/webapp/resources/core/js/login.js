import { validarCamposDeLogin } from "./login_funciones.js";

const btnLoginNode = document.getElementById("btn-login");
const inputEmailNode = document.getElementById("email");
const inputPasswordNode = document.getElementById("password");

function actualizarBotonLogin() {
  const email = inputEmailNode.value;
  const password = inputPasswordNode.value;
  btnLoginNode.disabled = !validarCamposDeLogin(email, password);
}

// Cubre escritura, pegado y autocompletado del navegador.
for (const input of [inputEmailNode, inputPasswordNode]) {
  input.addEventListener("keyup", actualizarBotonLogin);
  input.addEventListener("input", actualizarBotonLogin);
  input.addEventListener("change", actualizarBotonLogin);
}

window.addEventListener("load", actualizarBotonLogin);
actualizarBotonLogin();
