package com.uex.fablab.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.uex.fablab.data.model.User;
import com.uex.fablab.data.services.AdminService;
import com.uex.fablab.data.services.BookingService;
import com.uex.fablab.data.services.CourseService;
import com.uex.fablab.data.services.InscriptionService;
import com.uex.fablab.data.services.MachineService;
import com.uex.fablab.data.services.ReceiptService;
import com.uex.fablab.data.services.ShiftService;
import com.uex.fablab.data.services.UserService;

/**
 * Controlador para las vistas del panel de administración.
 *
 * <p>Este controlador agrupa las rutas necesarias para la gestión administrativa
 * del sistema: listado y modificación de usuarios, gestión de máquinas,
 * reservas, turnos, cursos, inscripciones y recibos. Proporciona enlaces a
 * servicios de dominio (AdminService, MachineService, BookingService, etc.) y
 * centraliza la lógica de flujo de operaciones que afectan a varias entidades
 * (por ejemplo, confirmar reservas al marcar un recibo como pagado).</p>
 *
 * <p>Notas:
 * - Las rutas relativas comienzan por <code>/admin/...</code>.
 * - Se delega la lógica de negocio en los servicios inyectados.
 * - Los métodos de controlador devuelven nombres de vistas o redirecciones.</p>
 */
@Controller
public class AdminController {

    private final AdminService adminService;
    private final MachineService machineService;
    private final BookingService bookingService;
    private final ShiftService shiftService;
    private final CourseService courseService;
    private final InscriptionService inscriptionService;
    private final ReceiptService receiptService;
    private final UserService userService;
    private final com.uex.fablab.data.services.ProductService productService;

    public AdminController(AdminService adminService,
                           MachineService machineService,
                           BookingService bookingService,
                           ShiftService shiftService,
                           CourseService courseService,
                           InscriptionService inscriptionService,
                           ReceiptService receiptService,
                           UserService userService,
                           com.uex.fablab.data.services.ProductService productService) {
        this.adminService = adminService;
        this.machineService = machineService;
        this.bookingService = bookingService;
        this.shiftService = shiftService;
        this.courseService = courseService;
        this.inscriptionService = inscriptionService;
        this.receiptService = receiptService;
        this.userService = userService;
        this.productService = productService;
    }

    @org.springframework.web.bind.annotation.GetMapping("/admin/modify-user")
    public String modifyUserPage(@org.springframework.web.bind.annotation.RequestParam("id") Long id, org.springframework.ui.Model model) {
        var opt = userService.findById(id);
        if (opt.isEmpty()) {
            return "redirect:/admin/admin?error=" + java.net.URLEncoder.encode("Usuario no encontrado", java.nio.charset.StandardCharsets.UTF_8);
        }
        model.addAttribute("user", opt.get());
        return "admin/modify-user";
    }

    @org.springframework.web.bind.annotation.PostMapping("/admin/users/{id}")
    /**
     * Actualiza los datos de un usuario a partir de un formulario.
     *
     * <p>Se permite actualizar nombre, email, teléfono y (opcionalmente)
     * contraseña. Si se marca el parámetro <code>admin</code>, se delega al
     * servicio <code>AdminService</code> para cambiar el rol del usuario.</p>
     *
     * @param id              identificador del usuario a actualizar
     * @param name            nuevo nombre (opcional)
     * @param email           nuevo correo (opcional)
     * @param newPassword     nueva contraseña (opcional)
     * @param confirmPassword confirmación de la contraseña (opcional)
     * @param telefono        teléfono (opcional)
     * @param adminFlag       si true, asignar rol de administrador
     * @return redirección a la página de administración o a la edición con error
     */
    public String updateUserFromForm(@org.springframework.web.bind.annotation.PathVariable("id") Long id,
                                     @org.springframework.web.bind.annotation.RequestParam(value = "name", required = false) String name,
                                     @org.springframework.web.bind.annotation.RequestParam(value = "email", required = false) String email,
                                     @org.springframework.web.bind.annotation.RequestParam(value = "newPassword", required = false) String newPassword,
                                     @org.springframework.web.bind.annotation.RequestParam(value = "confirmPassword", required = false) String confirmPassword,
                                     @org.springframework.web.bind.annotation.RequestParam(value = "telefono", required = false) String telefono,
                                     @org.springframework.web.bind.annotation.RequestParam(value = "admin", required = false) Boolean adminFlag) {
        try {
            if (newPassword != null && !newPassword.isBlank()) {
                if (!newPassword.equals(confirmPassword)) {
                    return "redirect:/admin/modify-user?id=" + id + "&error=" + java.net.URLEncoder.encode("Las contraseñas no coinciden", java.nio.charset.StandardCharsets.UTF_8);
                }
            }
            User input = new User();
            input.setName(name);
            input.setEmail(email);
            if (newPassword != null && !newPassword.isBlank()) input.setPassword(newPassword);
            input.setTelefono(telefono);
            userService.update(id, input);
            if (adminFlag != null) {
                adminService.changeRole(id, adminFlag);
            }
        } catch (Exception ex) {
            return "redirect:/admin/modify-user?id=" + id + "&error=" + java.net.URLEncoder.encode("Error actualizando usuario", java.nio.charset.StandardCharsets.UTF_8);
        }
        return "redirect:/admin/admin";
    }

    @GetMapping("/admin/admin")
    /**
     * Muestra el panel de administración con listados (usuarios, máquinas,
     * reservas, turnos, cursos, inscripciones y recibos).
     *
     * <p>Admite parámetros de ordenación y dirección para varios listados.</p>
     *
     * @param model          modelo de la vista
     * @param sortUsers      campo para ordenar usuarios (no obligatorio)
     * @param dirUsers       dirección del orden (asc|desc)
     * @param sortBookings   campo de orden para reservas
     * @param dirBookings    dirección para reservas
     * @param sortShifts     campo de orden para turnos
     * @param dirShifts      dirección para turnos
     * @param sortInscriptions campo de orden para inscripciones
     * @param dirInscriptions dirección para inscripciones
     * @param sortReceipts   campo de orden para recibos
     * @param dirReceipts    dirección para recibos
     * @return nombre de la vista del panel de administración
     */
    public String adminPanel(Model model,
                             @org.springframework.web.bind.annotation.RequestParam(value = "sortUsers", required = false) String sortUsers,
                             @org.springframework.web.bind.annotation.RequestParam(value = "dirUsers", required = false) String dirUsers,
                             @org.springframework.web.bind.annotation.RequestParam(value = "sortBookings", required = false) String sortBookings,
                             @org.springframework.web.bind.annotation.RequestParam(value = "dirBookings", required = false) String dirBookings,
                             @org.springframework.web.bind.annotation.RequestParam(value = "sortShifts", required = false) String sortShifts,
                             @org.springframework.web.bind.annotation.RequestParam(value = "dirShifts", required = false) String dirShifts,
                             @org.springframework.web.bind.annotation.RequestParam(value = "sortInscriptions", required = false) String sortInscriptions,
                             @org.springframework.web.bind.annotation.RequestParam(value = "dirInscriptions", required = false) String dirInscriptions,
                             @org.springframework.web.bind.annotation.RequestParam(value = "sortReceipts", required = false) String sortReceipts,
                             @org.springframework.web.bind.annotation.RequestParam(value = "dirReceipts", required = false) String dirReceipts) {

        // Users: default order by fechaRegistro desc (nuevas -> antiguas)
        var users = adminService.listAllUsers();
        java.util.Comparator<com.uex.fablab.data.model.User> userComp = java.util.Comparator.comparing(u -> u.getFechaRegistro(), java.util.Comparator.nullsLast(java.time.LocalDate::compareTo));
        if (dirUsers == null || !"asc".equalsIgnoreCase(dirUsers)) {
            users.sort(userComp.reversed());
        } else {
            users.sort(userComp);
        }
        model.addAttribute("users", users);

        model.addAttribute("machines", machineService.listAll());
        model.addAttribute("products", productService.findAll());

        // Bookings: default by fechaReserva desc
        var bookings = bookingService.listAll();
        java.util.Comparator<com.uex.fablab.data.model.Booking> bookingComp = java.util.Comparator.comparing(b -> b.getFechaReserva(), java.util.Comparator.nullsLast(java.time.LocalDate::compareTo));
        if (dirBookings == null || !"asc".equalsIgnoreCase(dirBookings)) bookings.sort(bookingComp.reversed()); else bookings.sort(bookingComp);
        model.addAttribute("bookings", bookings);

        // Shifts: default by date desc
        var shifts = shiftService.listAll();
        java.util.Comparator<com.uex.fablab.data.model.Shift> shiftComp = java.util.Comparator.comparing(s -> s.getDate(), java.util.Comparator.nullsLast(java.time.LocalDate::compareTo));
        if (dirShifts == null || !"asc".equalsIgnoreCase(dirShifts)) shifts.sort(shiftComp.reversed()); else shifts.sort(shiftComp);
        model.addAttribute("shifts", shifts);

        model.addAttribute("courses", courseService.listAll());

        // Inscriptions: default by date desc
        var inscriptions = inscriptionService.listAll();
        java.util.Comparator<com.uex.fablab.data.model.Inscription> insComp = java.util.Comparator.comparing(i -> i.getDate(), java.util.Comparator.nullsLast(java.time.LocalDate::compareTo));
        if (dirInscriptions == null || !"asc".equalsIgnoreCase(dirInscriptions)) inscriptions.sort(insComp.reversed()); else inscriptions.sort(insComp);
        model.addAttribute("inscriptions", inscriptions);

        // Receipts: default by fechaEmision desc
        var receipts = receiptService.listAll();
        java.util.Comparator<com.uex.fablab.data.model.Receipt> recComp = java.util.Comparator.comparing(r -> r.getFechaEmision(), java.util.Comparator.nullsLast(java.time.LocalDate::compareTo));
        if (dirReceipts == null || !"asc".equalsIgnoreCase(dirReceipts)) receipts.sort(recComp.reversed()); else receipts.sort(recComp);
        model.addAttribute("receipts", receipts);

        return "admin/admin";
    }

    @org.springframework.web.bind.annotation.PostMapping("/admin/users/{id}/role")
    public String changeUserRole(@org.springframework.web.bind.annotation.PathVariable("id") Long id,
                                 @org.springframework.web.bind.annotation.RequestParam("admin") boolean admin) {
        adminService.changeRole(id, admin);
        return "redirect:/admin/admin";
    }

    @org.springframework.web.bind.annotation.PostMapping("/admin/users/{id}/delete")
    public String deleteUser(@org.springframework.web.bind.annotation.PathVariable("id") Long id) {
        adminService.deleteByAdmin(id);
        return "redirect:/admin/admin";
    }

    // Machine deletion is implemented in MachinesController at POST /admin/machines/{id}/delete
    // We avoid defining the same mapping here to prevent ambiguous handler mappings.

    @org.springframework.web.bind.annotation.PostMapping("/admin/bookings/{id}/delete")
    public String deleteBookingAdmin(@org.springframework.web.bind.annotation.PathVariable("id") Long id) {
        bookingService.delete(id);
        return "redirect:/admin/admin";
    }

    @org.springframework.web.bind.annotation.PostMapping("/admin/shifts/{id}/delete")
    public String deleteShiftAdmin(@org.springframework.web.bind.annotation.PathVariable("id") Long id) {
        shiftService.delete(id);
        return "redirect:/admin/admin";
    }

    // Course deletion is implemented in CourseController at POST /admin/courses/{id}/delete
    // We avoid defining the same mapping here to prevent ambiguous handler mappings.

    @org.springframework.web.bind.annotation.PostMapping("/admin/inscriptions/{id}/delete")
    public String deleteInscriptionAdmin(@org.springframework.web.bind.annotation.PathVariable("id") Long id) {
        inscriptionService.delete(id);
        return "redirect:/admin/admin";
    }

    // Cambiar estado de un recibo (Pagado / Pendiente / Anulado)
    @org.springframework.web.bind.annotation.PostMapping("/admin/receipts/{id}/status")
    /**
     * Cambia el estado de un recibo. Si el recibo se marca como "Pagado",
     * realiza acciones asociadas: confirma inscripciones, confirma o crea
     * reservas y marca turnos como reservados cuando procede.
     *
     * @param id     identificador del recibo
     * @param status nombre del estado (por ejemplo: "Pagado", "Pendiente", "Anulado")
     * @return redirección al panel de administración o con parámetro de error
     */
    public String changeReceiptStatus(@org.springframework.web.bind.annotation.PathVariable("id") Long id,
                                      @org.springframework.web.bind.annotation.RequestParam("status") String status) {
        try {
            var opt = receiptService.findById(id);
            if (opt.isEmpty()) {
                return "redirect:/admin/admin?error=" + java.net.URLEncoder.encode("Recibo no encontrado", java.nio.charset.StandardCharsets.UTF_8);
            }
            com.uex.fablab.data.model.Receipt r = opt.get();
            try {
                com.uex.fablab.data.model.ReceiptStatus rs = com.uex.fablab.data.model.ReceiptStatus.valueOf(status);
                r.setEstadoRecibo(rs);
                receiptService.save(r);
                // If the receipt is now paid, confirm related resources
                if (rs == com.uex.fablab.data.model.ReceiptStatus.Pagado) {
                    // Confirm course inscription if present on the receipt
                    if (r.getCourse() != null) {
                        var course = r.getCourse();
                        var user = r.getUser();
                        boolean already = inscriptionService.listAll().stream().anyMatch(i -> i.getCourse() != null && i.getCourse().getId() != null && i.getCourse().getId().equals(course.getId())
                                && i.getUser() != null && i.getUser().getId() != null && i.getUser().getId().equals(user.getId()));
                        if (!already) {
                            com.uex.fablab.data.model.Inscription ins = new com.uex.fablab.data.model.Inscription();
                            ins.setUser(user);
                            ins.setCourse(course);
                            ins.setDate(java.time.LocalDate.now());
                            inscriptionService.save(ins);
                        }
                    }
                    // Confirm bookings and reserve shifts associated to this receipt
                    if (r.getShifts() != null && !r.getShifts().isEmpty()) {
                        for (com.uex.fablab.data.model.Shift s : r.getShifts()) {
                            var bopt = bookingService.findByUserAndShift(r.getUser(), s);
                            if (bopt.isPresent()) {
                                var b = bopt.get();
                                try {
                                    b.setEstado(com.uex.fablab.data.model.BookingStatus.Confirmada);
                                    bookingService.save(b);
                                } catch (Exception ignore) {}
                            } else {
                                com.uex.fablab.data.model.Booking nb = new com.uex.fablab.data.model.Booking();
                                nb.setUser(r.getUser());
                                nb.setShift(s);
                                nb.setFechaReserva(java.time.LocalDate.now());
                                nb.setEstado(com.uex.fablab.data.model.BookingStatus.Confirmada);
                                bookingService.save(nb);
                            }
                            if (s.getStatus() != com.uex.fablab.data.model.ShiftStatus.Reservado) {
                                s.setStatus(com.uex.fablab.data.model.ShiftStatus.Reservado);
                                try { shiftService.save(s); } catch (Exception ignore) {}
                            }
                        }
                    }
                }
            } catch (IllegalArgumentException iae) {
                return "redirect:/admin/admin?error=" + java.net.URLEncoder.encode("Estado inválido", java.nio.charset.StandardCharsets.UTF_8);
            }
        } catch (Exception ex) {
            return "redirect:/admin/admin?error=" + java.net.URLEncoder.encode("Error actualizando recibo", java.nio.charset.StandardCharsets.UTF_8);
        }
        return "redirect:/admin/admin";
    }
}
