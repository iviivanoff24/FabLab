// Calendario semanal por franjas horarias
// Muestra una rejilla con horas (filas) y días (columnas).
// Colorea cada celda según el porcentaje de ocupación:
//  <50% -> verde (.slot-available)
//  50-75% -> amarillo (.slot-medium)
//  75-99% -> naranja (.slot-high)
//  100% -> rojo (.slot-full)

document.addEventListener('DOMContentLoaded', () => {
  const container = document.getElementById('weekSchedule');
  if (!container) return;

  // Configuración: franjas horarias por defecto (modificar según las máquinas)
  const defaultSlots = [];
  for (let h = 9; h <= 20; h++) {
    defaultSlots.push((h < 10 ? '0' + h : h) + ':00');
  }

  // Estado de la vista (semana que comienza en lunes)
  let view = new Date();
  view.setHours(0,0,0,0);
  view.setDate(view.getDate() - ((view.getDay() + 6) % 7)); // lunes de la semana actual

  const weekPrev = document.getElementById('weekPrev');
  const weekNext = document.getElementById('weekNext');

  if (weekPrev) weekPrev.addEventListener('click', () => { view.setDate(view.getDate() - 7); render(); });
  if (weekNext) weekNext.addEventListener('click', () => { view.setDate(view.getDate() + 7); render(); });

  // Generador de fechas de la semana (lunes..domingo)
  function weekDates(start) {
    const arr = [];
    for (let i = 0; i < 7; i++) {
      const d = new Date(start);
      d.setDate(start.getDate() + i);
      arr.push(d);
    }
    return arr;
  }

  // Formatear fecha en formato local YYYY-MM-DD sin convertir a UTC
  function formatDateLocal(d) {
    const y = d.getFullYear();
    const m = d.getMonth() + 1;
    const day = d.getDate();
    return y + '-' + (m < 10 ? '0' + m : m) + '-' + (day < 10 ? '0' + day : day);
  }

  // Función que decide clase según porcentaje
  function occupancyClass(pct) {
    if (pct === null) return 'slot-empty';
    if (pct >= 100) return 'slot-full';
    if (pct >= 75) return 'slot-high';
    if (pct >= 50) return 'slot-medium';
    return 'slot-available';
  }

  // Formatea fecha para etiqueta (ej: L 24)
  function shortDayLabel(d) {
    const names = ['D','L','M','X','J','V','S'];
    return names[d.getDay()]+ ' ' + d.getDate();
  }

  // Intenta obtener datos del servidor; NO usar datos demo aquí — mostrar aviso si no hay datos
  async function fetchAvailability(weekStartISO) {
    try {
      const res = await fetch(`/api/availability/week?start=${weekStartISO}`, { credentials: 'same-origin' });
      if (!res.ok) return null;
      const data = await res.json();
      // DEBUG: dejar traza en consola para facilitar depuración (se puede quitar luego)
      console.debug('availability.week', weekStartISO, data);
      // Exponer datos para depuración en consola del navegador
      try { window.__lastWeekAvailability = { weekStart: weekStartISO, data: data }; } catch (e) {}
      return data; // Esperado: { slots: { 'YYYY-MM-DD|HH:MM': { occupied: N, total: M } }, totalMachines: X }
    } catch (err) {
      return null; // No hay datos reales disponibles
    }
  }

  // Renderizamos la tabla semanal
  async function render() {
    container.innerHTML = '';
    const monday = new Date(view);
    monday.setHours(0,0,0,0);
    const dates = weekDates(monday);
    const weekStartISO = formatDateLocal(monday);

    const data = await fetchAvailability(weekStartISO);

    if (!data || !data.slots) {
      const alert = document.createElement('div');
      alert.className = 'alert alert-warning';
      alert.textContent = 'No hay datos de disponibilidad para esta semana.';
      container.appendChild(alert);
      return;
    }

    const table = document.createElement('table');
    table.className = 'table table-bordered week-table';

    const thead = document.createElement('thead');
    const headRow = document.createElement('tr');
    const corner = document.createElement('th'); corner.scope = 'col'; corner.textContent = '';
    headRow.appendChild(corner);
    const __usedKeys = {};
    for (const d of dates) {
      const th = document.createElement('th');
      th.scope = 'col';
      th.textContent = shortDayLabel(d);
      th.title = d.toLocaleDateString();
      headRow.appendChild(th);
    }
    thead.appendChild(headRow);
    table.appendChild(thead);

    const tbody = document.createElement('tbody');
    for (const slot of defaultSlots) {
      const tr = document.createElement('tr');
      const hourTd = document.createElement('th');
      hourTd.scope = 'row';
      hourTd.textContent = slot;
      tr.appendChild(hourTd);
      for (const d of dates) {
        const iso = formatDateLocal(d);
        // Buscar info de la franja de forma robusta: intentar clave directa
        const slotsMap = data.slots || {};
        const tryKeys = [iso + '|' + slot];
        let info = null;
        for (const k of tryKeys) {
          if (k && slotsMap[k]) { info = slotsMap[k]; break; }
        }
        // Si no encontramos clave exacta, buscar cualquier clave que empiece por la fecha y contenga la hora
        if (!info) {
          const hour = slot.split(':')[0];
          for (const k of Object.keys(slotsMap)) {
            if (k.startsWith(iso + '|') && k.indexOf(hour + ':') !== -1) { info = slotsMap[k]; break; }
          }
        }

        let pct = null;
        let text = '-';
        let isUnavailable = false;
        let occ = 0;
        let slotTotal = (data && typeof data.totalMachines !== 'undefined') ? Number(data.totalMachines) : 0;
        if (info && typeof info.occupied !== 'undefined') {
          occ = Number(info.occupied) || 0;
          // Preferir el total definido en el slot si existe; si no, usar totalMachines general
          if (typeof info.total !== 'undefined') {
            slotTotal = Number(info.total) || 0;
          }
          if (slotTotal > 0) {
            pct = Math.round((occ / slotTotal) * 100);
            text = pct + '%';
          } else {
            // Si el total del slot es 0 significa que no hay máquinas con turno en esa franja => no disponible
            isUnavailable = true;
            text = 'No disponible';
          }
        }

        const td = document.createElement('td');
        td.className = occupancyClass(pct);
        if (isUnavailable) td.classList.add('slot-unavailable');
        // Mostrar SOLO color en la celda. Para accesibilidad dejamos label/title pero no mostramos números.
        td.textContent = isUnavailable ? '\u2014' : '';
        td.setAttribute('aria-label', info ? (isUnavailable ? 'No disponible' : (pct !== null ? (pct + '%') : occDescription(info))) : 'Sin datos');
        td.title = info ? (isUnavailable ? 'No disponible' : occDescription(info)) : 'Sin datos';
        tr.appendChild(td);
        // Guardar la clave usada para esta celda (útil para depuración)
        try { __usedKeys[iso + '|' + slot] = { info: info, pct: pct, text: text }; } catch(e) {}
      }
      tbody.appendChild(tr);
    }
    table.appendChild(tbody);

    // Exponer las claves usadas por el render
    try { window.__lastWeekUsedKeys = __usedKeys; } catch(e) {}

    // Leyenda
    const legend = document.createElement('div');
    legend.className = 'd-flex gap-2 mt-2 slot-legend';
    legend.innerHTML = `
      <div class="px-2 slot-available">&nbsp;</div><div> <50%</div>
      <div class="px-2 slot-medium">&nbsp;</div><div>50-75%</div>
      <div class="px-2 slot-high">&nbsp;</div><div>75-99%</div>
      <div class="px-2 slot-full">&nbsp;</div><div>100%</div>
    `;

    container.appendChild(table);
    container.appendChild(legend);
  }

  function occDescription(info) {
    const occ = info.occupied || 0;
    const total = info.total || '?';
    return `${occ} de ${total} máquinas ocupadas`;
  }

  render();
});
