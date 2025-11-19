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
				const hello = document.createElement('span');
				hello.className = 'small text-secondary';
				hello.textContent = `Hola, ${data.name || data.email}`;
				const logoutForm = document.createElement('form');
				logoutForm.method = 'post';
				logoutForm.action = '/logout';
				logoutForm.className = 'm-0';
				const logoutBtn = document.createElement('button');
				logoutBtn.type = 'submit';
				logoutBtn.className = 'btn btn-outline-secondary btn-sm';
				logoutBtn.textContent = 'Cerrar sesión';
				logoutForm.appendChild(logoutBtn);
				authContainer.appendChild(hello);
				authContainer.appendChild(logoutForm);
			})
			.catch(err => {
				console.warn('[session] error obteniendo sesión', err);
			});
	} else {
		console.debug('[session] authLinks no encontrado');
	}

	// Elementos del calendario

	const monthEl = document.getElementById('fabcalMonth');
	const daysEl = document.getElementById('fabcalDays');
	const prevBtn = document.getElementById('fabcalPrev');
	const nextBtn = document.getElementById('fabcalNext');

	// Si no estamos en una página con el calendario, salimos sin hacer nada
	if (!monthEl || !daysEl || !prevBtn || !nextBtn) return;

	const meses = [
		'enero', 'febrero', 'marzo', 'abril', 'mayo', 'junio',
		'julio', 'agosto', 'septiembre', 'octubre', 'noviembre', 'diciembre'
	];

	let view = new Date();
	view.setDate(1);
	// Selección por defecto: hoy
	let selected = new Date();

	function sameDay(a, b) {
		return (
			a && b &&
			a.getFullYear() === b.getFullYear() &&
			a.getMonth() === b.getMonth() &&
			a.getDate() === b.getDate()
		);
	}

	function render() {
		const y = view.getFullYear();
		const m = view.getMonth();
		monthEl.textContent =
			meses[m].charAt(0).toUpperCase() + meses[m].slice(1) + ' ' + y;
		daysEl.innerHTML = '';

		const first = new Date(y, m, 1);
		const last = new Date(y, m + 1, 0);
		const startOffset = (first.getDay() + 6) % 7; // Lunes=0

		for (let i = 0; i < startOffset; i++) daysEl.appendChild(document.createElement('div'));

		for (let d = 1; d <= last.getDate(); d++) {
			const btn = document.createElement('button');
			btn.type = 'button';
			btn.className = 'day btn btn-light border-0 p-0';
			btn.textContent = d;
			const thisDate = new Date(y, m, d);
			if (sameDay(thisDate, selected)) btn.classList.add('selected');
			btn.addEventListener('click', () => {
				selected = thisDate;
				render();
			});
			daysEl.appendChild(btn);
		}
	}

	prevBtn.addEventListener('click', () => {
		view.setMonth(view.getMonth() - 1);
		render();
	});
	nextBtn.addEventListener('click', () => {
		view.setMonth(view.getMonth() + 1);
		render();
	});

	render();
});

