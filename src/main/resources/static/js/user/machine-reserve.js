document.addEventListener("DOMContentLoaded", function () {
    const mainContainer = document.querySelector('main');
    const machineId = mainContainer.dataset.machineId;
    const pricePerHour = parseFloat((mainContainer.dataset.hourlyPrice || "0").replace(",", ".")) || 0;
    const isAdmin = mainContainer.dataset.isAdmin === 'true';

    // State
    let currentDate = new Date(); // For calendar view
    let selectedDateStr = null; // Currently viewing slots for this date
    // Basket: key = "YYYY-MM-DD|HH", value = { date, hour, shiftId, label }
    let basket = {}; 

    // DOM Elements
    const calendarGrid = document.getElementById("calendarGrid");
    const currentMonthLabel = document.getElementById("currentMonthLabel");
    const slotsContainer = document.getElementById("slotsContainer");
    const selectedDateTitle = document.getElementById("selectedDateTitle");
    const basketList = document.getElementById("basketList");
    const basketTotal = document.getElementById("basketTotal");
    const payBtn = document.getElementById("payBtn");
    const form = document.getElementById("multiReserveForm");

    // Calendar Logic
    function renderCalendar(date) {
        calendarGrid.innerHTML = "";
        const year = date.getFullYear();
        const month = date.getMonth();
        
        currentMonthLabel.textContent = new Intl.DateTimeFormat('es-ES', { month: 'long', year: 'numeric' }).format(date);

        const firstDay = new Date(year, month, 1);
        const lastDay = new Date(year, month + 1, 0);
        
        // Adjust for Monday start (0=Sun, 1=Mon...)
        let startDay = firstDay.getDay() - 1;
        if (startDay === -1) startDay = 6;

        // Empty cells for previous month
        for (let i = 0; i < startDay; i++) {
            const div = document.createElement("div");
            calendarGrid.appendChild(div);
        }

        const today = new Date();
        today.setHours(0,0,0,0);

        for (let d = 1; d <= lastDay.getDate(); d++) {
            const dayDate = new Date(year, month, d);
            const dayStr = formatDateISO(dayDate);
            const div = document.createElement("div");
            div.className = "calendar-day";
            div.textContent = d;
            
            // Check if past or weekend
            const isWeekend = dayDate.getDay() === 0 || dayDate.getDay() === 6;
            const isPast = dayDate < today;

            if (isPast || isWeekend) {
                div.classList.add("disabled");
            } else {
                div.onclick = () => loadSlots(dayStr);
            }

            if (dayStr === selectedDateStr) {
                div.classList.add("selected");
            }
            if (dayDate.getTime() === today.getTime()) {
                div.classList.add("today");
            }
            
            // Check if any selection in basket for this day
            const hasSelection = Object.values(basket).some(item => item.date === dayStr);
            if (hasSelection) {
                div.classList.add("has-selection");
            }

            calendarGrid.appendChild(div);
        }
    }

    if (document.getElementById("prevMonthBtn")) {
        document.getElementById("prevMonthBtn").onclick = () => {
            currentDate.setMonth(currentDate.getMonth() - 1);
            renderCalendar(currentDate);
        };
    }
    if (document.getElementById("nextMonthBtn")) {
        document.getElementById("nextMonthBtn").onclick = () => {
            currentDate.setMonth(currentDate.getMonth() + 1);
            renderCalendar(currentDate);
        };
    }

    // Slots Logic
    function loadSlots(dateStr) {
        selectedDateStr = dateStr;
        renderCalendar(currentDate); // Re-render to update selection highlight
        selectedDateTitle.textContent = "Turnos del día " + dateStr;
        slotsContainer.innerHTML = '<div class="text-center py-3"><div class="spinner-border text-primary" role="status"></div></div>';

        fetch(`/api/machines/${machineId}/slots?date=${dateStr}`)
            .then(res => res.json())
            .then(slots => {
                renderSlots(slots, dateStr);
            })
            .catch(err => {
                slotsContainer.innerHTML = '<div class="alert alert-danger m-3">Error cargando turnos.</div>';
                console.error(err);
            });
    }

    function renderSlots(slots, dateStr) {
        slotsContainer.innerHTML = "";
        if (slots.length === 0) {
            slotsContainer.innerHTML = '<div class="list-group-item text-muted">No hay turnos disponibles.</div>';
            return;
        }

        slots.forEach(slot => {
            const item = document.createElement("div");
            item.className = "list-group-item d-flex justify-content-between align-items-center";
            
            // Status badges
            let statusBadge = "";
            if (slot.reservado) {
                const text = slot.reservadoPorMi ? 'Reservado · Tu reserva' : 'Reservado';
                statusBadge = `<span class="badge bg-secondary">${text}</span>`;
                if (isAdmin && slot.bookings) {
                    slot.bookings.forEach(b => {
                        statusBadge += `<span class="badge bg-info text-dark ms-1">Reserva: ${b.user ? b.user.name : 'Usuario'}</span>`;
                    });
                }
            } else if (slot.canReserve) {
                statusBadge = `<span class="badge bg-success">Disponible</span>`;
            } else {
                statusBadge = `<span class="badge bg-danger">No disponible</span>`;
            }

            // Checkbox state
            const slotKey = `${dateStr}|${slot.hour}`;
            const isChecked = !!basket[slotKey];

            // Action area
            let actionHtml = "";
            if (slot.canReserve) {
                const btnClass = isChecked ? "btn-primary" : "btn-outline-primary";
                const btnText = isChecked ? '<i class="bi bi-check-lg"></i> Seleccionado' : 'Seleccionar';
                actionHtml = `
                    <button type="button" class="btn btn-sm ${btnClass} slot-select-btn" 
                        data-key="${slotKey}"
                        data-date="${dateStr}"
                        data-hour="${slot.hour}"
                        data-shift-id="${slot.shiftId || ''}"
                        data-label="${slot.startTime} - ${slot.endTime}"
                    >
                        ${btnText}
                    </button>
                `;
            } else if (slot.reservado && slot.canCancel) {
                    // Cancel button logic (simplified for now, reusing modal logic if possible)
                    // We need to attach data attributes for the modal
                    // Since we are generating HTML string, we need to be careful with quotes
                    let bookingId = "";
                    if (slot.bookings && slot.bookings.length > 0) bookingId = slot.bookings[0].id;
                    
                    actionHtml = `
                    <button type="button" class="btn btn-sm btn-outline-danger"
                        data-bs-toggle="modal" data-bs-target="#cancelBookingModal"
                        data-booking-id="${bookingId}"
                        data-shift-id="${slot.shiftId}"
                        data-start-hour="${slot.hour}"
                        data-date="${dateStr}">
                        Eliminar
                    </button>
                    `;
            }

            item.innerHTML = `
                <div>
                    <span class="fw-semibold">${slot.startTime} - ${slot.endTime}</span>
                    ${statusBadge}
                </div>
                <div>${actionHtml}</div>
            `;
            slotsContainer.appendChild(item);
        });

        // Attach event listeners to new buttons
        document.querySelectorAll(".slot-select-btn").forEach(btn => {
            btn.addEventListener("click", (e) => {
                const target = e.currentTarget;
                const key = target.getAttribute("data-key");
                
                if (basket[key]) {
                    // Deselect
                    delete basket[key];
                    target.classList.remove("btn-primary");
                    target.classList.add("btn-outline-primary");
                    target.innerHTML = "Seleccionar";
                } else {
                    // Select
                    basket[key] = {
                        date: target.getAttribute("data-date"),
                        hour: target.getAttribute("data-hour"),
                        shiftId: target.getAttribute("data-shift-id"),
                        label: target.getAttribute("data-label")
                    };
                    target.classList.remove("btn-outline-primary");
                    target.classList.add("btn-primary");
                    target.innerHTML = '<i class="bi bi-check-lg"></i> Seleccionado';
                }
                updateBasket();
                renderCalendar(currentDate); // Update dots
            });
        });
    }

    // Basket Logic
    function updateBasket() {
        basketList.innerHTML = "";
        const items = Object.values(basket);
        
        if (items.length === 0) {
            basketList.innerHTML = '<li class="list-group-item text-muted small fst-italic">No has seleccionado ningún turno.</li>';
            basketTotal.textContent = "0.00";
            payBtn.disabled = true;
            return;
        }

        // Sort by date and hour
        items.sort((a, b) => {
            if (a.date !== b.date) return a.date.localeCompare(b.date);
            return parseInt(a.hour) - parseInt(b.hour);
        });

        items.forEach(item => {
            const li = document.createElement("li");
            li.className = "list-group-item d-flex justify-content-between align-items-center";
            li.innerHTML = `
                <span><strong>${item.date}</strong>: ${item.label}</span>
                <button type="button" class="btn btn-sm btn-link text-danger p-0" onclick="removeFromBasket('${item.date}|${item.hour}')">
                    <i class="bi bi-trash"></i>
                </button>
            `;
            basketList.appendChild(li);
        });

        const total = items.length * pricePerHour;
        basketTotal.textContent = total.toFixed(2);
        payBtn.disabled = false;
    }

    window.removeFromBasket = function(key) {
        delete basket[key];
        updateBasket();
        // If currently viewing the day of the removed item, update the button
        const btn = document.querySelector(`.slot-select-btn[data-key="${key}"]`);
        if (btn) {
            btn.classList.remove("btn-primary");
            btn.classList.add("btn-outline-primary");
            btn.innerHTML = "Seleccionar";
        }
        renderCalendar(currentDate);
    };

    // Form Submission
    if (form) {
        form.addEventListener("submit", function () {
            // Clear old hidden inputs
            form.querySelectorAll('input[name="shiftIds"], input[name="startHours"], input[name="shiftDates"], input[name="startDates"]').forEach(el => el.remove());
            
            Object.values(basket).forEach(item => {
                if (item.shiftId && item.shiftId !== "null" && item.shiftId !== "") {
                    addHidden(form, "shiftIds", item.shiftId);
                    addHidden(form, "shiftDates", item.date);
                } else {
                    addHidden(form, "startHours", item.hour);
                    addHidden(form, "startDates", item.date);
                }
            });
        });
    }

    function addHidden(form, name, value) {
        const i = document.createElement("input");
        i.type = "hidden";
        i.name = name;
        i.value = value;
        form.appendChild(i);
    }

    function formatDateISO(date) {
        const year = date.getFullYear();
        const month = String(date.getMonth() + 1).padStart(2, '0');
        const day = String(date.getDate()).padStart(2, '0');
        return `${year}-${month}-${day}`;
    }

    // Cancel Modal Logic
    var modal = document.getElementById("cancelBookingModal");
    var dateInput = document.getElementById("cancelDate");
    var hourInput = document.getElementById("cancelStartHour");
    var shiftInput = document.getElementById("cancelShiftId");
    var bookingInput = document.getElementById("cancelBookingId");
    var confirmBtn = document.getElementById("confirmCancelBookingBtn");
    
    if (modal) {
        modal.addEventListener("show.bs.modal", function (ev) {
            var btn = ev.relatedTarget;
            if (!btn) return;
            dateInput.value = btn.getAttribute("data-date");
            hourInput.value = btn.getAttribute("data-start-hour");
            shiftInput.value = btn.getAttribute("data-shift-id");
            bookingInput.value = btn.getAttribute("data-booking-id") || "";
        });
        
        confirmBtn.addEventListener("click", function () {
            document.getElementById("cancelBookingForm").submit();
        });
    }

    // Init
    renderCalendar(currentDate);
    // Optionally load today's slots
    // loadSlots(formatDateISO(new Date()));
});
