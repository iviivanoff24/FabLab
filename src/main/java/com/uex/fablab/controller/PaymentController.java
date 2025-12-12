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
import com.uex.fablab.data.model.Cart;
import com.uex.fablab.data.model.CartItem;
import com.uex.fablab.data.model.Inscription;
import com.uex.fablab.data.model.PaymentMethod;
import com.uex.fablab.data.model.Receipt;
import com.uex.fablab.data.model.ReceiptProduct;
import com.uex.fablab.data.model.ReceiptProductKey;
import com.uex.fablab.data.model.ReceiptStatus;
import com.uex.fablab.data.model.Shift;
import com.uex.fablab.data.model.ShiftStatus;
import com.uex.fablab.data.model.SubProduct;
import com.uex.fablab.data.model.User;
import com.uex.fablab.data.services.BookingService;
import com.uex.fablab.data.services.CartService;
import com.uex.fablab.data.services.CourseService;
import com.uex.fablab.data.services.InscriptionService;
import com.uex.fablab.data.services.MachineService;
import com.uex.fablab.data.services.ProductService;
import com.uex.fablab.data.services.ReceiptService;
import com.uex.fablab.data.services.ShiftService;
import com.uex.fablab.data.services.UserService;

import jakarta.servlet.http.HttpSession;

/**
 * Controlador para la página y proceso de pago.
 *
 * <p>Proporciona una vista para iniciar pagos y endpoints que simulan el
 * flujo de pago. Está pensado como ejemplo/poC: en un entorno real debería
 * integrarse con un proveedor de pagos (Stripe, PayPal, etc.).</p>
 *
 * <p>Responsabilidades:
 * <ul>
 *   <li>Mostrar la página de pago con datos resumidos del elemento a pagar.</li>
 *   <li>Procesar la petición de pago (simulada) y crear recibos.</li>
 *   <li>Tras marcar un recibo como pagado, realizar la acción posterior
 *       correspondiente (crear inscripción o reservas).</li>
 * </ul>
 * </p>
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
    private final CartService cartService;
    private final ProductService productService;

    public PaymentController(UserService userService, BookingService bookingService, ShiftService shiftService,
            MachineService machineService, CourseService courseService, InscriptionService inscriptionService, ReceiptService receiptService,
            CartService cartService, ProductService productService) {
        this.userService = userService;
        this.bookingService = bookingService;
        this.shiftService = shiftService;
        this.machineService = machineService;
        this.courseService = courseService;
        this.inscriptionService = inscriptionService;
        this.receiptService = receiptService;
        this.cartService = cartService;
        this.productService = productService;
    }

    /**
        * Muestra la página de pago.
        *
        * <p>Prepara los datos necesarios para la vista de pago (tipo de pago,
        * importe, elementos/turnos seleccionados, etc.). Requiere que el usuario
        * esté autenticado (se comprueba el atributo {@code USER_ID} en la sesión).
        * Si no hay usuario redirige a login con un mensaje.</p>
        *
        * @param type tipo de compra ("course","reservation","reservation_multi",...)
        * @param itemId id del recurso (curso o máquina) relacionado con el pago
        * @param itemName nombre legible del elemento a pagar (opcional)
        * @param amount importe mostrado en la UI (opcional; se recomputa cuando procede)
        * @param returnUrl URL de retorno tras el pago (opcional)
        * @param date fecha asociada a la reserva (opcional)
        * @param startHour hora asociada a la reserva (opcional)
        * @param shiftId id de turno (opcional)
        * @param shiftIds lista de ids de turnos (opcional, para multi-reserva)
        * @param startHours lista de horas seleccionadas (opcional, para multi-reserva)
        * @param session sesión HTTP que contiene datos de usuario
        * @param model modelo Thymeleaf donde se añaden atributos para la vista
        * @return nombre de la vista {@code user/payment} o redirección a login
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
            @RequestParam(value = "startDates", required = false) java.util.List<String> startDates,
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
                int sids = (shiftIds != null) ? (int) shiftIds.stream().filter(java.util.Objects::nonNull).count() : 0;
                int shours = (startHours != null) ? startHours.size() : 0;
                selectedCount = sids + shours;
                if (selectedCount == 0) {
                    selectedCount = 1; // fallback visual

                                }if (amount == null || amount.isBlank()) {
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
        model.addAttribute("startDates", startDates);
        // Cargar información de turnos seleccionados para mostrar en la página de pago
        java.util.List<com.uex.fablab.data.model.Shift> selectedShifts = java.util.Collections.emptyList();
        if (shiftIds != null && !shiftIds.isEmpty()) {
            selectedShifts = new java.util.ArrayList<>();
            for (Long sidShift : shiftIds) {
                if (sidShift == null) {
                    continue;
                }
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

        // Prepare unified view items
        java.util.List<java.util.Map<String, Object>> paymentItems = new java.util.ArrayList<>();
        if (selectedShifts != null) {
            for (com.uex.fablab.data.model.Shift s : selectedShifts) {
                java.util.Map<String, Object> item = new java.util.HashMap<>();
                item.put("date", s.getDate().toString()); // ISO format
                item.put("time", s.getStartTime() + " - " + s.getEndTime());
                item.put("type", "Existente");
                paymentItems.add(item);
            }
        }
        if (startHours != null) {
            for (int i = 0; i < startHours.size(); i++) {
                Integer h = startHours.get(i);
                String d = (startDates != null && i < startDates.size()) ? startDates.get(i) : date;
                java.util.Map<String, Object> item = new java.util.HashMap<>();
                item.put("date", d);
                item.put("time", String.format("%02d:00 - %02d:00", h, h + 1));
                item.put("type", "Nuevo");
                paymentItems.add(item);
            }
        }
        model.addAttribute("paymentItems", paymentItems);

        return "user/payment";
    }

    private String buildReservationConcept(com.uex.fablab.data.model.Machine machine, int count, String dateStr) {
        /** Construye el texto de concepto para una reserva. */
        String concepto = "Reserva máquina " + (machine != null ? machine.getName() : "N/D");
        if (count > 1) {
            concepto += " · " + count + " turno(s)";
        }
        if (dateStr != null && !dateStr.isBlank()) {
            concepto += " · " + dateStr;
        }
        return concepto;
    }

    /**
     * Procesa la petición de pago (simulada) y crea el recibo asociado.
     *
     * <p>El método soporta diferentes {@code type}:
     * <ul>
     *   <li>{@code course}: pago de una inscripción a curso.</li>
     *   <li>{@code reservation}: pago de una única reserva.</li>
     *   <li>{@code reservation_multi}: pago de varias reservas/horas.</li>
     * </ul></p>
     *
     * <p>Dependiendo del método de pago (efectivo, online, tarjeta) el recibo
     * se marca como pagado o pendiente y luego se invoca
     * {@link #performPostPaymentAction} para crear la inscripción/reserva si
     * procede.</p>
     *
     * @param type tipo de pago (ver arriba)
     * @param itemId id del recurso relacionado (curso o máquina)
     * @param itemName nombre legible del recurso (opcional)
     * @param amount importe recibido desde la UI (opcional)
     * @param paymentMethod método de pago (Tarjeta, Efectivo, Online)
     * @param onlineProvider proveedor online (opcional)
     * @param returnUrl URL de retorno tras pago online (opcional)
     * @param shiftId id de turno (opcional)
     * @param shiftIds lista de ids para multi-reserva (opcional)
     * @param startHours lista de horas seleccionadas para crear turnos (opcional)
     * @param startHourStr hora seleccionada (opcional)
     * @param dateStr fecha asociada (opcional)
     * @param session sesión HTTP con {@code USER_ID}
     * @return redirección a la vista correspondiente según el resultado
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
            @RequestParam(value = "startDates", required = false) java.util.List<String> startDates,
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
            if ("cart".equalsIgnoreCase(type)) {
                Cart cart = cartService.getCartByUser(user);
                if (cart != null && !cart.getItems().isEmpty()) {
                    // Validate stock
                    for (CartItem item : cart.getItems()) {
                        if (item.getQuantity() > item.getSubProduct().getStock()) {
                            return "redirect:/cart?error=stock";
                        }
                    }
                    computedAmount = cart.getItems().stream()
                        .mapToDouble(item -> item.getSubProduct().getPrice() * item.getQuantity())
                        .sum();
                } else {
                    return "redirect:/cart";
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
            // amount = String.format(java.util.Locale.US, "%.2f", computedAmount != null ? computedAmount : 0.0);

            String pm = paymentMethod != null ? paymentMethod : "Tarjeta";
            // Bloquear creación de un nuevo recibo para el mismo curso si ya existe
            // un recibo pendiente (efectivo/online). Evita múltiples recibos pendientes
            // por la misma inscripción.
            if ("course".equalsIgnoreCase(type) && itemId != null) {
                final String[] courseName = {""};
                var coptChk = courseService.findById(itemId);
                if (coptChk.isPresent() && coptChk.get().getName() != null) courseName[0] = coptChk.get().getName();
                boolean hasPending = user.getReceipts().stream().anyMatch(r ->
                    r.getEstadoRecibo() == ReceiptStatus.Pendiente && (
                        (r.getCourse() != null && r.getCourse().getId() != null && r.getCourse().getId().equals(itemId))
                        || (r.getConcepto() != null && !r.getConcepto().isBlank() && !courseName[0].isBlank() && r.getConcepto().contains(courseName[0]))
                    )
                );
                if (hasPending) {
                    return "redirect:/courses/" + itemId + "?error=" + URLEncoder.encode("Tienes un pago pendiente para este curso. Espera a que se confirme o se anule.", StandardCharsets.UTF_8);
                }
            }
            if ("Efectivo".equalsIgnoreCase(pm)) {
                // cash: assume paid, create receipt and perform action
                Receipt r = new Receipt();
                r.setUser(user);
                r.setTotalPrice(computedAmount != null ? computedAmount : 0.0);
                r.setMetodoPago(PaymentMethod.Efectivo);
                // For cash payments, the receipt remains pending until confirmation
                r.setEstadoRecibo(ReceiptStatus.Pendiente);
                // Si es pago de curso, asociar curso y concepto
                if ("course".equalsIgnoreCase(type) && itemId != null) {
                    var copt2 = courseService.findById(itemId);
                    if (copt2.isPresent()) {
                        var c = copt2.get();
                        r.setCourse(c);
                        r.setConcepto("Inscripción curso " + c.getName());
                    }
                }
                // Set machine and concepto for reservations
                if (("reservation".equalsIgnoreCase(type) || "reservation_multi".equalsIgnoreCase(type)) && itemId != null) {
                    var mopt2 = machineService.findById(itemId);
                    mopt2.ifPresent(r::setMachine);
                    int cnt2 = 0;
                    cnt2 += (shiftIds != null) ? (int) shiftIds.stream().filter(java.util.Objects::nonNull).count() : 0;
                    cnt2 += (startHours != null) ? startHours.size() : 0;
                    String fechaTxt2 = (dateStr != null && !dateStr.isBlank()) ? dateStr : java.time.LocalDate.now().toString();
                    r.setConcepto(buildReservationConcept(mopt2.orElse(null), cnt2, fechaTxt2));
                }
                if ("cart".equalsIgnoreCase(type)) {
                    r.setConcepto("Compra de productos");
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
                return performPostPaymentAction(type, itemId, shiftId, startHourStr, dateStr, user, shiftIds, r.getId(), startHours, startDates);
            }
            if ("Online".equalsIgnoreCase(pm)) {
                // online: create pending receipt and redirect to simulated completion
                Receipt r = new Receipt();
                r.setUser(user);
                r.setTotalPrice(computedAmount != null ? computedAmount : 0.0);
                r.setMetodoPago(PaymentMethod.Online);
                r.setEstadoRecibo(ReceiptStatus.Pendiente);
                // Si es pago de curso, asociar curso y concepto
                if ("course".equalsIgnoreCase(type) && itemId != null) {
                    var copt2 = courseService.findById(itemId);
                    if (copt2.isPresent()) {
                        var c = copt2.get();
                        r.setCourse(c);
                        r.setConcepto("Inscripción curso " + c.getName());
                    }
                }
                if (("reservation".equalsIgnoreCase(type) || "reservation_multi".equalsIgnoreCase(type)) && itemId != null) {
                    var mopt2 = machineService.findById(itemId);
                    mopt2.ifPresent(r::setMachine);
                    int cnt2 = 0;
                    cnt2 += (shiftIds != null) ? (int) shiftIds.stream().filter(java.util.Objects::nonNull).count() : 0;
                    cnt2 += (startHours != null) ? startHours.size() : 0;
                    String fechaTxt2 = (dateStr != null && !dateStr.isBlank()) ? dateStr : java.time.LocalDate.now().toString();
                    r.setConcepto(buildReservationConcept(mopt2.orElse(null), cnt2, fechaTxt2));
                }
                if ("cart".equalsIgnoreCase(type)) {
                    r.setConcepto("Compra de productos");
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
            // Si es pago de curso, asociar curso y concepto
            if ("course".equalsIgnoreCase(type) && itemId != null) {
                var copt2 = courseService.findById(itemId);
                if (copt2.isPresent()) {
                    var c = copt2.get();
                    r.setCourse(c);
                    r.setConcepto("Inscripción curso " + c.getName());
                }
            }
            if (("reservation".equalsIgnoreCase(type) || "reservation_multi".equalsIgnoreCase(type)) && itemId != null) {
                var mopt2 = machineService.findById(itemId);
                mopt2.ifPresent(r::setMachine);
                int cnt2 = 0;
                cnt2 += (shiftIds != null) ? (int) shiftIds.stream().filter(java.util.Objects::nonNull).count() : 0;
                cnt2 += (startHours != null) ? startHours.size() : 0;
                String fechaTxt2 = (dateStr != null && !dateStr.isBlank()) ? dateStr : java.time.LocalDate.now().toString();
                r.setConcepto(buildReservationConcept(mopt2.orElse(null), cnt2, fechaTxt2));
            }
            if ("reservation_multi".equalsIgnoreCase(type) && shiftIds != null) {
                for (Long sidShift : shiftIds) {
                    shiftService.findById(sidShift).ifPresent(r::addShift);
                }
            } else if (shiftId != null) {
                shiftService.findById(shiftId).ifPresent(r::addShift);
            }
            r = receiptService.save(r);
            return performPostPaymentAction(type, itemId, shiftId, startHourStr, dateStr, user, shiftIds, r.getId(), startHours, startDates);
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
        /*
         * Callback simulado tras pago online: marca el recibo como pagado y
         * delega en performPostPaymentAction para completar la operación.
         * Parámetros CSV son convertidos a listas si están presentes.
         */
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
            return performPostPaymentAction(type, itemId, shiftId, startHourStr, dateStr, user, shiftIds, receiptId, startHours, null);
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

    private String performPostPaymentAction(String type, Long itemId, Long shiftId, String startHourStr, String dateStr, User user, java.util.List<Long> shiftIds, Long receiptId, java.util.List<Integer> startHours, java.util.List<String> startDates) {
        /**
         * Ejecuta la acción posterior al pago: crear inscripción o reservas.
         *
         * <p>Si el recibo está marcado como pagado realiza las operaciones
         * correspondientes de forma inmediata; si el recibo está pendiente
         * (por ejemplo, pago en efectivo), devuelve una redirección indicando
         * el estado pendiente para que un administrador lo confirme.</p>
         */
        try {
            boolean receiptPaid = true;
            if (receiptId != null) {
                var ropt = receiptService.findById(receiptId);
                if (ropt.isPresent()) {
                    receiptPaid = ropt.get().getEstadoRecibo() == ReceiptStatus.Pagado;
                }
            }
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
                if (receiptPaid) {
                    Inscription ins = new Inscription();
                    ins.setUser(user);
                    ins.setCourse(course);
                    ins.setDate(LocalDate.now());
                    inscriptionService.save(ins);
                    return "redirect:/courses/" + course.getId() + "?success=1";
                } else {
                    // Receipt pending: do not create inscription yet, admin will confirm later
                    return "redirect:/courses/" + course.getId() + "?pending=1";
                }
            } else if ("cart".equalsIgnoreCase(type)) {
                Cart cart = cartService.getCartByUser(user);
                if (cart != null && !cart.getItems().isEmpty()) {
                    Receipt receipt = receiptService.findById(receiptId).orElse(null);
                    if (receipt != null) {
                        // Limpiar la colección existente en lugar de reemplazarla para evitar error de orphanRemoval
                        receipt.getReceiptProducts().clear();
                        
                        for (CartItem item : cart.getItems()) {
                            SubProduct subProduct = item.getSubProduct();
                            // Update stock
                            subProduct.setStock(subProduct.getStock() - item.getQuantity());
                            productService.saveSubProduct(subProduct);
                            
                            // Create ReceiptProduct
                            ReceiptProduct rp = new ReceiptProduct();
                            ReceiptProductKey key = new ReceiptProductKey();
                            key.setReceiptId(receipt.getId());
                            key.setSubProductId(subProduct.getId());
                            rp.setId(key);
                            rp.setReceipt(receipt);
                            rp.setSubProduct(subProduct);
                            rp.setQuantity(item.getQuantity());
                            rp.setUnitPrice(subProduct.getPrice());
                            
                            // Añadir a la colección existente
                            receipt.getReceiptProducts().add(rp);
                        }
                        receipt.setConcepto("Compra de productos");
                        receiptService.save(receipt);
                        cartService.clearCart(user);
                    }
                }
                return "redirect:/recibos";
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
                            if (receiptPaid) {
                                if (s.getStatus() == ShiftStatus.Disponible) {
                                    s.setStatus(ShiftStatus.Reservado);
                                    shiftService.save(s);
                                }
                            }
                        }
                    }
                    // crear y reservar por horas seleccionadas
                    if (startHours != null && !startHours.isEmpty()) {
                        // obtener recibo para asociar los nuevos shifts
                        Receipt recForAssoc = null;
                        if (receiptId != null) {
                            var rOpt = receiptService.findById(receiptId);
                            if (rOpt.isPresent()) {
                                recForAssoc = rOpt.get();
                            }
                        }
                        for (int i = 0; i < startHours.size(); i++) {
                            Integer hourSel = startHours.get(i);
                            if (hourSel == null) continue;
                            
                            LocalDate targetDate = LocalDate.now();
                            // Use specific date if available, otherwise fallback to global dateStr
                            if (startDates != null && i < startDates.size() && startDates.get(i) != null && !startDates.get(i).isBlank()) {
                                try { targetDate = LocalDate.parse(startDates.get(i)); } catch (Exception ignored) {}
                            } else if (dateStr != null && !dateStr.isBlank()) {
                                try { targetDate = LocalDate.parse(dateStr); } catch (Exception ignored) {}
                            }

                            java.time.LocalTime start = java.time.LocalTime.of(hourSel, 0);
                            Shift found = null;
                            for (Shift s : shiftService.findByMachineAndDate(machine, targetDate)) {
                                if (s.getStartTime().equals(start)) {
                                    found = s;
                                    break;
                                }
                            }
                            if (found == null) {
                                Shift ns = new Shift();
                                ns.setMachine(machine);
                                ns.setDate(targetDate);
                                ns.setStartTime(start);
                                ns.setEndTime(start.plusHours(1));
                                ns.setStatus(ShiftStatus.Disponible);
                                found = shiftService.save(ns);
                            }
                            if (recForAssoc != null) {
                                recForAssoc.addShift(found);
                            }
                            if (bookingService.findByUserAndShift(user, found).isPresent()) {
                                continue;
                            }
                            if (found.getStatus() != ShiftStatus.Disponible && bookingService.findByUserAndShift(user, found).isEmpty()) {
                                continue;
                            }
                            Booking b = new Booking();
                            b.setUser(user);
                            b.setShift(found);
                            b.setFechaReserva(LocalDate.now());
                            bookingService.save(b);
                            if (receiptPaid) {
                                if (found.getStatus() == ShiftStatus.Disponible) {
                                    found.setStatus(ShiftStatus.Reservado);
                                    shiftService.save(found);
                                }
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
                if (receiptPaid) {
                    if (shift.getStatus() == ShiftStatus.Disponible) {
                        shift.setStatus(ShiftStatus.Reservado);
                        shiftService.save(shift);
                    }
                }
                return "redirect:/machines/" + machine.getId() + "/reserve?success=1&date=" + date + (receiptId != null ? "&paymentSuccess=Recibo+" + receiptId : "");
            }
            return "redirect:/";
        } catch (Exception ex) {
            return "redirect:/?paymentError=" + URLEncoder.encode("Error creando recurso tras pago", StandardCharsets.UTF_8);
        }
    }

    @GetMapping("/checkout")
    public String checkoutPage(HttpSession session, Model model) {
        Long userId = (Long) session.getAttribute("USER_ID");
        if (userId == null) return "redirect:/login";

        User user = userService.findById(userId).orElse(null);
        if (user == null) return "redirect:/login";

        Cart cart = cartService.getCartByUser(user);
        if (cart.getItems().isEmpty()) {
            return "redirect:/cart";
        }

        // Calculate total
        double total = cart.getItems().stream()
                .mapToDouble(item -> item.getSubProduct().getPrice() * item.getQuantity())
                .sum();

        model.addAttribute("cart", cart);
        model.addAttribute("amount", String.format(java.util.Locale.US, "%.2f", total));
        model.addAttribute("type", "cart");
        model.addAttribute("returnUrl", "/cart");
        
        return "user/payment";
    }
}
