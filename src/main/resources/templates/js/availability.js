function mainAvailability() {
    const date = new Date();
    let currentMonth = date.getMonth();
    let currentYear = date.getFullYear();
    const monthNames = ["ENERO", "FEBRERO", "MARZO", "ABRIL", "MAYO", "JUNIO", "JULIO", "AGOSTO", "SEPTIEMBRE", "OCTUBRE", "NOVIEMBRE", "DICIEMBRE"];

    const tableBody = document.getElementById('weekTableBody');
    const selectedLabel = document.getElementById('selectedWeekLabel');
    const hours = ["09:00", "10:00", "11:00", "12:00", "13:00", "16:00", "17:00", "18:00", "19:00"];

    // Devuelve el lunes de la semana para una fecha dada
    function getMonday(d) {
        const date = new Date(d);
        const day = date.getDay();
        const diff = (day === 0 ? -6 : 1 - day);
        date.setDate(date.getDate() + diff);
        date.setHours(0,0,0,0);
        return date;
    }

    // Formatea fecha ISO YYYY-MM-DD sin convertir a UTC (evita desfases por zona horaria)
    function toIsoDate(dt) {
        const y = dt.getFullYear();
        const m = ('0' + (dt.getMonth() + 1)).slice(-2);
        const d = ('0' + dt.getDate()).slice(-2);
        return y + '-' + m + '-' + d;
    }

    // 1. Render Tabla consumiendo el endpoint REST
    async function renderWeekTable(startDate) {
        tableBody.innerHTML = "";
        const monday = getMonday(startDate);
        selectedLabel.textContent = "Semana del " + monday.getDate() + " de " + monthNames[monday.getMonth()];

        // Consumir endpoint que devuelve disponibilidad semanal
        const startParam = toIsoDate(monday);
        let slots = null;
        let totalMachines = 0;
        try {
            const resp = await fetch('/api/availability/week?start=' + startParam);
            if (!resp.ok) throw new Error('HTTP ' + resp.status);
            const data = await resp.json();
            slots = data.slots || {};
            totalMachines = data.totalMachines || 0;
        } catch (e) {
            console.warn('No se pudo obtener disponibilidad desde servidor, usando datos simulados', e);
            slots = null; // fallback usado más abajo
        }

        hours.forEach(hour => {
            const row = document.createElement('tr');
            const th = document.createElement('th');
            th.textContent = hour;
            row.appendChild(th);

            for(let i=0; i<5; i++) {
                const td = document.createElement('td');

                // Si no hay datos del servidor, usar simulación ligera
                if (!slots) {
                    const rand = Math.random();
                    if(rand > 0.7) { td.className = 'avail-high'; td.title = 'Libre'; }
                    else if(rand > 0.4) { td.className = 'avail-med'; td.title = 'Media ocupación'; }
                    else { td.className = 'avail-low'; td.title = 'Completo'; }
                } else {
                    const dayDate = new Date(monday);
                    dayDate.setDate(monday.getDate() + i);
                    const key = toIsoDate(dayDate) + '|' + hour;
                    const info = slots[key];
                    if (!info) {
                        td.className = 'avail-high';
                        td.title = 'Libre';
                    } else {
                        const occupied = info.occupied || 0;
                        const total = info.total || 0;
                        if (total === 0) {
                            td.className = 'avail-closed';
                            td.title = 'Cerrado';
                        } else {
                            const ratio = occupied / total;
                            if (ratio <= 0.3) { td.className = 'avail-high'; td.title = 'Libre'; }
                            else if (ratio <= 0.7) { td.className = 'avail-med'; td.title = 'Media ocupación'; }
                            else { td.className = 'avail-low'; td.title = 'Completo'; }
                        }
                    }
                }

                row.appendChild(td);
            }
            tableBody.appendChild(row);
        });
    }

    // Cache para semanas ya consultadas (clave: monday ISO)
    const weekCache = {};

    // Obtener slots de la semana (usa cache)
    async function fetchWeekSlots(mondayIso) {
        if (weekCache[mondayIso]) return weekCache[mondayIso];
        try {
            const resp = await fetch('/api/availability/week?start=' + mondayIso);
            if (!resp.ok) throw new Error('HTTP ' + resp.status);
            const data = await resp.json();
            const slots = data.slots || {};
            weekCache[mondayIso] = slots;
            return slots;
        } catch (e) {
            console.warn('No se pudo obtener semana', mondayIso, e);
            weekCache[mondayIso] = null; // marca como fallo
            return null;
        }
    }

    // 2. Render Calendario: colorea cada día según disponibilidad consultando al backend
    async function renderCalendar(month, year) {
        const monthDiv = document.getElementById('fabcalMonth');
        const daysDiv = document.getElementById('fabcalDays');
        monthDiv.textContent = monthNames[month] + " " + year;
        daysDiv.innerHTML = "";

        const firstDay = new Date(year, month, 1).getDay(); 
        const startDay = (firstDay === 0) ? 6 : firstDay - 1; 
        const daysInMonth = new Date(year, month + 1, 0).getDate();

        for(let i=0; i < startDay; i++) { daysDiv.appendChild(document.createElement('div')); }

        const today = new Date();
        // Recolectar celdas y semanas necesarias
        const dayCells = [];
        const weeksToFetch = new Set();
        for(let i=1; i <= daysInMonth; i++) {
            const dayCell = document.createElement('div');
            dayCell.textContent = i;
            dayCell.className = 'py-1 text-center rounded small cursor-pointer calendar-day';

            const checkDate = new Date(year, month, i);
            const dayOfWeek = checkDate.getDay();

            const iso = toIsoDate(checkDate);
            dayCell.dataset.dateIso = iso;

            if (dayOfWeek === 0 || dayOfWeek === 6) {
                dayCell.style.color = '#ccc'; // Fines de semana
                dayCell.classList.add('avail-closed');
            } else {
                const monday = getMonday(checkDate);
                const mondayIso = toIsoDate(monday);
                weeksToFetch.add(mondayIso);
                dayCell.dataset.mondayIso = mondayIso;
            }

            dayCell.addEventListener('click', function() {
                document.querySelectorAll('#fabcalDays div').forEach(d => d.style.border = 'none');
                this.style.border = '2px solid #333'; // Selección
                renderWeekTable(new Date(year, month, i));
            });

            if(i === today.getDate() && month === today.getMonth() && year === today.getFullYear()) {
                dayCell.style.fontWeight = '900';
                dayCell.style.border = '2px solid #f8bc1e';
            }
            daysDiv.appendChild(dayCell);
            dayCells.push(dayCell);
        }

        // Fetch de todas las semanas necesarias en paralelo
        const fetchPromises = Array.from(weeksToFetch).map(w => fetchWeekSlots(w));
        await Promise.all(fetchPromises);

        // Para cada celda, aplicar clase según la disponibilidad del día (sumando horas)
        for (const cell of dayCells) {
            if (cell.dataset.mondayIso == null) continue; // fin de semana
            const iso = cell.dataset.dateIso;
            const mondayIso = cell.dataset.mondayIso;
            const slots = weekCache[mondayIso];
            if (!slots) {
                // no hay datos: dejar tal cual (sin color extra)
                continue;
            }
            // sumar occupied y total para las horas mostradas
            let sumOcc = 0;
            let sumTot = 0;
            for (const h of hours) {
                const key = iso + '|' + h;
                const info = slots[key];
                if (info) {
                    sumOcc += info.occupied || 0;
                    sumTot += info.total || 0;
                }
            }
            if (sumTot === 0) {
                cell.classList.add('avail-closed');
                cell.title = 'Cerrado';
            } else {
                const ratio = sumOcc / sumTot;
                if (ratio <= 0.3) { cell.classList.add('avail-high'); cell.title = 'Libre'; }
                else if (ratio <= 0.7) { cell.classList.add('avail-med'); cell.title = 'Media ocupación'; }
                else { cell.classList.add('avail-low'); cell.title = 'Completo'; }
            }
        }
    }

    document.getElementById('fabcalPrev').addEventListener('click', () => { currentMonth--; if(currentMonth < 0) { currentMonth=11; currentYear--; } renderCalendar(currentMonth, currentYear); });
    document.getElementById('fabcalNext').addEventListener('click', () => { currentMonth++; if(currentMonth > 11) { currentMonth=0; currentYear++; } renderCalendar(currentMonth, currentYear); });

    renderCalendar(currentMonth, currentYear);
    renderWeekTable(new Date());
}

if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', mainAvailability);
} else {
    mainAvailability();
}
