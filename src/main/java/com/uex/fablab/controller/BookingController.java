package com.uex.fablab.controller;

import java.nio.charset.StandardCharsets;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
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
 *
 * <p>Gestiona las vistas y acciones relacionadas con la reserva y
 * cancelación de turnos para las máquinas. Está pensado para ser usado
 * por capas de presentación basadas en Thymeleaf: los métodos preparan
 * información en el {@code Model} y devuelven nombres de vistas o
 * redirecciones con parámetros de estado.</p>
 *
 * <p>Responsabilidades principales:
 * - Mostrar la página de reserva de una máquina con sus franjas horarias.
 * - Crear reservas para usuarios autenticados, validando reglas de negocio
 *   (no reservar en fines de semana, no reservar en el pasado, controlar
 *   duplicados, etc.).
 * - Cancelar reservas por el usuario que reservó o por un administrador
 *   siempre que el turno no haya comenzado.</p>
 *
 * <p>El controlador delega la lógica de acceso y persistencia a los
 * servicios inyectados: {@code MachineService}, {@code ShiftService},
 * {@code BookingService} y {@code UserService}.</p>
 */
@Controller
public class BookingController {

    private final MachineService machineService;
    private final ShiftService shiftService;
    private final BookingService bookingService;
    private final UserService userService;

    /**
     * Constructor del controlador de reservas.
     *
     * @param machineService servicio para operaciones sobre máquinas
     * @param shiftService servicio para operaciones sobre turnos
     * @param bookingService servicio para operaciones sobre reservas
     * @param userService servicio para operaciones sobre usuarios
     */
    public BookingController(MachineService machineService, ShiftService shiftService, BookingService bookingService, UserService userService) {
        this.machineService = machineService;
        this.shiftService = shiftService;
        this.bookingService = bookingService;
        this.userService = userService;
    }

    /**
     * Muestra la página de reserva para una máquina específica (vista Thymeleaf).
     *
     * <p>Prepara los slots horarios de la máquina para la fecha seleccionada,
     * añade flags de estado en el modelo (si el usuario es admin, mensajes de
     * éxito/error) y devuelve la vista {@code user/machine-reserve}.</p>
     *
     * @param machineId id de la máquina
     * @param session sesión HTTP (contiene {@code USER_ID})
     * @param dateStr fecha seleccionada en formato ISO (opcional)
     * @param successParam indicador de operación exitosa (opcional)
     * @param canceledParam indicador de cancelación (opcional)
     * @param errorParam texto de error (opcional)
     * @param model modelo de la vista donde se añaden atributos
     * @return nombre de la vista Thymeleaf
     */
    @GetMapping("/machines/{id}/reserve")
    public String reserveMachinePage(@PathVariable("id") Long machineId, HttpSession session,
            @RequestParam(value = "date", required = false) String dateStr,
            @RequestParam(value = "success", required = false) String successParam,
            @RequestParam(value = "canceled", required = false) String canceledParam,
            @RequestParam(value = "error", required = false) String errorParam,
            Model model) {
        Object userId = session.getAttribute("USER_ID");
        var machineOpt = machineService.findById(machineId);
        if (machineOpt.isEmpty()) {
            return "redirect:/machines?error=" + java.net.URLEncoder.encode("Máquina no encontrada", StandardCharsets.UTF_8);
        }
        Machine machine = machineOpt.get();
        LocalDate selectedDate = null;
        if (dateStr != null && !dateStr.isBlank()) {
            try { selectedDate = LocalDate.parse(dateStr); } catch (Exception ignored) {}
        }
        if (selectedDate == null) { selectedDate = LocalDate.now(); }
        List<Shift> existingShifts = shiftService.findByMachineAndDate(machine, selectedDate);
        // Construir vista de slots
        List<SlotView> slots = buildSlots(existingShifts, machine, selectedDate, userId);
        // Exponer en el modelo si el usuario es admin y su id para la vista
        Long currentUserId = null;
        boolean isAdmin = false;
        if (userId instanceof Long aLong) {
            currentUserId = aLong;
            var uopt = userService.findById(currentUserId);
            if (uopt.isPresent()) isAdmin = uopt.get().isAdmin();
        }
        model.addAttribute("isAdmin", isAdmin);
        model.addAttribute("currentUserId", currentUserId);
        if (successParam != null) model.addAttribute("messageSuccess", "Reserva creada correctamente.");
        if (canceledParam != null) model.addAttribute("messageSuccess", "Reserva cancelada correctamente.");
        if (errorParam != null) model.addAttribute("messageError", errorParam);
        model.addAttribute("machine", machine);
        model.addAttribute("selectedDate", selectedDate);
        model.addAttribute("slots", slots);
        return "user/machine-reserve";
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
        // No permitir reservas en fines de semana
        if (date.getDayOfWeek() == DayOfWeek.SATURDAY || date.getDayOfWeek() == DayOfWeek.SUNDAY) {
            return "redirect:/machines/" + machineId + 
                    "/reserve?error=" + java.net.URLEncoder.encode("No se puede reservar fines de semana", StandardCharsets.UTF_8) + "&date=" + date;
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
            @RequestParam(value = "bookingId", required = false) Long bookingId,
            @RequestParam(value = "bookingUserId", required = false) Long bookingUserId,
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
        java.time.LocalDateTime now = java.time.LocalDateTime.now();
        if (start != null) {
            LocalTime end = start.plusHours(1);
            if (!java.time.LocalDateTime.of(date, end).isAfter(now)) {
                return "redirect:/machines/" + machineId + "/reserve?error=" + java.net.URLEncoder.encode("No se puede cancelar una reserva pasada", StandardCharsets.UTF_8) + "&date=" + date;
            }
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
        java.util.Optional<Booking> bookingOpt;
        boolean isAdmin = user.isAdmin();
        if (isAdmin) {
            if (bookingId != null) {
                bookingOpt = bookingService.findById(bookingId);
            } else if (bookingUserId != null) {
                var targetUserOpt = userService.findById(bookingUserId);
                if (targetUserOpt.isPresent()) {
                    bookingOpt = bookingService.findByUserAndShift(targetUserOpt.get(), shift);
                } else {
                    bookingOpt = java.util.Optional.empty();
                }
            } else {
                Long _shiftIdForFilter = shift.getId();
                bookingOpt = bookingService.listAll().stream().filter(b -> b.getShift() != null && b.getShift().getId().equals(_shiftIdForFilter)).findFirst();
            }
            if (bookingOpt.isEmpty()) {
                return "redirect:/machines/" + machineId + "/reserve?error=" + java.net.URLEncoder.encode("No hay reservas en este turno para cancelar", StandardCharsets.UTF_8) + "&date=" + date;
            }
        } else {
            bookingOpt = bookingService.findByUserAndShift(user, shift);
            if (bookingOpt.isEmpty()) {
                return "redirect:/machines/" + machineId + "/reserve?error=" + java.net.URLEncoder.encode("No tienes reserva en este turno", StandardCharsets.UTF_8) + "&date=" + date;
            }
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

    /** DTO simple para representar un slot horario en la vista. */
    public static class SlotView {
        public int hour;
        public LocalTime startTime;
        public LocalTime endTime;
        public boolean reservado;
        public boolean reservadoPorMi;
        public boolean canReserve;
        public boolean canCancel;
        public java.util.List<Booking> bookings = java.util.List.of();
        public Long shiftId;
    }

    /**
     * Construye la lista de {@link SlotView} para la vista de reserva de la
     * máquina.
     *
     * <p>Genera slots horarios desde las 09:00 hasta las 20:00 teniendo en
     * cuenta la existencia de turnos, reservas, si el usuario es administrador
     * o el propietario de la reserva, y si la franja está en el futuro.</p>
     *
     * @param existing lista de turnos existentes para la máquina y fecha
     * @param machine la máquina consultada
     * @param date la fecha objetivo
     * @param userId id del usuario en sesión (puede ser null)
     * @return lista ordenada de {@link SlotView}
     */
    private List<SlotView> buildSlots(List<Shift> existing, Machine machine, LocalDate date, Object userId) {
        java.util.Map<LocalTime, Shift> shiftByStart = new java.util.HashMap<>();
        for (Shift s : existing) {
            shiftByStart.put(s.getStartTime(), s);
        }
        List<SlotView> slots = new ArrayList<>();
        java.time.LocalDateTime now = java.time.LocalDateTime.now();
        boolean isAdmin = false;
        if (userId instanceof Long aLong) {
            var uopt = userService.findById(aLong);
            if (uopt.isPresent()) isAdmin = uopt.get().isAdmin();
        }
        boolean isWeekend = date.getDayOfWeek() == DayOfWeek.SATURDAY || date.getDayOfWeek() == DayOfWeek.SUNDAY;
        for (int hour = 9; hour < 21; hour++) {
            LocalTime start = LocalTime.of(hour, 0);
            LocalTime end = start.plusHours(1);
            Shift s = shiftByStart.get(start);
            boolean reservado = false;
            boolean reservadoPorMi = false;
            if (s != null) {
                var bookings = s.getBookings();
                // comprobar de forma eficiente en repositorio si hay reservas para el turno
                boolean anyInRepo = bookingService.existsByShift(s);
                reservado = (!bookings.isEmpty()) || anyInRepo || s.getStatus() == ShiftStatus.Reservado;
                if (reservado) {
                    if (userId instanceof Long uid) {
                        reservadoPorMi = bookingService.findByUserAndShift(userService.findById(uid).orElse(null), s).isPresent();
                    } else {
                        reservadoPorMi = bookings.stream().anyMatch(b -> b.getUser() != null && b.getUser().getId().equals(userId));
                    }
                }
            }
            boolean maquinaDisponible = machine != null && machine.getStatus() != null && machine.getStatus().name().equalsIgnoreCase("Disponible");
            boolean slotStartsInFuture = java.time.LocalDateTime.of(date, start).isAfter(now);
            SlotView sv = new SlotView();
            sv.hour = hour;
            sv.startTime = start;
            sv.endTime = end;
            sv.reservado = reservado;
            sv.reservadoPorMi = reservadoPorMi;
            sv.canReserve = !reservado && maquinaDisponible && slotStartsInFuture && !isWeekend;
            // Permitir cancelar si eres el que reservó, o si eres admin y el slot está reservado.
            // Se permite cancelar mientras el turno no haya empezado (start > now).
            sv.canCancel = reservado && slotStartsInFuture && s != null && (reservadoPorMi || isAdmin);
            sv.shiftId = s != null ? s.getId() : null;
            if (s != null) {
                sv.bookings = new ArrayList<>(s.getBookings());
            }
            slots.add(sv);
        }
        return slots;
    }
}
