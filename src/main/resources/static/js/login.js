(function () {
  const params = new URLSearchParams(window.location.search);
  const errorParam = params.get("error");
  const alertDiv = document.getElementById("loginError");
  const form = document.getElementById("loginForm");
  const emailInput = document.getElementById("email");
  const passInput = document.getElementById("password");

  // Restaurar desde sessionStorage si hubo fallo anterior
  const storedEmail = sessionStorage.getItem("login_email");
  const storedPass = sessionStorage.getItem("login_password");

  if (errorParam && alertDiv) {
    alertDiv.classList.remove("d-none");
    const textNode = alertDiv.querySelector(".alert-text");
    if (textNode) textNode.textContent = decodeURIComponent(errorParam);
    if (storedEmail) emailInput.value = storedEmail;
    if (storedPass) passInput.value = storedPass; // Nota: mantener password en cliente, no poner en URL
  } else {
    // Si no hay error, limpiar cualquier dato previo
    sessionStorage.removeItem("login_email");
    sessionStorage.removeItem("login_password");
  }

  if (form) {
    form.addEventListener("submit", () => {
      sessionStorage.setItem("login_email", emailInput.value);
      sessionStorage.setItem("login_password", passInput.value);
    });
  }
})();

// Toggle Password Visibility
const togglePassword = document.querySelector('#togglePassword');
const password = document.querySelector('#password');
if(togglePassword && password) {
  togglePassword.addEventListener('click', function (e) {
    const type = password.getAttribute('type') === 'password' ? 'text' : 'password';
    password.setAttribute('type', type);
    this.querySelector('i').classList.toggle('bi-eye');
    this.querySelector('i').classList.toggle('bi-eye-slash');
  });
}
