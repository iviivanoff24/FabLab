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

import com.uex.fablab.data.model.User;
import com.uex.fablab.data.model.Receipt;
import com.uex.fablab.data.model.PaymentMethod;
import com.uex.fablab.data.model.ReceiptStatus;
import com.uex.fablab.data.model.Booking;
import com.uex.fablab.data.model.Inscription;
import com.uex.fablab.data.model.Shift;
import com.uex.fablab.data.model.ShiftStatus;
import com.uex.fablab.data.services.BookingService;
import com.uex.fablab.data.services.ReceiptService;
import com.uex.fablab.data.services.CourseService;
import com.uex.fablab.data.services.InscriptionService;
import com.uex.fablab.data.services.MachineService;
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
            HttpSession session,
            Model model) {
        Object sid = session.getAttribute("USER_ID");
        if (!(sid instanceof Long)) {
            return "redirect:/login?error=" + URLEncoder.encode("Necesitas iniciar sesión para pagar", StandardCharsets.UTF_8);
        }
        model.addAttribute("type", type);
        model.addAttribute("itemId", itemId);
        model.addAttribute("itemName", itemName);
        model.addAttribute("amount", amount);
        model.addAttribute("returnUrl", returnUrl);
        model.addAttribute("date", date);
        model.addAttribute("startHour", startHour);
        model.addAttribute("shiftId", shiftId);
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
            try {
                if (amount != null && !amount.isBlank()) {
                    computedAmount = Double.valueOf(amount);
            
                }} catch (NumberFormatException ignore) {
                computedAmount = null;
            }
            if ("course".equalsIgnoreCase(type) && itemId != null) {
                var copt = courseService.findById(itemId);
                if (copt.isPresent()) {
                    computedAmount = copt.get().getPrecio();
                }
            }
            if ("reservation".equalsIgnoreCase(type) && itemId != null) {
                var mopt = machineService.findById(itemId);
                if (mopt.isPresent() && mopt.get().getHourlyPrice() != null) {
                    computedAmount = mopt.get().getHourlyPrice().doubleValue();
                }
            }
            if (computedAmount != null) {
                amount = String.format("%.2f", computedAmount);
            }

            String pm = paymentMethod != null ? paymentMethod : "Tarjeta";
            if ("Efectivo".equalsIgnoreCase(pm)) {
                // cash: assume paid, create receipt and perform action
                Receipt r = new Receipt();
                r.setUser(user);
                try {
                    r.setTotalPrice(Double.valueOf(amount));
                } catch (NumberFormatException ex) {
                    r.setTotalPrice(0.0);
                }
                r.setMetodoPago(PaymentMethod.Efectivo);
                r.setEstadoRecibo(ReceiptStatus.Pagado);
                receiptService.save(r);
                return performPostPaymentAction(type, itemId, shiftId, startHourStr, dateStr, user);
            }
            if ("Online".equalsIgnoreCase(pm)) {
                // online: create pending receipt and redirect to simulated completion
                Receipt r = new Receipt();
                r.setUser(user);
                try {
                    r.setTotalPrice(Double.valueOf(amount));
                } catch (NumberFormatException ex) {
                    r.setTotalPrice(0.0);
                }
                r.setMetodoPago(PaymentMethod.Online);
                r.setEstadoRecibo(ReceiptStatus.Pendiente);
                r = receiptService.save(r);
                String redirect = "/payment/online/complete?receiptId=" + r.getId()
                        + "&type=" + (type != null ? type : "")
                        + "&itemId=" + (itemId != null ? itemId : "")
                        + "&startHour=" + (startHourStr != null ? startHourStr : "")
                        + "&date=" + (dateStr != null ? dateStr : "")
                        + "&shiftId=" + (shiftId != null ? shiftId : "")
                        + "&returnUrl=" + (returnUrl != null ? URLEncoder.encode(returnUrl, StandardCharsets.UTF_8) : "");
                return "redirect:" + redirect;
            }
            // Tarjeta (default): simulate card payment, create paid receipt and perform action
            Receipt r = new Receipt();
            r.setUser(user);
            try {
                r.setTotalPrice(Double.valueOf(amount));
            } catch (NumberFormatException ex) {
                r.setTotalPrice(0.0);
            }
            r.setMetodoPago(PaymentMethod.Tarjeta);
            r.setEstadoRecibo(ReceiptStatus.Pagado);
            receiptService.save(r);
            return performPostPaymentAction(type, itemId, shiftId, startHourStr, dateStr, user);
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
            @RequestParam(value = "returnUrl", required = false) String returnUrl) {
        var ropt = receiptService.findById(receiptId);
        if (ropt.isPresent()) {
            Receipt rec = ropt.get();
            rec.setEstadoRecibo(ReceiptStatus.Pagado);
            receiptService.save(rec);
            User user = rec.getUser();
            return performPostPaymentAction(type, itemId, shiftId, startHourStr, dateStr, user);
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

    private String performPostPaymentAction(String type, Long itemId, Long shiftId, String startHourStr, String dateStr, User user) {
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
            } else if ("reservation".equalsIgnoreCase(type) && itemId != null) {
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
                return "redirect:/machines/" + machine.getId() + "/reserve?success=1&date=" + date;
            }
            return "redirect:/";
        } catch (Exception ex) {
            return "redirect:/?paymentError=" + URLEncoder.encode("Error creando recurso tras pago", StandardCharsets.UTF_8);
        }
    }
}
