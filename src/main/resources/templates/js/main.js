// Inicialización del calendario de reservas (movido desde index.html)
// Aseguramos que el DOM esté listo antes de buscar elementos
document.addEventListener('DOMContentLoaded', () => {
	// Estado de sesión y cabecera (login/register vs usuario + logout)
	// -------- Sesión / Header dinámico --------
	const authContainer = document.getElementById('authLinks');
	if (authContainer) {
		fetch('/api/session/me', { credentials: 'same-origin' })
			.then(r => {
				if (!r.ok) throw new Error('Estado HTTP ' + r.status);
				return r.json();
			})
			.then(data => {
				console.debug('[session] respuesta', data);
				if (!data.logged) {
					console.debug('[session] no logueado');
					return;
				}
				authContainer.innerHTML = '';
				const logoutForm = document.createElement('form');
				logoutForm.method = 'post';
				logoutForm.action = '/logout';
				logoutForm.className = 'm-0';
				const logoutBtn = document.createElement('button');
				logoutBtn.type = 'submit';
				logoutBtn.className = 'btn btn-outline-secondary btn-sm';
				logoutBtn.textContent = 'Cerrar sesión';

				// Botón perfil (mostrar antes del logout). Poner el nombre en el title y activar tooltip
				const profileLink = document.createElement('a');
				profileLink.href = '/profile';
				profileLink.className = 'btn btn-secondary btn-sm me-2';
				profileLink.setAttribute('role','button');
				profileLink.setAttribute('data-bs-toggle','tooltip');
				profileLink.setAttribute('data-bs-placement','bottom');
				profileLink.setAttribute('title', data.name || data.email || 'Mi perfil');
				profileLink.innerHTML = '<i class="bi bi-person"></i>';

				// Modal de confirmación (se crea una sola vez si no existe)
				function ensureLogoutModal() {
					let modalEl = document.getElementById('logoutConfirmModal');
					if (modalEl) return modalEl;
					modalEl = document.createElement('div');
					modalEl.id = 'logoutConfirmModal';
					modalEl.className = 'modal fade';
					modalEl.tabIndex = -1;
					modalEl.innerHTML = `
						<div class="modal-dialog">
							<div class="modal-content">
								<div class="modal-header">
									<h1 class="modal-title fs-5">Confirmar cierre de sesión</h1>
									<button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Cerrar"></button>
								</div>
								<div class="modal-body">
									<p>¿Seguro que deseas cerrar tu sesión?</p>
								</div>
								<div class="modal-footer">
									<button type="button" class="btn btn-secondary" data-bs-dismiss="modal">Cancelar</button>
									<button type="button" id="logoutConfirmBtn" class="btn btn-primary">Cerrar sesión</button>
								</div>
							</div>
						</div>`;
					document.body.appendChild(modalEl);
					return modalEl;
				}

				logoutBtn.addEventListener('click', function (e) {
					e.preventDefault(); // Evita envío inmediato
					const modalEl = ensureLogoutModal();
					const confirmBtn = modalEl.querySelector('#logoutConfirmBtn');
					if (confirmBtn) {
						confirmBtn.onclick = () => {
							logoutForm.submit();
						};
					}
					// Usar la API de Bootstrap para mostrar el modal
					if (window.bootstrap && bootstrap.Modal) {
						const modal = bootstrap.Modal.getOrCreateInstance(modalEl);
						modal.show();
					} else {
						// Fallback simple si Bootstrap JS no está cargado
						alert('¿Seguro que deseas cerrar tu sesión?');
						logoutForm.submit();
					}
				});
				logoutForm.appendChild(logoutBtn);
				authContainer.appendChild(profileLink);
				authContainer.appendChild(logoutForm);

				// Inicializar tooltip dinámico si Bootstrap está disponible
				try {
					if (window.bootstrap && bootstrap.Tooltip) {
						bootstrap.Tooltip.getOrCreateInstance(profileLink);
					}
				} catch (e) { /* ignore */ }
			})
			.catch(err => {
				console.warn('[session] error obteniendo sesión', err);
			});
	} else {
		console.debug('[session] authLinks no encontrado');
	}
});

