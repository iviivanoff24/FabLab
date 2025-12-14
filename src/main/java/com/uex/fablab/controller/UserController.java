package com.uex.fablab.controller;

import java.net.URI;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.uex.fablab.data.model.BookingStatus;
import com.uex.fablab.data.model.CourseStatus;
import com.uex.fablab.data.model.InscriptionStatus;
import com.uex.fablab.data.model.PaymentMethod;
import com.uex.fablab.data.model.ReceiptStatus;
import com.uex.fablab.data.model.User;
import com.uex.fablab.data.services.BookingService;
import com.uex.fablab.data.services.InscriptionService;
import com.uex.fablab.data.services.ReceiptService;
import com.uex.fablab.data.services.UserService;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

/**
 * Controlador de vistas para usuarios (perfil).
 */
@Controller
public class UserController {

    private final UserService userService;
    private final BookingService bookingService;
    private final InscriptionService inscriptionService;
    private final ReceiptService receiptService;

    public UserController(UserService userService, BookingService bookingService, InscriptionService inscriptionService, ReceiptService receiptService) {
        this.userService = userService;
        this.bookingService = bookingService;
        this.inscriptionService = inscriptionService;
        this.receiptService = receiptService;
    }

    @GetMapping({"/recibos","/recibos.html"})
    public String viewRecibos(HttpSession session, Model model) {
        Object idObj = session.getAttribute("USER_ID");
        if (idObj == null) return "redirect:/login";
        Long id;
        try { if (idObj instanceof Number number) id = number.longValue(); else id = Long.valueOf(idObj.toString()); }
        catch (NumberFormatException e) { session.invalidate(); return "redirect:/login"; }
        Optional<User> u = userService.findById(id);
        if (u.isEmpty()) { session.invalidate(); return "redirect:/login"; }
        User user = u.get();
        // preparar recibos ordenados (más reciente primero)
        List<Map<String, ?>> recibos = user.getReceipts().stream()
                .sorted(Comparator.<com.uex.fablab.data.model.Receipt, java.time.LocalDate>comparing(
                        r -> r.getFechaEmision(), Comparator.nullsLast(java.time.LocalDate::compareTo)).reversed())
                .map(r -> {
                    Map<String, Object> m = new HashMap<>();
                    m.put("id", r.getId());
                    m.put("concepto", r.getConcepto());
                    m.put("fechaEmision", r.getFechaEmision());
                    m.put("totalPrice", r.getTotalPrice());
                    m.put("metodoPago", r.getMetodoPago() != null ? r.getMetodoPago().name() : null);
                    m.put("estado", r.getEstadoRecibo() != null ? r.getEstadoRecibo().name() : null);
                    m.put("hasCourse", r.getCourse() != null);
                    m.put("hasMachine", r.getMachine() != null);
                    return m;
                }).collect(Collectors.toList());
        model.addAttribute("recibos", recibos);
        model.addAttribute("metodosPago", PaymentMethod.values());
        return "user/recibos";
    }

    @GetMapping({"/mis-cursos","/mis-cursos.html"})
    public String viewMisCursos(HttpSession session, Model model) {
        Object idObj = session.getAttribute("USER_ID");
        if (idObj == null) return "redirect:/login";
        Long id;
        try { if (idObj instanceof Number number) id = number.longValue(); else id = Long.valueOf(idObj.toString()); }
        catch (NumberFormatException e) { session.invalidate(); return "redirect:/login"; }
        Optional<User> u = userService.findById(id);
        if (u.isEmpty()) { session.invalidate(); return "redirect:/login"; }
        User user = u.get();
        List<Map<String, ?>> cursos = user.getInscriptions().stream()
                .sorted(Comparator.<com.uex.fablab.data.model.Inscription, java.time.LocalDate>comparing(
                        i -> i.getCourse() != null ? i.getCourse().getStartDate() : null,
                        Comparator.nullsLast(java.time.LocalDate::compareTo)).reversed())
                .map(i -> {
                    var c = i.getCourse();
                    Map<String, Object> m = new HashMap<>();
                    if (c != null) {
                        m.put("id", c.getId());
                        m.put("inscriptionId", i.getId());
                        m.put("inscriptionStatus", i.getEstado() != null ? i.getEstado().name() : null);
                        m.put("name", c.getName());
                        m.put("startDate", c.getStartDate());
                        m.put("capacity", c.getCapacity());
                        m.put("precio", c.getPrecio());
                        m.put("estado", c.getEstado() != null ? c.getEstado().name() : null);
                    }
                    return m;
                }).collect(Collectors.toList());
        model.addAttribute("cursos", cursos);
        model.addAttribute("estadosCurso", CourseStatus.values());
        return "user/mis-cursos";
    }

    @GetMapping({"/mis-reservas","/mis-reservas.html"})
    public String viewMisReservas(HttpSession session, Model model) {
        Object idObj = session.getAttribute("USER_ID");
        if (idObj == null) return "redirect:/login";
        Long id;
        try { if (idObj instanceof Number number) id = number.longValue(); else id = Long.valueOf(idObj.toString()); }
        catch (NumberFormatException e) { session.invalidate(); return "redirect:/login"; }
        Optional<User> u = userService.findById(id);
        if (u.isEmpty()) { session.invalidate(); return "redirect:/login"; }
        User user = u.get();
        List<Map<String, ?>> reservas = user.getBookings().stream()
                .sorted(Comparator.<com.uex.fablab.data.model.Booking, java.time.LocalDate>comparing(
                        b -> b.getShift() != null ? b.getShift().getDate() : null,
                        Comparator.nullsLast(java.time.LocalDate::compareTo)).reversed())
                .map(b -> {
                    Map<String, Object> m = new HashMap<>();
                    m.put("id", b.getId());
                    m.put("maquina", b.getShift() != null && b.getShift().getMachine() != null ? b.getShift().getMachine().getName() : "-");
                    m.put("fecha", b.getShift() != null ? b.getShift().getDate() : null);
                    m.put("hora", b.getShift() != null ? b.getShift().getStartTime() : null);
                    m.put("estadoReserva", b.getEstado() != null ? b.getEstado().name() : null);
                    m.put("shiftStatus", b.getShift() != null ? b.getShift().getStatus() : null);
                    return m;
                }).collect(Collectors.toList());
        model.addAttribute("reservas", reservas);
        model.addAttribute("estadosReserva", BookingStatus.values());
        List<String> maquinas = reservas.stream().map(r -> r.get("maquina") != null ? r.get("maquina").toString() : "").distinct().collect(Collectors.toList());
        model.addAttribute("misMaquinas", maquinas);
        List<String> horas = reservas.stream().map(r -> r.get("hora") != null ? r.get("hora").toString() : "").distinct().sorted().collect(Collectors.toList());
        model.addAttribute("misHoras", horas);
        return "user/mis-reservas";
    }

    @GetMapping({"/profile","/profile.html"})
    public String profile(HttpSession session, Model model) {
        Object idObj = session.getAttribute("USER_ID");
        if (idObj == null) {
            return "redirect:/login";
        }
        Long id;
            try {
                if (idObj instanceof Number number) id = number.longValue(); else id = Long.valueOf(idObj.toString());
            } catch (NumberFormatException e) {
                session.invalidate();
                return "redirect:/login";
            }

        Optional<User> u = userService.findById(id);
        if (u.isEmpty()) {
            session.invalidate();
            return "redirect:/login";
        }
        User user = u.get();
        model.addAttribute("user", user);
            // Recibos -> ordenar por fecha de emisión (más nuevo primero) y mapear a mapas útiles para la plantilla
            final int LIMIT = 3;
                List<Map<String, ?>> recibos = user.getReceipts().stream()
                    .sorted(Comparator.<com.uex.fablab.data.model.Receipt, java.time.LocalDate>comparing(
                        r -> r.getFechaEmision(), Comparator.nullsLast(java.time.LocalDate::compareTo)).reversed())
                    .map(r -> {
                        Map<String, Object> m = new HashMap<>();
                        m.put("id", r.getId());
                        m.put("concepto", r.getConcepto());
                        m.put("fechaEmision", r.getFechaEmision());
                        m.put("totalPrice", r.getTotalPrice());
                        m.put("metodoPago", r.getMetodoPago() != null ? r.getMetodoPago().name() : null);
                        m.put("estado", r.getEstadoRecibo() != null ? r.getEstadoRecibo().name() : null);
                        m.put("hasCourse", r.getCourse() != null);
                        m.put("hasMachine", r.getMachine() != null);
                        return m;
                    }).collect(Collectors.toList());
            model.addAttribute("recibos", recibos);
            List<Map<String, ?>> recibosRecientes = recibos.stream().limit(LIMIT).collect(Collectors.toList());
            model.addAttribute("recibosRecientes", recibosRecientes);
            // Total gastado en recibos (suma segura de nulls)
                double totalRecibos = user.getReceipts().stream()
                    .filter(r -> r.getTotalPrice() != null)
                    .filter(r -> r.getEstadoRecibo() == ReceiptStatus.Pagado)
                    .mapToDouble(r -> r.getTotalPrice())
                    .sum();
            model.addAttribute("totalRecibos", totalRecibos);
            // Cursos: derivamos lista de mapas desde las inscripciones
                List<Map<String, ?>> cursos = user.getInscriptions().stream()
                    .sorted(Comparator.<com.uex.fablab.data.model.Inscription, java.time.LocalDate>comparing(
                        i -> i.getCourse() != null ? i.getCourse().getStartDate() : null,
                        Comparator.nullsLast(java.time.LocalDate::compareTo)).reversed())
                    .map(i -> {
                        var c = i.getCourse();
                        Map<String, Object> m = new HashMap<>();
                        if (c != null) {
                            m.put("id", c.getId());
                            m.put("name", c.getName());
                            m.put("startDate", c.getStartDate());
                            m.put("capacity", c.getCapacity());
                            m.put("precio", c.getPrecio());
                            m.put("estado", c.getEstado() != null ? c.getEstado().name() : null);
                        }
                        return m;
                    }).collect(Collectors.toList());
            model.addAttribute("cursos", cursos);
            List<Map<String, ?>> cursosRecientes = cursos.stream().limit(LIMIT).collect(Collectors.toList());
            model.addAttribute("cursosRecientes", cursosRecientes);
            // Reservas: transformamos cada Booking a un mapa con máquina, fecha, estado e id
                List<Map<String, ?>> reservas = user.getBookings().stream()
                    .sorted(Comparator.<com.uex.fablab.data.model.Booking, java.time.LocalDate>comparing(
                        b -> b.getShift() != null ? b.getShift().getDate() : null,
                        Comparator.nullsLast(java.time.LocalDate::compareTo)).reversed())
                    .map(b -> {
                        Map<String, Object> m = new HashMap<>();
                        m.put("id", b.getId());
                        m.put("maquina", b.getShift() != null && b.getShift().getMachine() != null ? b.getShift().getMachine().getName() : "-");
                        m.put("fecha", b.getShift() != null ? b.getShift().getDate() : null);
                        m.put("hora", b.getShift() != null ? b.getShift().getStartTime() : null);
                        m.put("estadoReserva", b.getEstado() != null ? b.getEstado().name() : null);
                        m.put("shiftStatus", b.getShift() != null ? b.getShift().getStatus() : null);
                        return m;
                    }).collect(Collectors.toList());
            model.addAttribute("reservas", reservas);
            List<Map<String, ?>> reservasRecientes = reservas.stream().limit(LIMIT).collect(Collectors.toList());
            model.addAttribute("reservasRecientes", reservasRecientes);
        return "user/profile";
    }

    // ---- API endpoints (prefixed with /users) ----
    @GetMapping("/users")
    @ResponseBody
    public List<User> all() {
        return userService.listAll();
    }

    @PostMapping("/users")
    @ResponseBody
    public ResponseEntity<User> create(@Valid @RequestBody User user) {
        try {
            User saved = userService.create(user);
            return ResponseEntity.created(URI.create("/users/" + saved.getId())).body(saved);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/users/{id}")
    @ResponseBody
    public ResponseEntity<User> one(@PathVariable Long id) {
        return userService.findById(id).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/users/{id}")
    @ResponseBody
    public ResponseEntity<User> update(@PathVariable Long id, @Valid @RequestBody User input) {
        return userService.update(id, input)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/users/{id}")
    @ResponseBody
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        if (!userService.delete(id)) return ResponseEntity.notFound().build();
        return ResponseEntity.noContent().build();
    }

    /**
     * Formulario para editar perfil (solo teléfono y contraseña).
     */
    @GetMapping({"/profile/edit","/profile/edit.html"})
    public String editProfileForm(HttpSession session, Model model) {
        Object idObj = session.getAttribute("USER_ID");
        if (idObj == null) return "redirect:/login";
        Long id;
        try {
            if (idObj instanceof Number number) id = number.longValue(); else id = Long.valueOf(idObj.toString());
        } catch (NumberFormatException e) {
            session.invalidate();
            return "redirect:/login";
        }
        Optional<User> u = userService.findById(id);
        if (u.isEmpty()) { session.invalidate(); return "redirect:/login"; }
        model.addAttribute("user", u.get());
        return "user/edit-profile";
    }

    /**
     * Procesa cambios del perfil: solo `telefono` y `password` (opcional).
     */
    @PostMapping("/profile/edit")
    public String submitEditProfile(HttpSession session,
                                    @RequestParam(value = "telefono", required = false) String telefono,
                                    @RequestParam(value = "password", required = false) String password,
                                    @RequestParam(value = "passwordConfirm", required = false) String passwordConfirm) {
        Object idObj = session.getAttribute("USER_ID");
        if (idObj == null) return "redirect:/login";
        Long id;
        try {
            if (idObj instanceof Number number) id = number.longValue(); else id = Long.valueOf(idObj.toString());
        } catch (NumberFormatException e) {
            session.invalidate();
            return "redirect:/login";
        }

        if (password != null && !password.isBlank()) {
            if (passwordConfirm == null || !password.equals(passwordConfirm)) {
                return "redirect:/profile/edit?error=pass_mismatch";
            }
        }

        User input = new User();
        if (password != null && !password.isBlank()) input.setPassword(password);
        if (telefono != null) input.setTelefono(telefono);

        userService.update(id, input);

        return "redirect:/profile?updated=1";
    }

    /**
     * Borra la cuenta del usuario autenticado e invalida la sesión.
     */
    @PostMapping("/profile/delete")
    public String deleteProfile(HttpSession session) {
        Object idObj = session.getAttribute("USER_ID");
        if (idObj == null) return "redirect:/login";
        Long id;
        try {
            if (idObj instanceof Number number) id = number.longValue(); else id = Long.valueOf(idObj.toString());
        } catch (NumberFormatException e) {
            session.invalidate();
            return "redirect:/login";
        }

        boolean deleted = userService.delete(id);
        // invalidar sesión en cualquier caso
        session.invalidate();
        if (deleted) {
            return "redirect:/?account_deleted=1";
        } else {
            return "redirect:/profile?error=delete_failed";
        }
    }

    @PostMapping("/user/cancel-receipt/{id}")
    public String cancelReceipt(@PathVariable Long id, HttpSession session) {
        Object userIdObj = session.getAttribute("USER_ID");
        if (userIdObj == null) return "redirect:/login";
        Long userId;
        if (userIdObj instanceof Number number) userId = number.longValue(); else userId = Long.valueOf(userIdObj.toString());

        receiptService.findById(id).ifPresent(r -> {
            if (r.getUser().getId().equals(userId)) {
                r.setEstadoRecibo(ReceiptStatus.Anulado);
                receiptService.save(r);
            }
        });
        return "redirect:/recibos";
    }

    @PostMapping("/user/cancel-course/{id}")
    public String cancelCourse(@PathVariable Long id, HttpSession session) {
        Object userIdObj = session.getAttribute("USER_ID");
        if (userIdObj == null) return "redirect:/login";
        Long userId;
        if (userIdObj instanceof Number number) userId = number.longValue(); else userId = Long.valueOf(userIdObj.toString());

        inscriptionService.findById(id).ifPresent(i -> {
             if (i.getUser().getId().equals(userId)) {
                 i.setEstado(InscriptionStatus.Cancelado);
                 inscriptionService.save(i);
             }
        });
        return "redirect:/mis-cursos";
    }

    @PostMapping("/user/cancel-booking/{id}")
    public String cancelBooking(@PathVariable Long id, HttpSession session) {
        Object userIdObj = session.getAttribute("USER_ID");
        if (userIdObj == null) return "redirect:/login";
        Long userId;
        if (userIdObj instanceof Number number) userId = number.longValue(); else userId = Long.valueOf(userIdObj.toString());

        bookingService.findById(id).ifPresent(b -> {
            if (b.getUser().getId().equals(userId)) {
                bookingService.delete(b.getId());
            }
        });
        return "redirect:/mis-reservas";
    }
}
