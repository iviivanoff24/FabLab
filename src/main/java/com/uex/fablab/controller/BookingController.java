package com.uex.fablab.controller;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Locale;

import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.uex.fablab.data.model.Booking;
import com.uex.fablab.data.model.Machine;
import com.uex.fablab.data.model.Shift;
import com.uex.fablab.data.model.ShiftStatus;
import com.uex.fablab.data.model.User;
import com.uex.fablab.data.services.BookingService;
import com.uex.fablab.data.services.MachineService;
import com.uex.fablab.data.services.ShiftService;
import com.uex.fablab.data.services.UserService;

import jakarta.servlet.http.HttpSession;

/**
 * Controlador de reservas de máquinas.
 * Proporciona páginas para reservar por máquina y una página genérica con selector,
 * además de los endpoints para crear y cancelar reservas. Genera HTML dinámico
 * inyectando fragmentos en plantillas estáticas.
 */
@Controller
public class BookingController {

    private static final String MACHINE_INFO_MARKER = "<!-- MACHINE_INFO_MARKER -->";
    private static final String SHIFTS_LIST_MARKER = "<!-- SHIFTS_LIST_MARKER -->";
    private static final String RES_MACHINE_SELECTOR_MARKER = "<!-- MACHINE_SELECTOR_MARKER -->";
    private static final String RES_SLOTS_MARKER = "<!-- SLOTS_MARKER -->";

    private final MachineService machineService;
    private final ShiftService shiftService;
    private final BookingService bookingService;
    private final UserService userService;

    public BookingController(MachineService machineService, ShiftService shiftService, BookingService bookingService, UserService userService) {
        this.machineService = machineService;
        this.shiftService = shiftService;
        this.bookingService = bookingService;
        this.userService = userService;
    }

    /**
     * Página de reserva para una máquina específica.
     * Muestra los turnos del día seleccionado, permite reservar turnos futuros y cancelar
     * la reserva propia todavía no iniciada.
     *
     * @param machineId id de la máquina
     * @param session sesión HTTP con atributos de usuario
     * @param dateStr fecha en formato ISO (yyyy-MM-dd), por defecto hoy
     * @param successParam indicador de reserva creada
     * @param canceledParam indicador de reserva cancelada
     * @param errorParam mensaje de error a mostrar
     * @return HTML de la página renderizada
     */
    @GetMapping("/machines/{id}/reserve")
    public ResponseEntity<String> reserveMachinePage(@PathVariable("id") Long machineId, HttpSession session,
            @RequestParam(value = "date", required = false) String dateStr,
            @RequestParam(value = "success", required = false) String successParam,
            @RequestParam(value = "canceled", required = false) String canceledParam,
            @RequestParam(value = "error", required = false) String errorParam) throws java.io.IOException {
        // Interceptor garantiza que el usuario está autenticado
        Object userId = session.getAttribute("USER_ID");
        var machineOpt = machineService.findById(machineId);
        if (machineOpt.isEmpty()) {
            return ResponseEntity.status(302)
                    .header("Location", "/machines?error=" + java.net.URLEncoder.encode("Máquina no encontrada", StandardCharsets.UTF_8))
                    .build();
        }
        Machine machine = machineOpt.get();

        var resource = new ClassPathResource("templates/user/machine-reserve.html");
        String html = new String(resource.getInputStream().readAllBytes(), StandardCharsets.UTF_8);

        // Construir bloque de información de la máquina
        StringBuilder info = new StringBuilder();
        info.append("<h2 class='h4 mb-3'>Reservar: ")
                .append(safe(machine.getName())).append("</h2>")
                .append("<p class='text-muted mb-1'>Ubicación: ")
                .append(safe(machine.getLocation() != null ? machine.getLocation() : "No especificada"))
                .append("</p>")
                .append("<p class='mb-1'>Precio: €")
                .append(machine.getHourlyPrice() != null ? String.format(Locale.US, "%.2f", machine.getHourlyPrice()) : "0.00")
                .append(" / hora</p>")
                .append("<p class='mb-3'>Estado: <span class='badge bg-primary'>")
                .append(machine.getStatus() != null ? machine.getStatus().name() : "Desconocido")
                .append("</span></p>")
                .append("<a href='/machines' class='btn btn-sm btn-outline-secondary mb-3'>&larr; Volver</a>");
        html = html.replace(MACHINE_INFO_MARKER, info.toString());

        // Fecha seleccionada (por defecto hoy)
        java.time.LocalDate selectedDate = null;
        if (dateStr != null && !dateStr.isBlank()) {
            try {
                selectedDate = java.time.LocalDate.parse(dateStr);
            } catch (Exception ignored) {
            }
        }
        if (selectedDate == null) {
            selectedDate = java.time.LocalDate.now();
        }

        // Cargar turnos existentes (solo representan reservas ya realizadas)
        java.util.List<Shift> existingShifts = shiftService.findByMachineAndDate(machine, selectedDate);
        java.util.Map<LocalTime, Shift> shiftByStart = new java.util.HashMap<>();
        for (Shift s : existingShifts) {
            shiftByStart.put(s.getStartTime(), s);
        }

        StringBuilder shiftsHtml = new StringBuilder();
        shiftsHtml.append("<h3 class='h6'>Turnos del día ")
                .append(selectedDate.toString())
                .append("</h3>");
        // Formulario para cambiar fecha
        shiftsHtml.append("<form method='get' class='row g-2 align-items-end mb-3'>")
                .append("<div class='col-auto'><label class='form-label mb-0'>Fecha</label><input type='date' name='date' class='form-control form-control-sm' value='")
                .append(selectedDate.toString()).append("'/></div>")
                .append("<div class='col-auto'><button class='btn btn-sm btn-primary'>Ver</button></div>")
                .append("</form>");

        if (successParam != null) {
            shiftsHtml.append("<div class='alert alert-success py-1'>Reserva creada correctamente.</div>");
        } else if (canceledParam != null) {
            shiftsHtml.append("<div class='alert alert-success py-1'>Reserva cancelada correctamente.</div>");
        } else if (errorParam != null) {
            shiftsHtml.append("<div class='alert alert-danger py-1'>").append(safe(errorParam)).append("</div>");
        }

        shiftsHtml.append("<div class='list-group mb-4'>");
        // Generar slots horarios 09:00 - 21:00 (último empieza 20:00 termina 21:00)
        java.time.LocalDateTime now = java.time.LocalDateTime.now();
        for (int hour = 9; hour < 21; hour++) {
            LocalTime start = LocalTime.of(hour, 0);
            LocalTime end = start.plusHours(1);
            Shift s = shiftByStart.get(start); // puede ser null si no hay reserva
            boolean reservado = false;
            boolean reservadoPorMi = false;
            if (s != null) {
                // Si existe shift consideramos reservado si tiene Booking o estado != Disponible
                reservado = !s.getBookings().isEmpty() || s.getStatus() == ShiftStatus.Reservado;
                if (reservado) {
                    reservadoPorMi = s.getBookings().stream().anyMatch(b -> b.getUser() != null && b.getUser().getId().equals(userId));
                }
            }
            shiftsHtml.append("<div class='list-group-item d-flex justify-content-between align-items-center'>");
            shiftsHtml.append("<div><span class='fw-semibold'>")
                    .append(start)
                    .append(" - ")
                    .append(end)
                    .append("</span> ");
            if (reservado) {
                shiftsHtml.append("<span class='badge bg-secondary'>Reservado</span>");
                if (reservadoPorMi) {
                    shiftsHtml.append(" <span class='badge bg-warning text-dark'>Tu reserva</span>");
                }
            } else if (machine.getStatus() != null && machine.getStatus().name().equalsIgnoreCase("Disponible")) {
                shiftsHtml.append("<span class='badge bg-success'>Disponible</span>");
            } else {
                shiftsHtml.append("<span class='badge bg-danger'>Máquina no disponible</span>");
            }
            shiftsHtml.append("</div>");
            boolean maquinaDisponible = machine.getStatus() != null && machine.getStatus().name().equalsIgnoreCase("Disponible");
            boolean slotEnFuturo = java.time.LocalDateTime.of(selectedDate, start).isAfter(now);
            if (!reservado && maquinaDisponible && slotEnFuturo) {
                shiftsHtml.append("<form method='post' action='/machines/")
                        .append(machineId)
                        .append("/reserve' class='m-0'>")
                        .append("<input type='hidden' name='date' value='")
                        .append(selectedDate)
                        .append("' />")
                        .append("<input type='hidden' name='startHour' value='")
                        .append(hour)
                        .append("' />")
                        .append("<button class='btn btn-sm btn-outline-primary'>Reservar</button></form>");
            } else {
                shiftsHtml.append("<button class='btn btn-sm btn-outline-secondary' disabled>Reservar</button>");
            }
            // Botón cancelar (abre modal) cuando es mi reserva y aún no ha pasado
            if (reservado && reservadoPorMi && slotEnFuturo && s != null) {
                shiftsHtml.append("<button type='button' class='btn btn-sm btn-outline-danger ms-2' ")
                        .append("data-bs-toggle='modal' data-bs-target='#cancelBookingModal' ")
                        .append("data-shift-id='").append(s.getId()).append("' ")
                        .append("data-start-hour='").append(hour).append("' ")
                        .append("data-date='").append(selectedDate).append("'>Cancelar</button>");
            }
            shiftsHtml.append("</div>");
        }
        shiftsHtml.append("</div>");
        // Modal de confirmación + formulario oculto para cancelar
        shiftsHtml.append("<div class='modal fade' id='cancelBookingModal' tabindex='-1' aria-labelledby='cancelBookingModalLabel' aria-hidden='true'>")
                .append("  <div class='modal-dialog'>")
                .append("    <div class='modal-content'>")
                .append("      <div class='modal-header'>")
                .append("        <h1 class='modal-title fs-5' id='cancelBookingModalLabel'>Confirmar cancelación de reserva</h1>")
                .append("        <button type='button' class='btn-close' data-bs-dismiss='modal' aria-label='Cerrar'></button>")
                .append("      </div>")
                .append("      <div class='modal-body'>")
                .append("        <p>¿Seguro que deseas cancelar esta reserva?</p>")
                .append("      </div>")
                .append("      <div class='modal-footer'>")
                .append("        <button type='button' class='btn btn-secondary' data-bs-dismiss='modal'>No, volver</button>")
                .append("        <button type='button' id='confirmCancelBookingBtn' class='btn btn-primary'>Sí, cancelar</button>")
                .append("      </div>")
                .append("    </div>")
                .append("  </div>")
                .append("</div>");
        // Formulario oculto
        shiftsHtml.append("<form id='cancelBookingForm' method='post' action='/machines/")
                .append(machineId)
                .append("/reserve/cancel' class='d-none'>")
                .append("  <input type='hidden' name='date' id='cancelDate' />")
                .append("  <input type='hidden' name='startHour' id='cancelStartHour' />")
                .append("  <input type='hidden' name='shiftId' id='cancelShiftId' />")
                .append("</form>");
        // Script para poblar y confirmar
        shiftsHtml.append("<script>document.addEventListener('DOMContentLoaded',function(){")
                .append("var modal=document.getElementById('cancelBookingModal');")
                .append("var dateInput=document.getElementById('cancelDate');")
                .append("var hourInput=document.getElementById('cancelStartHour');")
                .append("var shiftInput=document.getElementById('cancelShiftId');")
                .append("var confirmBtn=document.getElementById('confirmCancelBookingBtn');")
                .append("modal.addEventListener('show.bs.modal',function(ev){var btn=ev.relatedTarget;dateInput.value=btn.getAttribute('data-date');hourInput.value=btn.getAttribute('data-start-hour');shiftInput.value=btn.getAttribute('data-shift-id');});")
                .append("confirmBtn.addEventListener('click',function(){document.getElementById('cancelBookingForm').submit();});")
                .append("});</script>");
        html = html.replace(SHIFTS_LIST_MARKER, shiftsHtml.toString());

        return ResponseEntity.ok().contentType(MediaType.TEXT_HTML).body(html);
    }

    /**
     * Página genérica de reservas con selector de máquina.
     * Permite elegir máquina y fecha, reservar turnos futuros y cancelar la reserva propia
     * si el turno aún no ha comenzado.
     *
     * @param session sesión HTTP
     * @param machineId id de la máquina seleccionada (opcional)
     * @param dateStr fecha en ISO (yyyy-MM-dd)
     * @param success indicador de reserva creada
     * @param canceled indicador de reserva cancelada
     * @param error mensaje de error
     * @return HTML renderizado de la página
     */
    @GetMapping({"/reservar", "/reservar.html"})
    public ResponseEntity<String> genericReservePage(HttpSession session,
            @RequestParam(value = "machineId", required = false) Long machineId,
            @RequestParam(value = "date", required = false) String dateStr,
            @RequestParam(value = "success", required = false) String success,
            @RequestParam(value = "canceled", required = false) String canceled,
            @RequestParam(value = "error", required = false) String error) throws java.io.IOException {
        // Interceptor garantiza autenticación
        Object userId = session.getAttribute("USER_ID");
        var resource = new ClassPathResource("templates/user/reservar.html");
        String html = new String(resource.getInputStream().readAllBytes(), StandardCharsets.UTF_8);

        java.util.List<Machine> machines = machineService.listAll();
        StringBuilder selector = new StringBuilder();
        selector.append("<form method='get' class='row g-3 align-items-end mb-3'>");
        selector.append("<div class='col-md-4'><label class='form-label'>Máquina</label><select name='machineId' class='form-select form-select-sm' required>");
        if (machines.isEmpty()) {
            selector.append("<option value=''>No hay máquinas</option>");
        } else {
            for (Machine m : machines) {
                selector.append("<option value='").append(m.getId()).append("'");
                if (machineId != null && m.getId().equals(machineId)) {
                    selector.append(" selected");
                }
                selector.append(">")
                        .append(safe(m.getName()))
                        .append("</option>");
            }
        }
        selector.append("</select></div>");
        java.time.LocalDate selectedDate = null;
        if (dateStr != null && !dateStr.isBlank()) {
            try {
                selectedDate = java.time.LocalDate.parse(dateStr);
            } catch (Exception ignored) {
            }
        }
        if (selectedDate == null) {
            selectedDate = java.time.LocalDate.now();
        }
        selector.append("<div class='col-md-3'><label class='form-label'>Fecha</label><input type='date' name='date' class='form-control form-control-sm' value='")
                .append(selectedDate)
                .append("' /></div>");
        selector.append("<div class='col-auto'><button class='btn btn-sm btn-primary'>Ver turnos</button></div>");
        selector.append("</form>");
        html = html.replace(RES_MACHINE_SELECTOR_MARKER, selector.toString());

        StringBuilder slotsHtml = new StringBuilder();
        if (success != null) {
            slotsHtml.append("<div class='alert alert-success py-1'>Reserva creada correctamente.</div>");
        } else if (canceled != null) {
            slotsHtml.append("<div class='alert alert-success py-1'>Reserva cancelada correctamente.</div>");
        } else if (error != null) {
            slotsHtml.append("<div class='alert alert-danger py-1'>").append(safe(error)).append("</div>");
        }
        if (machineId == null) {
            slotsHtml.append("<div class='alert alert-info'>Seleccione una máquina para ver turnos.</div>");
        } else {
            var machineOpt = machineService.findById(machineId);
            if (machineOpt.isEmpty()) {
                slotsHtml.append("<div class='alert alert-danger'>Máquina no encontrada.</div>");
            } else {
                Machine machine = machineOpt.get();
                java.util.List<Shift> existingShifts = shiftService.findByMachineAndDate(machine, selectedDate);
                java.util.Map<LocalTime, Shift> shiftByStart = new java.util.HashMap<>();
                for (Shift s : existingShifts) {
                    shiftByStart.put(s.getStartTime(), s);
                }
                slotsHtml.append("<div class='list-group mb-4'>");
                java.time.LocalDateTime now2 = java.time.LocalDateTime.now();
                for (int hour = 9; hour < 21; hour++) {
                    LocalTime start = LocalTime.of(hour, 0);
                    LocalTime end = start.plusHours(1);
                    Shift s = shiftByStart.get(start);
                    boolean reservado = false;
                    boolean reservadoPorMi = false;
                    if (s != null) {
                        reservado = !s.getBookings().isEmpty() || s.getStatus() == ShiftStatus.Reservado;
                        if (reservado) {
                            reservadoPorMi = s.getBookings().stream().anyMatch(b -> b.getUser() != null && b.getUser().getId().equals(userId));
                        }
                    }
                    slotsHtml.append("<div class='list-group-item d-flex justify-content-between align-items-center'>");
                    slotsHtml.append("<div><span class='fw-semibold'>").append(start).append(" - ").append(end).append("</span> ");
                    if (reservado) {
                        slotsHtml.append("<span class='badge bg-secondary'>Reservado</span>");
                        if (reservadoPorMi) {
                            slotsHtml.append(" <span class='badge bg-warning text-dark'>Tu reserva</span>");
                        }
                    } else if (machine.getStatus() != null && machine.getStatus().name().equalsIgnoreCase("Disponible")) {
                        slotsHtml.append("<span class='badge bg-success'>Disponible</span>");
                    } else {
                        slotsHtml.append("<span class='badge bg-danger'>Máquina no disponible</span>");
                    }
                    slotsHtml.append("</div>");
                    boolean maquinaDisponible = machine.getStatus() != null && machine.getStatus().name().equalsIgnoreCase("Disponible");
                    boolean slotEnFuturo = java.time.LocalDateTime.of(selectedDate, start).isAfter(now2);
                    if (!reservado && maquinaDisponible && slotEnFuturo) {
                        slotsHtml.append("<form method='post' action='/reservar' class='m-0'>")
                                .append("<input type='hidden' name='machineId' value='").append(machineId).append("' />")
                                .append("<input type='hidden' name='date' value='").append(selectedDate).append("' />")
                                .append("<input type='hidden' name='startHour' value='").append(hour).append("' />")
                                .append("<button class='btn btn-sm btn-outline-primary'>Reservar</button></form>");
                    } else {
                        slotsHtml.append("<button class='btn btn-sm btn-outline-secondary' disabled>Reservar</button>");
                    }
                    if (reservado && reservadoPorMi && slotEnFuturo && s != null) {
                        slotsHtml.append("<button type='button' class='btn btn-sm btn-outline-danger ms-2' ")
                                .append("data-bs-toggle='modal' data-bs-target='#cancelBookingModal' ")
                                .append("data-shift-id='").append(s.getId()).append("' ")
                                .append("data-start-hour='").append(hour).append("' ")
                                .append("data-date='").append(selectedDate).append("' ")
                                .append("data-machine-id='").append(machineId).append("'>Cancelar</button>");
                    }
                    slotsHtml.append("</div>");
                }
                slotsHtml.append("</div>");
                // Modal de confirmación + formulario oculto para cancelar
                slotsHtml.append("<div class='modal fade' id='cancelBookingModal' tabindex='-1' aria-labelledby='cancelBookingModalLabel' aria-hidden='true'>")
                        .append("  <div class='modal-dialog'>")
                        .append("    <div class='modal-content'>")
                        .append("      <div class='modal-header'>")
                        .append("        <h1 class='modal-title fs-5' id='cancelBookingModalLabel'>Confirmar cancelación de reserva</h1>")
                        .append("        <button type='button' class='btn-close' data-bs-dismiss='modal' aria-label='Cerrar'></button>")
                        .append("      </div>")
                        .append("      <div class='modal-body'>")
                        .append("        <p>¿Seguro que deseas cancelar esta reserva?</p>")
                        .append("      </div>")
                        .append("      <div class='modal-footer'>")
                        .append("        <button type='button' class='btn btn-secondary' data-bs-dismiss='modal'>No, volver</button>")
                        .append("        <button type='button' id='confirmCancelBookingBtn' class='btn btn-primary'>Sí, cancelar</button>")
                        .append("      </div>")
                        .append("    </div>")
                        .append("  </div>")
                        .append("</div>");
                // Formulario oculto para la página genérica
                slotsHtml.append("<form id='cancelBookingForm' method='post' action='/reservar/cancel' class='d-none'>")
                        .append("  <input type='hidden' name='machineId' id='cancelMachineId' />")
                        .append("  <input type='hidden' name='date' id='cancelDate' />")
                        .append("  <input type='hidden' name='startHour' id='cancelStartHour' />")
                        .append("  <input type='hidden' name='shiftId' id='cancelShiftId' />")
                        .append("</form>");
                // Script para poblar y confirmar
                slotsHtml.append("<script>document.addEventListener('DOMContentLoaded',function(){")
                        .append("var modal=document.getElementById('cancelBookingModal');")
                        .append("var mInput=document.getElementById('cancelMachineId');")
                        .append("var dateInput=document.getElementById('cancelDate');")
                        .append("var hourInput=document.getElementById('cancelStartHour');")
                        .append("var shiftInput=document.getElementById('cancelShiftId');")
                        .append("var confirmBtn=document.getElementById('confirmCancelBookingBtn');")
                        .append("modal.addEventListener('show.bs.modal',function(ev){var btn=ev.relatedTarget;mInput.value=btn.getAttribute('data-machine-id');dateInput.value=btn.getAttribute('data-date');hourInput.value=btn.getAttribute('data-start-hour');shiftInput.value=btn.getAttribute('data-shift-id');});")
                        .append("confirmBtn.addEventListener('click',function(){document.getElementById('cancelBookingForm').submit();});")
                        .append("});</script>");
            }
        }
        html = html.replace(RES_SLOTS_MARKER, slotsHtml.toString());
        return ResponseEntity.ok().contentType(MediaType.TEXT_HTML).body(html);
    }

    /**
     * Crea una reserva desde la página genérica.
     * Valida que el turno esté en el rango permitido (09–21) y en el futuro.
     *
     * @param session sesión HTTP
     * @param machineId id de la máquina
     * @param startHourStr hora de inicio (9..20)
     * @param dateStr fecha del turno (yyyy-MM-dd)
     * @return redirección a la página genérica con estado
     */
    @PostMapping("/reservar")
    public String genericCreateBooking(HttpSession session,
            @RequestParam("machineId") Long machineId,
            @RequestParam("startHour") String startHourStr,
            @RequestParam("date") String dateStr) {
        // Interceptor garantiza autenticación
        Object userId = session.getAttribute("USER_ID");
        var machineOpt = machineService.findById(machineId);
        if (machineOpt.isEmpty()) {
            return "redirect:/reservar?error=" + java.net.URLEncoder.encode("Máquina no encontrada", StandardCharsets.UTF_8);
        }
        Machine machine = machineOpt.get();
        LocalDate date = LocalDate.now();
        if (dateStr != null && !dateStr.isBlank()) {
            try {
                date = LocalDate.parse(dateStr);
            } catch (Exception ignored) {
            }
        }
        Long uid = (Long) userId;
        var userOpt = userService.findById(uid);
        if (userOpt.isEmpty()) {
            return "redirect:/login?error=" + java.net.URLEncoder.encode("Usuario inválido", StandardCharsets.UTF_8);
        }
        User user = userOpt.get();
        int hour;
        try {
            hour = Integer.parseInt(startHourStr);
        } catch (NumberFormatException ex) {
            return "redirect:/reservar?error=" + java.net.URLEncoder.encode("Hora inválida", StandardCharsets.UTF_8) + "&machineId=" + machineId + "&date=" + date;
        }
        if (hour < 9 || hour > 20) {
            return "redirect:/reservar?error=" + java.net.URLEncoder.encode("Hora fuera de rango", StandardCharsets.UTF_8) + "&machineId=" + machineId + "&date=" + date;
        }
        LocalTime start = LocalTime.of(hour, 0);
        // Validar que el turno está en el futuro respecto a ahora
        if (!java.time.LocalDateTime.of(date, start).isAfter(java.time.LocalDateTime.now())) {
            return "redirect:/reservar?error=" + java.net.URLEncoder.encode("No se puede reservar en el pasado", StandardCharsets.UTF_8) + "&machineId=" + machineId + "&date=" + date;
        }
        Shift found = null;
        for (Shift s : shiftService.findByMachineAndDate(machine, date)) {
            if (s.getStartTime().equals(start)) {
                found = s;
                break;
            }
        }
        Shift shift;
        if (found != null) {
            shift = found;
            boolean reservado = !shift.getBookings().isEmpty() || shift.getStatus() == ShiftStatus.Reservado;
            if (reservado && bookingService.findByUserAndShift(user, shift).isEmpty()) {
                return "redirect:/reservar?error=" + java.net.URLEncoder.encode("Turno ya reservado", StandardCharsets.UTF_8) + "&machineId=" + machineId + "&date=" + date;
            }
        } else {
            shift = new Shift();
            shift.setMachine(machine);
            shift.setDate(date);
            shift.setStartTime(start);
            shift.setEndTime(start.plusHours(1));
            shift.setStatus(ShiftStatus.Disponible);
            shift = shiftService.save(shift);
        }
        if (shift.getStatus() != ShiftStatus.Disponible && bookingService.findByUserAndShift(user, shift).isEmpty()) {
            return "redirect:/reservar?error=" + java.net.URLEncoder.encode("Turno no disponible", StandardCharsets.UTF_8) + "&machineId=" + machineId + "&date=" + date;
        }
        if (bookingService.findByUserAndShift(user, shift).isPresent()) {
            return "redirect:/reservar?error=" + java.net.URLEncoder.encode("Ya reservaste este turno", StandardCharsets.UTF_8) + "&machineId=" + machineId + "&date=" + date;
        }
        try {
            Booking b = new Booking();
            b.setUser(user);
            b.setShift(shift);
            b.setFechaReserva(LocalDate.now());
            bookingService.save(b);
            if (shift.getStatus() == ShiftStatus.Disponible) {
                shift.setStatus(ShiftStatus.Reservado);
                shiftService.save(shift);
            }
            return "redirect:/reservar?success=1&machineId=" + machineId + "&date=" + date;
        } catch (Exception ex) {
            return "redirect:/reservar?error=" + java.net.URLEncoder.encode("Error al reservar", StandardCharsets.UTF_8) + "&machineId=" + machineId + "&date=" + date;
        }
    }

    /**
     * Cancela una reserva propia desde la página genérica.
     * Solo permite cancelar si el turno aún no ha comenzado.
     *
     * @param session sesión HTTP
     * @param machineId id de la máquina
     * @param startHourStr hora de inicio del turno
     * @param dateStr fecha del turno
     * @param shiftId id del turno (opcional)
     * @return redirección con estado de cancelación
     */
    @PostMapping("/reservar/cancel")
    public String genericCancelBooking(HttpSession session,
            @RequestParam("machineId") Long machineId,
            @RequestParam("startHour") String startHourStr,
            @RequestParam("date") String dateStr,
            @RequestParam(value = "shiftId", required = false) Long shiftId) {
        Object userId = session.getAttribute("USER_ID");
        var machineOpt = machineService.findById(machineId);
        if (machineOpt.isEmpty()) {
            return "redirect:/reservar?error=" + java.net.URLEncoder.encode("Máquina no encontrada", StandardCharsets.UTF_8);
        }
        Machine machine = machineOpt.get();
        LocalDate date = LocalDate.now();
        if (dateStr != null && !dateStr.isBlank()) {
            try {
                date = LocalDate.parse(dateStr);
            } catch (Exception ignored) {
            }
        }
        int hour;
        try {
            hour = Integer.parseInt(startHourStr);
        } catch (NumberFormatException ex) {
            return "redirect:/reservar?error=" + java.net.URLEncoder.encode("Hora inválida", StandardCharsets.UTF_8) + "&machineId=" + machineId + "&date=" + date;
        }
        LocalTime start = LocalTime.of(hour, 0);
        if (!java.time.LocalDateTime.of(date, start).isAfter(java.time.LocalDateTime.now())) {
            return "redirect:/reservar?error=" + java.net.URLEncoder.encode("No se puede cancelar una reserva pasada", StandardCharsets.UTF_8) + "&machineId=" + machineId + "&date=" + date;
        }
        Long uid = (Long) userId;
        var userOpt = userService.findById(uid);
        if (userOpt.isEmpty()) {
            return "redirect:/login?error=" + java.net.URLEncoder.encode("Usuario inválido", StandardCharsets.UTF_8);
        }
        User user = userOpt.get();
        Shift shift = null;
        if (shiftId != null) {
            var shOpt = shiftService.findById(shiftId);
            if (shOpt.isPresent()) {
                shift = shOpt.get();
            }
        }
        if (shift == null) {
            for (Shift s : shiftService.findByMachineAndDate(machine, date)) {
                if (s.getStartTime().equals(start)) {
                    shift = s;
                    break;
                }
            }
        }
        if (shift == null) {
            return "redirect:/reservar?error=" + java.net.URLEncoder.encode("Turno no encontrado", StandardCharsets.UTF_8) + "&machineId=" + machineId + "&date=" + date;
        }
        var bookingOpt = bookingService.findByUserAndShift(user, shift);
        if (bookingOpt.isEmpty()) {
            return "redirect:/reservar?error=" + java.net.URLEncoder.encode("No tienes reserva en este turno", StandardCharsets.UTF_8) + "&machineId=" + machineId + "&date=" + date;
        }
        try {
            Booking b = bookingOpt.get();
            bookingService.delete(b.getId());
            // Si no quedan reservas, marcar el turno como disponible
            var refreshedShiftOpt = shiftService.findById(shift.getId());
            if (refreshedShiftOpt.isPresent() && (refreshedShiftOpt.get().getBookings() == null || refreshedShiftOpt.get().getBookings().isEmpty())) {
                Shift rs = refreshedShiftOpt.get();
                rs.setStatus(ShiftStatus.Disponible);
                shiftService.save(rs);
            }
            return "redirect:/reservar?canceled=1&machineId=" + machineId + "&date=" + date;
        } catch (Exception ex) {
            return "redirect:/reservar?error=" + java.net.URLEncoder.encode("Error al cancelar", StandardCharsets.UTF_8) + "&machineId=" + machineId + "&date=" + date;
        }
    }

    /**
     * Crea una reserva desde la página de la máquina.
     * Valida hora y fecha y crea turno si no existía.
     *
     * @param machineId id de la máquina
     * @param shiftId id del turno (opcional)
     * @param startHourStr hora inicio (9..20)
     * @param dateStr fecha del turno
     * @param session sesión HTTP
     * @return redirección a la página de reserva de máquina con estado
     */
    @PostMapping("/machines/{id}/reserve")
    public String createBooking(@PathVariable("id") Long machineId,
            @RequestParam(value = "shiftId", required = false) Long shiftId,
            @RequestParam(value = "startHour", required = false) String startHourStr,
            @RequestParam(value = "date", required = false) String dateStr,
            HttpSession session) {
        // Interceptor garantiza autenticación
        Object userId = session.getAttribute("USER_ID");
        var machineOpt = machineService.findById(machineId);
        if (machineOpt.isEmpty()) {
            return "redirect:/machines?error=" + java.net.URLEncoder.encode("Máquina no encontrada", StandardCharsets.UTF_8);
        }
        Machine machine = machineOpt.get();
        LocalDate date = LocalDate.now();
        if (dateStr != null && !dateStr.isBlank()) {
            try {
                date = LocalDate.parse(dateStr);
            } catch (Exception ignored) {
            }
        }
        Long uid = (Long) userId;
        var userOpt = userService.findById(uid);
        if (userOpt.isEmpty()) {
            return "redirect:/login?error=" + java.net.URLEncoder.encode("Usuario inválido", StandardCharsets.UTF_8);
        }
        User user = userOpt.get();

        Shift shift;
        if (shiftId != null) {
            var shiftOpt = shiftService.findById(shiftId);
            if (shiftOpt.isEmpty()) {
                return "redirect:/machines/" + machineId + "/reserve?error=" + java.net.URLEncoder.encode("Turno no encontrado", StandardCharsets.UTF_8) + "&date=" + date;
            }
            shift = shiftOpt.get();
            if (!shift.getMachine().getId().equals(machineId)) {
                return "redirect:/machines/" + machineId + "/reserve?error=" + java.net.URLEncoder.encode("Turno inválido", StandardCharsets.UTF_8) + "&date=" + date;
            }
        } else if (startHourStr != null) {
            int hour;
            try {
                hour = Integer.parseInt(startHourStr);
            } catch (NumberFormatException ex) {
                return "redirect:/machines/" + machineId + "/reserve?error=" + java.net.URLEncoder.encode("Hora inválida", StandardCharsets.UTF_8) + "&date=" + date;
            }
            if (hour < 9 || hour > 20) {
                return "redirect:/machines/" + machineId + "/reserve?error=" + java.net.URLEncoder.encode("Hora fuera de rango", StandardCharsets.UTF_8) + "&date=" + date;
            }
            LocalTime start = LocalTime.of(hour, 0);
            if (!java.time.LocalDateTime.of(date, start).isAfter(java.time.LocalDateTime.now())) {
                return "redirect:/machines/" + machineId + "/reserve?error=" + java.net.URLEncoder.encode("No se puede reservar en el pasado", StandardCharsets.UTF_8) + "&date=" + date;
            }
            Shift found = null;
            for (Shift s : shiftService.findByMachineAndDate(machine, date)) {
                if (s.getStartTime().equals(start)) {
                    found = s;
                    break;
                }
            }
            if (found != null) {
                shift = found;
                // Verificar que no esté ya reservado completamente
                boolean reservado = !shift.getBookings().isEmpty() || shift.getStatus() == ShiftStatus.Reservado;
                if (reservado && bookingService.findByUserAndShift(user, shift).isEmpty()) {
                    return "redirect:/machines/" + machineId + "/reserve?error=" + java.net.URLEncoder.encode("Turno ya reservado", StandardCharsets.UTF_8) + "&date=" + date;
                }
            } else {
                // Crear turno nuevo disponible
                shift = new Shift();
                shift.setMachine(machine);
                shift.setDate(date);
                shift.setStartTime(start);
                shift.setEndTime(start.plusHours(1));
                shift.setStatus(ShiftStatus.Disponible);
                shift = shiftService.save(shift);
            }
        } else {
            return "redirect:/machines/" + machineId + "/reserve?error=" + java.net.URLEncoder.encode("Parámetros insuficientes", StandardCharsets.UTF_8) + "&date=" + date;
        }

        // Validar disponibilidad
        if (shift.getStatus() != ShiftStatus.Disponible && bookingService.findByUserAndShift(user, shift).isEmpty()) {
            return "redirect:/machines/" + machineId + "/reserve?error=" + java.net.URLEncoder.encode("Turno no disponible", StandardCharsets.UTF_8) + "&date=" + date;
        }
        if (bookingService.findByUserAndShift(user, shift).isPresent()) {
            return "redirect:/machines/" + machineId + "/reserve?error=" + java.net.URLEncoder.encode("Ya reservaste este turno", StandardCharsets.UTF_8) + "&date=" + date;
        }
        try {
            Booking b = new Booking();
            b.setUser(user);
            b.setShift(shift);
            b.setFechaReserva(LocalDate.now());
            bookingService.save(b);
            // Marcar turno como Reservado si queremos reflejar estado
            if (shift.getStatus() == ShiftStatus.Disponible) {
                shift.setStatus(ShiftStatus.Reservado);
                shiftService.save(shift);
            }
            return "redirect:/machines/" + machineId + "/reserve?success=1&date=" + date;
        } catch (Exception ex) {
            return "redirect:/machines/" + machineId + "/reserve?error=" + java.net.URLEncoder.encode("Error al reservar", StandardCharsets.UTF_8) + "&date=" + date;
        }
    }

    /**
     * Cancela una reserva propia desde la página de la máquina.
     * Solo si el turno aún no ha comenzado.
     *
     * @param machineId id de la máquina
     * @param shiftId id del turno (opcional)
     * @param startHourStr hora de inicio
     * @param dateStr fecha del turno
     * @param session sesión HTTP
     * @return redirección con estado de cancelación
     */
    @PostMapping("/machines/{id}/reserve/cancel")
    public String cancelBooking(@PathVariable("id") Long machineId,
            @RequestParam(value = "shiftId", required = false) Long shiftId,
            @RequestParam(value = "startHour", required = false) String startHourStr,
            @RequestParam(value = "date", required = false) String dateStr,
            HttpSession session) {
        Object userId = session.getAttribute("USER_ID");
        var machineOpt = machineService.findById(machineId);
        if (machineOpt.isEmpty()) {
            return "redirect:/machines?error=" + java.net.URLEncoder.encode("Máquina no encontrada", StandardCharsets.UTF_8);
        }
        Machine machine = machineOpt.get();
        LocalDate date = LocalDate.now();
        if (dateStr != null && !dateStr.isBlank()) {
            try {
                date = LocalDate.parse(dateStr);
            } catch (Exception ignored) {
            }
        }
        int hour = -1;
        if (startHourStr != null) {
            try {
                hour = Integer.parseInt(startHourStr);
            } catch (NumberFormatException ignored) {
            }
        }
        LocalTime start = hour >= 0 ? LocalTime.of(hour, 0) : null;
        if (start != null && !java.time.LocalDateTime.of(date, start).isAfter(java.time.LocalDateTime.now())) {
            return "redirect:/machines/" + machineId + "/reserve?error=" + java.net.URLEncoder.encode("No se puede cancelar una reserva pasada", StandardCharsets.UTF_8) + "&date=" + date;
        }
        Long uid = (Long) userId;
        var userOpt = userService.findById(uid);
        if (userOpt.isEmpty()) {
            return "redirect:/login?error=" + java.net.URLEncoder.encode("Usuario inválido", StandardCharsets.UTF_8);
        }
        User user = userOpt.get();
        Shift shift = null;
        if (shiftId != null) {
            var shOpt = shiftService.findById(shiftId);
            if (shOpt.isPresent()) {
                shift = shOpt.get();
            }
        }
        if (shift == null && start != null) {
            for (Shift s : shiftService.findByMachineAndDate(machine, date)) {
                if (s.getStartTime().equals(start)) {
                    shift = s;
                    break;
                }
            }
        }
        if (shift == null) {
            return "redirect:/machines/" + machineId + "/reserve?error=" + java.net.URLEncoder.encode("Turno no encontrado", StandardCharsets.UTF_8) + "&date=" + date;
        }
        var bookingOpt = bookingService.findByUserAndShift(user, shift);
        if (bookingOpt.isEmpty()) {
            return "redirect:/machines/" + machineId + "/reserve?error=" + java.net.URLEncoder.encode("No tienes reserva en este turno", StandardCharsets.UTF_8) + "&date=" + date;
        }
        try {
            Booking b = bookingOpt.get();
            bookingService.delete(b.getId());
            var refreshedShiftOpt = shiftService.findById(shift.getId());
            if (refreshedShiftOpt.isPresent() && (refreshedShiftOpt.get().getBookings() == null || refreshedShiftOpt.get().getBookings().isEmpty())) {
                Shift rs = refreshedShiftOpt.get();
                rs.setStatus(ShiftStatus.Disponible);
                shiftService.save(rs);
            }
            return "redirect:/machines/" + machineId + "/reserve?canceled=1&date=" + date;
        } catch (Exception ex) {
            return "redirect:/machines/" + machineId + "/reserve?error=" + java.net.URLEncoder.encode("Error al cancelar", StandardCharsets.UTF_8) + "&date=" + date;
        }
    }

    private static String safe(String s) {
        if (s == null) {
            return "";
        }
        return s.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }
}
