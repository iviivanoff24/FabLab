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
 * Controlador simple para las vistas del panel de administración.
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

    public AdminController(AdminService adminService,
                           MachineService machineService,
                           BookingService bookingService,
                           ShiftService shiftService,
                           CourseService courseService,
                           InscriptionService inscriptionService,
                           ReceiptService receiptService,
                           UserService userService) {
        this.adminService = adminService;
        this.machineService = machineService;
        this.bookingService = bookingService;
        this.shiftService = shiftService;
        this.courseService = courseService;
        this.inscriptionService = inscriptionService;
        this.receiptService = receiptService;
        this.userService = userService;
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
    public String adminPanel(Model model) {
        model.addAttribute("users", adminService.listAllUsers());
        model.addAttribute("machines", machineService.listAll());
        model.addAttribute("bookings", bookingService.listAll());
        model.addAttribute("shifts", shiftService.listAll());
        model.addAttribute("courses", courseService.listAll());
        model.addAttribute("inscriptions", inscriptionService.listAll());
        model.addAttribute("receipts", receiptService.listAll());
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
