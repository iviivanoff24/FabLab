document.addEventListener('DOMContentLoaded', function() {
  function setupPasswordToggle(toggleId, inputId) {
    const toggle = document.getElementById(toggleId);
    const input = document.getElementById(inputId);
    
    if (toggle && input) {
      toggle.addEventListener('click', function() {
        const type = input.getAttribute('type') === 'password' ? 'text' : 'password';
        input.setAttribute('type', type);
        
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
  }

  setupPasswordToggle('togglePassword', 'password');
  setupPasswordToggle('togglePasswordConfirm', 'passwordConfirm');
});
