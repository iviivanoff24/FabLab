package com.uex.fablab.controller;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalTime;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.uex.fablab.data.model.Booking;
import com.uex.fablab.data.model.Inscription;
import com.uex.fablab.data.model.PaymentMethod;
import com.uex.fablab.data.model.Receipt;
import com.uex.fablab.data.model.ReceiptStatus;
import com.uex.fablab.data.model.Shift;
import com.uex.fablab.data.model.ShiftStatus;
import com.uex.fablab.data.model.User;
import com.uex.fablab.data.services.BookingService;
import com.uex.fablab.data.services.CourseService;
import com.uex.fablab.data.services.InscriptionService;
import com.uex.fablab.data.services.MachineService;
import com.uex.fablab.data.services.ReceiptService;
import com.uex.fablab.data.services.ShiftService;
import com.uex.fablab.data.services.UserService;

import jakarta.servlet.http.HttpSession;

/**
 * Controlador simple para la página de pago. Esta implementación ofrece una
 * vista de pago y una acción simulada de procesamiento. En producción hay que
 * integrar un proveedor de pagos (Stripe, PayPal, etc.)
 */
@Controller
public class PaymentController {

    private final UserService userService;
    private final BookingService bookingService;
    private final ShiftService shiftService;
    private final MachineService machineService;
    private final CourseService courseService;
    private final InscriptionService inscriptionService;
    private final ReceiptService receiptService;

    public PaymentController(UserService userService, BookingService bookingService, ShiftService shiftService,
            MachineService machineService, CourseService courseService, InscriptionService inscriptionService, ReceiptService receiptService) {
        this.userService = userService;
        this.bookingService = bookingService;
        this.shiftService = shiftService;
        this.machineService = machineService;
        this.courseService = courseService;
        this.inscriptionService = inscriptionService;
        this.receiptService = receiptService;
    }

    /**
     * Muestra la página de pago. Requiere usuario logueado.
     */
    @GetMapping("/payment")
        public String showPaymentPage(@RequestParam(value = "type", required = false) String type,
            @RequestParam(value = "itemId", required = false) Long itemId,
            @RequestParam(value = "itemName", required = false) String itemName,
            @RequestParam(value = "amount", required = false) String amount,
            @RequestParam(value = "returnUrl", required = false) String returnUrl,
            @RequestParam(value = "date", required = false) String date,
            @RequestParam(value = "startHour", required = false) String startHour,
            @RequestParam(value = "shiftId", required = false) Long shiftId,
            @RequestParam(value = "shiftIds", required = false) java.util.List<Long> shiftIds,
            @RequestParam(value = "startHours", required = false) java.util.List<Integer> startHours,
            HttpSession session,
            Model model) {
        Object sid = session.getAttribute("USER_ID");
        if (!(sid instanceof Long)) {
            return "redirect:/login?error=" + URLEncoder.encode("Necesitas iniciar sesión para pagar", StandardCharsets.UTF_8);
        }
        // Calcular importe para multi-reserva si no llega por parámetro
        int selectedCount = 0;
        Double unitPrice = null;
        if (itemId != null && ("reservation_multi".equalsIgnoreCase(type) || "reservation".equalsIgnoreCase(type))) {
            var mopt = machineService.findById(itemId);
            if (mopt.isPresent() && mopt.get().getHourlyPrice() != null) {
                unitPrice = mopt.get().getHourlyPrice().doubleValue();
                // contar solo ids válidos
                selectedCount = 0;
                int sids = (shiftIds != null) ? (int) shiftIds.stream().filter(java.util.Objects::nonNull).count() : 0;
                int shours = (startHours != null) ? startHours.size() : 0;
                selectedCount = sids + shours;
                if (selectedCount == 0) selectedCount = 1; // fallback visual
                if (amount == null || amount.isBlank()) {
                    amount = String.format("%.2f", unitPrice * selectedCount);
                }
            }
        }
        model.addAttribute("type", type);
        model.addAttribute("itemId", itemId);
        model.addAttribute("itemName", itemName);
        model.addAttribute("amount", amount);
        model.addAttribute("selectedCount", selectedCount);
        model.addAttribute("unitPrice", unitPrice);
        model.addAttribute("returnUrl", returnUrl);
        model.addAttribute("date", date);
        model.addAttribute("startHour", startHour);
        model.addAttribute("shiftId", shiftId);
        model.addAttribute("shiftIds", shiftIds);
        model.addAttribute("startHours", startHours);
        // Cargar información de turnos seleccionados para mostrar en la página de pago
        java.util.List<com.uex.fablab.data.model.Shift> selectedShifts = java.util.Collections.emptyList();
        if (shiftIds != null && !shiftIds.isEmpty()) {
            selectedShifts = new java.util.ArrayList<>();
            for (Long sidShift : shiftIds) {
                if (sidShift == null) continue;
                shiftService.findById(sidShift).ifPresent(selectedShifts::add);
            }
        } else if (shiftId != null) {
            var shOpt = shiftService.findById(shiftId);
            if (shOpt.isPresent()) {
                selectedShifts = java.util.List.of(shOpt.get());
            }
        }
        model.addAttribute("selectedShifts", selectedShifts);
        model.addAttribute("selectedHours", startHours);
        model.addAttribute("currentUserId", (Long) sid);
        return "user/payment";
    }

    /**
     * Procesa (simula) el pago y, según el tipo, crea la inscripción o la
     * reserva.
     */
    @PostMapping("/payment/process")
    public String processPayment(@RequestParam(value = "type", required = false) String type,
            @RequestParam(value = "itemId", required = false) Long itemId,
            @RequestParam(value = "itemName", required = false) String itemName,
            @RequestParam(value = "amount", required = false) String amount,
            @RequestParam(value = "paymentMethod", required = false) String paymentMethod,
            @RequestParam(value = "onlineProvider", required = false) String onlineProvider,
            @RequestParam(value = "returnUrl", required = false) String returnUrl,
            @RequestParam(value = "shiftId", required = false) Long shiftId,
            @RequestParam(value = "shiftIds", required = false) java.util.List<Long> shiftIds,
            @RequestParam(value = "startHours", required = false) java.util.List<Integer> startHours,
            @RequestParam(value = "startHour", required = false) String startHourStr,
            @RequestParam(value = "date", required = false) String dateStr,
            HttpSession session) {
        Object sid = session.getAttribute("USER_ID");
        if (!(sid instanceof Long)) {
            return "redirect:/login?error=" + URLEncoder.encode("Necesitas iniciar sesión para pagar", StandardCharsets.UTF_8);
        }
        Long uid = (Long) sid;
        var uopt = userService.findById(uid);
        if (uopt.isEmpty()) {
            return "redirect:/login?error=" + URLEncoder.encode("Usuario inválido", StandardCharsets.UTF_8);
        }
        User user = uopt.get();

        try {
            // validate/compute amount server-side when possible
            Double computedAmount = null;
            // First, recompute from server-side data; avoid parsing locale-formatted strings like "5,00"
            if ("course".equalsIgnoreCase(type) && itemId != null) {
                var copt = courseService.findById(itemId);
                if (copt.isPresent()) {
                    computedAmount = copt.get().getPrecio();
                }
            }
            if (("reservation".equalsIgnoreCase(type) || "reservation_multi".equalsIgnoreCase(type)) && itemId != null) {
                var mopt = machineService.findById(itemId);
                if (mopt.isPresent() && mopt.get().getHourlyPrice() != null) {
                    double unit = mopt.get().getHourlyPrice().doubleValue();
                    int sids = (shiftIds != null) ? (int) shiftIds.stream().filter(java.util.Objects::nonNull).count() : 0;
                    int shours = (startHours != null) ? startHours.size() : 0;
                    int count = Math.max(1, sids + shours);
                    computedAmount = unit * count;
                }
            }
            if (computedAmount == null) {
                // fallback: try parsing incoming amount safely with dot
                try {
                    if (amount != null && !amount.isBlank()) {
                        computedAmount = Double.valueOf(amount.replace(',', '.'));
                    }
                } catch (NumberFormatException ignore) {
                    computedAmount = 0.0;
                }
            }
            amount = String.format(java.util.Locale.US, "%.2f", computedAmount != null ? computedAmount : 0.0);

            String pm = paymentMethod != null ? paymentMethod : "Tarjeta";
            if ("Efectivo".equalsIgnoreCase(pm)) {
                // cash: assume paid, create receipt and perform action
                Receipt r = new Receipt();
                r.setUser(user);
                r.setTotalPrice(computedAmount != null ? computedAmount : 0.0);
                r.setMetodoPago(PaymentMethod.Efectivo);
                r.setEstadoRecibo(ReceiptStatus.Pagado);
                // Set machine and concepto for reservations
                if (("reservation".equalsIgnoreCase(type) || "reservation_multi".equalsIgnoreCase(type)) && itemId != null) {
                    var mopt2 = machineService.findById(itemId);
                    mopt2.ifPresent(r::setMachine);
                    int cnt2 = 0;
                    cnt2 += (shiftIds != null) ? (int) shiftIds.stream().filter(java.util.Objects::nonNull).count() : 0;
                    cnt2 += (startHours != null) ? startHours.size() : 0;
                    String fechaTxt2 = (dateStr != null && !dateStr.isBlank()) ? dateStr : java.time.LocalDate.now().toString();
                    r.setConcepto("Reserva máquina" + (mopt2.isPresent() ? (" " + mopt2.get().getName()) : "") + " · " + cnt2 + " turno(s) · " + fechaTxt2);
                }
                // Asociar turnos si es múltiple
                if ("reservation_multi".equalsIgnoreCase(type)) {
                    if (shiftIds != null) {
                    for (Long sidShift : shiftIds) {
                        shiftService.findById(sidShift).ifPresent(r::addShift);
                    }
                    }
                    // startHours se asociarán tras crear el Shift en performPostPaymentAction
                } else if (shiftId != null) {
                    shiftService.findById(shiftId).ifPresent(r::addShift);
                }
                r = receiptService.save(r);
                return performPostPaymentAction(type, itemId, shiftId, startHourStr, dateStr, user, shiftIds, r.getId(), startHours);
            }
            if ("Online".equalsIgnoreCase(pm)) {
                // online: create pending receipt and redirect to simulated completion
                Receipt r = new Receipt();
                r.setUser(user);
                r.setTotalPrice(computedAmount != null ? computedAmount : 0.0);
                r.setMetodoPago(PaymentMethod.Online);
                r.setEstadoRecibo(ReceiptStatus.Pendiente);
                if (("reservation".equalsIgnoreCase(type) || "reservation_multi".equalsIgnoreCase(type)) && itemId != null) {
                    var mopt2 = machineService.findById(itemId);
                    mopt2.ifPresent(r::setMachine);
                    int cnt2 = 0;
                    cnt2 += (shiftIds != null) ? (int) shiftIds.stream().filter(java.util.Objects::nonNull).count() : 0;
                    cnt2 += (startHours != null) ? startHours.size() : 0;
                    String fechaTxt2 = (dateStr != null && !dateStr.isBlank()) ? dateStr : java.time.LocalDate.now().toString();
                    r.setConcepto("Reserva máquina" + (mopt2.isPresent() ? (" " + mopt2.get().getName()) : "") + " · " + cnt2 + " turno(s) · " + fechaTxt2);
                }
                // Asociar turnos si es múltiple
                if ("reservation_multi".equalsIgnoreCase(type)) {
                    if (shiftIds != null) {
                    for (Long sidShift : shiftIds) {
                        shiftService.findById(sidShift).ifPresent(r::addShift);
                    }
                    }
                    // startHours se asociarán tras crear el Shift en performPostPaymentAction
                } else if (shiftId != null) {
                    shiftService.findById(shiftId).ifPresent(r::addShift);
                }
                r = receiptService.save(r);
                String redirect = "/payment/online/complete?receiptId=" + r.getId()
                        + "&type=" + (type != null ? type : "")
                        + "&itemId=" + (itemId != null ? itemId : "")
                        + "&startHour=" + (startHourStr != null ? startHourStr : "")
                        + "&date=" + (dateStr != null ? dateStr : "")
                        + "&shiftId=" + (shiftId != null ? shiftId : "")
                        + (shiftIds != null && !shiftIds.isEmpty() ? "&shiftIds=" + shiftIds.stream().map(String::valueOf).collect(java.util.stream.Collectors.joining(",")) : "")
                        + (startHours != null && !startHours.isEmpty() ? "&startHours=" + startHours.stream().map(String::valueOf).collect(java.util.stream.Collectors.joining(",")) : "")
                        + "&returnUrl=" + (returnUrl != null ? URLEncoder.encode(returnUrl, StandardCharsets.UTF_8) : "");
                return "redirect:" + redirect;
            }
            // Tarjeta (default): simulate card payment, create paid receipt and perform action
            Receipt r = new Receipt();
            r.setUser(user);
            r.setTotalPrice(computedAmount != null ? computedAmount : 0.0);
            r.setMetodoPago(PaymentMethod.Tarjeta);
            r.setEstadoRecibo(ReceiptStatus.Pagado);
            if (("reservation".equalsIgnoreCase(type) || "reservation_multi".equalsIgnoreCase(type)) && itemId != null) {
                var mopt2 = machineService.findById(itemId);
                mopt2.ifPresent(r::setMachine);
                int cnt2 = 0;
                cnt2 += (shiftIds != null) ? (int) shiftIds.stream().filter(java.util.Objects::nonNull).count() : 0;
                cnt2 += (startHours != null) ? startHours.size() : 0;
                String fechaTxt2 = (dateStr != null && !dateStr.isBlank()) ? dateStr : java.time.LocalDate.now().toString();
                r.setConcepto("Reserva máquina" + (mopt2.isPresent() ? (" " + mopt2.get().getName()) : "") + " · " + cnt2 + " turno(s) · " + fechaTxt2);
            }
            if ("reservation_multi".equalsIgnoreCase(type) && shiftIds != null) {
                for (Long sidShift : shiftIds) {
                    shiftService.findById(sidShift).ifPresent(r::addShift);
                }
            } else if (shiftId != null) {
                shiftService.findById(shiftId).ifPresent(r::addShift);
            }
            r = receiptService.save(r);
            return performPostPaymentAction(type, itemId, shiftId, startHourStr, dateStr, user, shiftIds, r.getId(), startHours);
        } catch (Exception ex) {
            String err = URLEncoder.encode("Error procesando pago", StandardCharsets.UTF_8);
            if (returnUrl != null && !returnUrl.isBlank()) {
                if (returnUrl.contains("?")) {
                    return "redirect:" + returnUrl + "&paymentError=" + err;
                } else {
                    return "redirect:" + returnUrl + "?paymentError=" + err;
                }
            }
            return "redirect:/?paymentError=" + err;
        }
    }

    @GetMapping("/payment/online/complete")
    public String paymentOnlineComplete(@RequestParam(value = "receiptId") Long receiptId,
            @RequestParam(value = "type", required = false) String type,
            @RequestParam(value = "itemId", required = false) Long itemId,
            @RequestParam(value = "startHour", required = false) String startHourStr,
            @RequestParam(value = "date", required = false) String dateStr,
            @RequestParam(value = "shiftId", required = false) Long shiftId,
            @RequestParam(value = "shiftIds", required = false) String shiftIdsCsv,
            @RequestParam(value = "startHours", required = false) String startHoursCsv,
            @RequestParam(value = "returnUrl", required = false) String returnUrl) {
        var ropt = receiptService.findById(receiptId);
        if (ropt.isPresent()) {
            Receipt rec = ropt.get();
            rec.setEstadoRecibo(ReceiptStatus.Pagado);
            receiptService.save(rec);
            User user = rec.getUser();
            java.util.List<Long> shiftIds = null;
            if (shiftIdsCsv != null && !shiftIdsCsv.isBlank()) {
                shiftIds = java.util.Arrays.stream(shiftIdsCsv.split(","))
                        .map(String::trim)
                        .filter(s -> !s.isEmpty())
                        .map(Long::valueOf)
                        .toList();
            }
            java.util.List<Integer> startHours = null;
            if (startHoursCsv != null && !startHoursCsv.isBlank()) {
                startHours = java.util.Arrays.stream(startHoursCsv.split(","))
                        .map(String::trim)
                        .filter(s -> !s.isEmpty())
                        .map(Integer::valueOf)
                        .toList();
            }
            return performPostPaymentAction(type, itemId, shiftId, startHourStr, dateStr, user, shiftIds, receiptId, startHours);
        }
        String err = URLEncoder.encode("Recibo online no encontrado", StandardCharsets.UTF_8);
        if (returnUrl != null && !returnUrl.isBlank()) {
            if (returnUrl.contains("?")) {
                return "redirect:" + returnUrl + "&paymentError=" + err;
            } else {
                return "redirect:" + returnUrl + "?paymentError=" + err;
            }
        }
        return "redirect:/?paymentError=" + err;
    }

    private String performPostPaymentAction(String type, Long itemId, Long shiftId, String startHourStr, String dateStr, User user, java.util.List<Long> shiftIds, Long receiptId, java.util.List<Integer> startHours) {
        try {
            if ("course".equalsIgnoreCase(type) && itemId != null) {
                var copt = courseService.findById(itemId);
                if (copt.isEmpty()) {
                    return "redirect:/courses?error=" + URLEncoder.encode("Curso no encontrado", StandardCharsets.UTF_8);
                }
                var course = copt.get();
                boolean already = inscriptionService.listAll().stream().anyMatch(i -> i.getCourse() != null && i.getCourse().getId() != null && i.getCourse().getId().equals(course.getId())
                        && i.getUser() != null && i.getUser().getId() != null && i.getUser().getId().equals(user.getId()));
                if (already) {
                    return "redirect:/courses/" + course.getId() + "?error=" + URLEncoder.encode("Ya estás inscrito", StandardCharsets.UTF_8);
                }
                Inscription ins = new Inscription();
                ins.setUser(user);
                ins.setCourse(course);
                ins.setDate(LocalDate.now());
                inscriptionService.save(ins);
                return "redirect:/courses/" + course.getId() + "?success=1";
            } else if (("reservation".equalsIgnoreCase(type) || "reservation_multi".equalsIgnoreCase(type)) && itemId != null) {
                var mopt = machineService.findById(itemId);
                if (mopt.isEmpty()) {
                    return "redirect:/machines?error=" + URLEncoder.encode("Máquina no encontrada", StandardCharsets.UTF_8);
                }
                var machine = mopt.get();
                LocalDate date = LocalDate.now();
                if (dateStr != null && !dateStr.isBlank()) {
                    try {
                        date = LocalDate.parse(dateStr);
                    } catch (Exception ignored) {
                    }
                }
                // Modo múltiple: reservar varios turnos
                if ("reservation_multi".equalsIgnoreCase(type)) {
                    // reservar por ids existentes
                    if (shiftIds != null && !shiftIds.isEmpty()) {
                    for (Long sidShift : shiftIds) {
                        var shOpt = shiftService.findById(sidShift);
                        if (shOpt.isEmpty()) {
                            continue;
                        }
                        Shift s = shOpt.get();
                        if (!s.getMachine().getId().equals(machine.getId())) {
                            continue;
                        }
                        if (bookingService.findByUserAndShift(user, s).isPresent()) {
                            continue;
                        }
                        if (s.getStatus() != ShiftStatus.Disponible && bookingService.findByUserAndShift(user, s).isEmpty()) {
                            continue;
                        }
                        Booking b = new Booking();
                        b.setUser(user);
                        b.setShift(s);
                        b.setFechaReserva(LocalDate.now());
                        bookingService.save(b);
                        if (s.getStatus() == ShiftStatus.Disponible) {
                            s.setStatus(ShiftStatus.Reservado);
                            shiftService.save(s);
                        }
                    }
                    }
                    // crear y reservar por horas seleccionadas
                    if (startHours != null && !startHours.isEmpty()) {
                        date = LocalDate.now();
                        if (dateStr != null && !dateStr.isBlank()) {
                            try { date = LocalDate.parse(dateStr); } catch (Exception ignored) {}
                        }
                        // obtener recibo para asociar los nuevos shifts
                        Receipt recForAssoc = null;
                        if (receiptId != null) {
                            var rOpt = receiptService.findById(receiptId);
                            if (rOpt.isPresent()) recForAssoc = rOpt.get();
                        }
                        for (Integer hourSel : startHours) {
                            if (hourSel == null) continue;
                            java.time.LocalTime start = java.time.LocalTime.of(hourSel, 0);
                            Shift found = null;
                            for (Shift s : shiftService.findByMachineAndDate(machine, date)) {
                                if (s.getStartTime().equals(start)) { found = s; break; }
                            }
                            if (found == null) {
                                Shift ns = new Shift();
                                ns.setMachine(machine);
                                ns.setDate(date);
                                ns.setStartTime(start);
                                ns.setEndTime(start.plusHours(1));
                                ns.setStatus(ShiftStatus.Disponible);
                                found = shiftService.save(ns);
                            }
                            if (recForAssoc != null) {
                                recForAssoc.addShift(found);
                            }
                            if (bookingService.findByUserAndShift(user, found).isPresent()) { continue; }
                            if (found.getStatus() != ShiftStatus.Disponible && bookingService.findByUserAndShift(user, found).isEmpty()) { continue; }
                            Booking b = new Booking();
                            b.setUser(user);
                            b.setShift(found);
                            b.setFechaReserva(LocalDate.now());
                            bookingService.save(b);
                            if (found.getStatus() == ShiftStatus.Disponible) {
                                found.setStatus(ShiftStatus.Reservado);
                                shiftService.save(found);
                            }
                        }
                        if (recForAssoc != null) {
                            receiptService.save(recForAssoc);
                        }
                    }
                    return "redirect:/machines/" + machine.getId() + "/reserve?success=1&date=" + date + (receiptId != null ? "&paymentSuccess=Recibo+" + receiptId : "");
                }
                // Modo único: reservar un turno
                Shift shift = null;
                if (shiftId != null) {
                    var shOpt = shiftService.findById(shiftId);
                    if (shOpt.isPresent()) {
                        shift = shOpt.get();
                    }
                }
                if (shift == null && startHourStr != null) {
                    int hour = -1;
                    try {
                        hour = Integer.parseInt(startHourStr);
                    } catch (NumberFormatException ignored) {
                    }
                    LocalTime start = hour >= 0 ? LocalTime.of(hour, 0) : null;
                    if (start != null) {
                        for (Shift s : shiftService.findByMachineAndDate(machine, date)) {
                            if (s.getStartTime().equals(start)) {
                                shift = s;
                                break;
                            }
                        }
                    }
                }
                if (shift == null) {
                    if (startHourStr != null) {
                        int hour = 9;
                        try {
                            hour = Integer.parseInt(startHourStr);
                        } catch (NumberFormatException ignored) {
                        }
                        LocalTime start = LocalTime.of(hour, 0);
                        Shift ns = new Shift();
                        ns.setMachine(machine);
                        ns.setDate(date);
                        ns.setStartTime(start);
                        ns.setEndTime(start.plusHours(1));
                        ns.setStatus(ShiftStatus.Disponible);
                        shift = shiftService.save(ns);
                    } else {
                        return "redirect:/machines/" + machine.getId() + "/reserve?error=" + URLEncoder.encode("Turno no encontrado", StandardCharsets.UTF_8) + "&date=" + date;
                    }
                }
                if (shift.getStatus() != ShiftStatus.Disponible && bookingService.findByUserAndShift(user, shift).isEmpty()) {
                    return "redirect:/machines/" + machine.getId() + "/reserve?error=" + URLEncoder.encode("Turno no disponible", StandardCharsets.UTF_8) + "&date=" + date;
                }
                if (bookingService.findByUserAndShift(user, shift).isPresent()) {
                    return "redirect:/machines/" + machine.getId() + "/reserve?error=" + URLEncoder.encode("Ya reservaste este turno", StandardCharsets.UTF_8) + "&date=" + date;
                }
                Booking b = new Booking();
                b.setUser(user);
                b.setShift(shift);
                b.setFechaReserva(LocalDate.now());
                bookingService.save(b);
                if (shift.getStatus() == ShiftStatus.Disponible) {
                    shift.setStatus(ShiftStatus.Reservado);
                    shiftService.save(shift);
                }
                return "redirect:/machines/" + machine.getId() + "/reserve?success=1&date=" + date + (receiptId != null ? "&paymentSuccess=Recibo+" + receiptId : "");
            }
            return "redirect:/";
        } catch (Exception ex) {
            return "redirect:/?paymentError=" + URLEncoder.encode("Error creando recurso tras pago", StandardCharsets.UTF_8);
        }
    }
}
