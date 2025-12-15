// Validación simple + guardia admin (cliente)
document.addEventListener('DOMContentLoaded', () => {
  // Mostrar error del servidor si viene en querystring ?error=
  const params = new URLSearchParams(location.search);
  const serverError = params.get('error');
  if (serverError) {
    const alert = document.createElement('div');
    alert.className = 'alert alert-danger';
    alert.textContent = decodeURIComponent(serverError);
    document.querySelector('main.container').insertBefore(alert, document.getElementById('machineForm'));
  }

  fetch('/api/session/me').then(r=>r.json()).then(data => {
    if (!data.admin) {
      document.getElementById('machineForm').classList.add('d-none');
      document.getElementById('adminGuard').classList.remove('d-none');
    }
  });
  const form = document.getElementById('machineForm');
  const imgInput = document.getElementById('image');
  
  const validateImage = () => {
    const f = imgInput.files && imgInput.files[0];
    if (f && f.size > 2000000) {
      imgInput.setCustomValidity('La imagen supera 2 MB');
    } else {
      imgInput.setCustomValidity('');
    }
  };
  imgInput.addEventListener('change', validateImage);
  form.addEventListener('submit', e => {
    validateImage();
    if (!form.checkValidity()) { e.preventDefault(); e.stopPropagation(); }
    form.classList.add('was-validated');
  });
});
