(function () {
  const params = new URLSearchParams(window.location.search);
  if (params.has("error")) {
    const alert = document.getElementById("registerError");
    if (alert) alert.classList.remove("d-none");
  }
})();

document.addEventListener("DOMContentLoaded", () => {
  const form = document.getElementById("registerForm");
  if (!form) return;

  const validators = {
    name: (v) => v.trim().length >= 2,
    email: (v) => /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(v),
    password: (v) => /^(?=.*[A-Za-z])(?=.*\d).{6,}$/.test(v), // min 6, letra y número
    telefono: (v) =>
      v.trim() === "" || /^\d{9}$/.test(v.replace(/\s+/g, "")),
  };

  function validateField(input) {
    const fn = validators[input.name];
    if (!fn) return true; // campos sin validador específico
    const valid = fn(input.value);
    input.classList.remove("is-valid", "is-invalid");
    input.classList.add(valid ? "is-valid" : "is-invalid");
    return valid;
  }

  const inputs = form.querySelectorAll(".form-control");
  inputs.forEach((inp) => {
    inp.addEventListener("input", () => validateField(inp));
    inp.addEventListener("blur", () => validateField(inp));
  });

  // Toggle Password Visibility
  const togglePassword = document.querySelector('#togglePassword');
  const password = document.querySelector('#password');

  if (togglePassword && password) {
      togglePassword.addEventListener('click', function (e) {
          // toggle the type attribute
          const type = password.getAttribute('type') === 'password' ? 'text' : 'password';
          password.setAttribute('type', type);
          
          // toggle the eye icon
          const icon = this.querySelector('i');
          if (type === 'password') {
              icon.classList.remove('bi-eye-slash');
              icon.classList.add('bi-eye');
          } else {
              icon.classList.remove('bi-eye');
              icon.classList.add('bi-eye-slash');
          }
      });
  }

  form.addEventListener("submit", (e) => {
    let allValid = true;
    inputs.forEach((i) => {
      if (!validateField(i)) allValid = false;
    });
    if (!allValid) {
      e.preventDefault();
    }
  });
});
