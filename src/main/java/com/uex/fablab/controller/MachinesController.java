package com.uex.fablab.controller;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.uex.fablab.data.model.Machine;
import com.uex.fablab.data.model.MachineStatus;
import com.uex.fablab.data.services.MachineService;

import jakarta.servlet.http.HttpSession;

/**
 * Controlador de máquinas.
 * Usa plantillas Thymeleaf para listar máquinas y páginas de administración.
 */
@Controller
public class MachinesController {

    private final MachineService machineService;

    public MachinesController(MachineService machineService) {
        this.machineService = machineService;
    }

    /**
     * Página de listado de máquinas usando Thymeleaf.
     * @param session sesión para detectar rol admin
     * @param model modelo con máquinas y urls de imagen
     * @return nombre de la vista
     */
    @GetMapping("/machines")
    public String machines(HttpSession session, Model model, 
                           @RequestParam(value = "q", required = false) String q,
                           @RequestParam(value = "date", required = false) String dateStr,
                           @RequestParam(value = "time", required = false) String timeStr) {
        boolean isAdmin = Boolean.TRUE.equals(session.getAttribute("USER_ADMIN"));
        
        java.util.List<Machine> list;
        if (dateStr != null && !dateStr.isBlank() && timeStr != null && !timeStr.isBlank()) {
             try {
                java.time.LocalDate date = java.time.LocalDate.parse(dateStr);
                // Asegurar formato HH:mm
                if(timeStr.length() == 5) timeStr += ":00";
                java.time.LocalTime time = java.time.LocalTime.parse(timeStr);
                list = machineService.findAvailableMachines(date, time);
                model.addAttribute("filterDate", dateStr);
                model.addAttribute("filterTime", timeStr);
             } catch (Exception e) {
                 list = machineService.searchByName(q);
             }
        } else {
            list = machineService.searchByName(q);
        }

        model.addAttribute("machines", list);
        model.addAttribute("isAdmin", isAdmin);
        model.addAttribute("q", q == null ? "" : q);
        Map<Long,String> imageUrls = new HashMap<>();
        for (Machine m : list) {
            imageUrls.put(m.getId(), resolveImageUrl(m.getId()));
        }
        model.addAttribute("machineImageUrls", imageUrls);
        return "machines"; // templates/machines.html
    }

    /**
     * API: obtiene datos de una máquina por id (útil para pre-rellenar formularios).
     * @param id identificador de la máquina
     * @return JSON con datos básicos o 404 si no existe
     */
    @GetMapping("/api/machines/{id}")
    public ResponseEntity<?> getMachineById(@PathVariable("id") Long id) {
        var opt = machineService.findById(id);
        if (opt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        var m = opt.get();
        java.util.Map<String, Object> body = new java.util.HashMap<>();
        body.put("id", m.getId());
        body.put("name", m.getName());
        body.put("location", m.getLocation());
        body.put("description", m.getDescription());
        body.put("status", m.getStatus() != null ? m.getStatus().name() : null);
        body.put("hourlyPrice", m.getHourlyPrice());
        body.put("imageUrl", resolveImageUrl(m.getId()));
        return ResponseEntity.ok(body);
    }

    private String resolveImageUrl(Long id) {
        if (id == null) {
            return "/img/logo.png";
        }
        String[] exts = {".jpg", ".png", ".gif"};
        for (String ext : exts) {
            // Ruta física dentro del módulo resources: .../uploads/machines
            java.nio.file.Path p = getUploadsDir().resolve("machine-" + id + ext);
            if (java.nio.file.Files.exists(p)) {
                // URL pública: /uploads/machines/...
                return "/uploads/machines/machine-" + id + ext;
            }
        }
        return "/img/maquina.png";
    }

    /**
     * Página de alta de máquina (solo administradores).
     *
     * @return HTML de la página de alta
     */
    @GetMapping("/admin/add-machine")
    public String addMachinePage() {
        return "admin/add-machine"; // templates/admin/add-machine.html
    }

    /**
     * Página de modificación de máquina (solo administradores).
     *
     * @param id id de la máquina a modificar
     * @return HTML de la página de modificación
     */
    @GetMapping("/admin/modify-machine")
    public String modifyMachinePage(@RequestParam("id") Long id, Model model) {
        model.addAttribute("machineId", id);
        return "admin/modify-machine"; // templates/admin/modify-machine.html
    }

    /**
     * Crea una máquina.
     * Valida imagen opcional y persiste la entidad.
     *
     * @param name nombre
     * @param location ubicación
     * @param description descripción
     * @param status estado
     * @param hourlyPrice precio por hora
     * @param image imagen subida (máx 2 MB)
     * @return redirección a listado o página de error
     */
    @PostMapping("/admin/machines")
    public String createMachine(
            @RequestParam("name") String name,
            @RequestParam(value = "location", required = false) String location,
            @RequestParam(value = "description", required = false) String description,
            @RequestParam(value = "status", required = false) String status,
            @RequestParam(value = "hourlyPrice", required = false) String hourlyPrice,
            @RequestParam(value = "image", required = false) MultipartFile image) {
        // Guardar usando repositorio inyectado
        try {
            // Validación previa: si hay imagen y supera 2 MB, abortar creación
            if (image != null && !image.isEmpty()) {
                long size = image.getSize();
                if (size > 2_000_000) {
                    return "redirect:/admin/add-machine?error=" + java.net.URLEncoder.encode("La imagen supera 2 MB", java.nio.charset.StandardCharsets.UTF_8);
                }
            }
            Machine m = new Machine();
            m.setName(name);
            if (location != null) {
                m.setLocation(location);
            }
            if (description != null) {
                m.setDescription(description);
            }
            if (status != null) {
                try {
                    m.setStatus(com.uex.fablab.data.model.MachineStatus.valueOf(status));
                } catch (IllegalArgumentException ignored) {
                }
            }
            if (hourlyPrice != null && !hourlyPrice.isBlank()) {
                try {
                    m.setHourlyPrice(new java.math.BigDecimal(hourlyPrice));
                } catch (NumberFormatException ignored) {
                }
            }
            m = machineService.save(m);

            // Guardar imagen opcional si viene y no está vacía
            if (image != null && !image.isEmpty() && m.getId() != null) {
                // Validación básica de tipo y tamaño (2 MB)
                String contentType = image.getContentType();
                long size = image.getSize();
                if (size <= 2_000_000 && contentType != null && (contentType.equals("image/jpeg") || contentType.equals("image/png") || contentType.equals("image/gif"))) {
                    String ext = contentType.equals("image/png") ? ".png" : contentType.equals("image/gif") ? ".gif" : ".jpg";
                    try {
                        Path dir = getUploadsDir();
                        Files.createDirectories(dir);
                        Path target = dir.resolve("machine-" + m.getId() + ext);
                        // Si ya existía con otra extensión, opcionalmente podríamos limpiar, pero mantenemos simple
                        try (var in = image.getInputStream()) {
                            Files.copy(in, target, StandardCopyOption.REPLACE_EXISTING);
                        }
                    } catch (java.io.IOException ignored) {
                        // Silenciar errores de subida para no bloquear la creación
                    }
                }
            }
            return "redirect:/machines";
        } catch (IllegalArgumentException | org.springframework.beans.BeansException ex) {
            return "redirect:/admin/add-machine?error=" + java.net.URLEncoder.encode("Error creando máquina", java.nio.charset.StandardCharsets.UTF_8);
        }
    }

    /**
     * Elimina una máquina por id (borra también imágenes asociadas).
     *
     * @param id identificador de la máquina
     * @return redirección al listado
     */
    @PostMapping("/admin/machines/{id}/delete")
    public String deleteMachine(@PathVariable("id") Long id) {
        try {
            machineService.delete(id); // ahora el servicio también borra las imágenes
        } catch (Exception ignored) {
        }
        return "redirect:/machines";
    }

    /**
     * Actualiza una máquina (y reemplaza imagen si se sube una nueva, borrando la anterior).
     *
     * @param id id de la máquina
     * @param name nombre
     * @param location ubicación
     * @param description descripción
     * @param status estado
     * @param hourlyPrice precio por hora
     * @param image imagen
     * @return redirección al listado o página de error
     */
    @PostMapping("/admin/machines/{id}")
    public String updateMachine(
            @PathVariable("id") Long id,
            @RequestParam(value = "name", required = false) String name,
            @RequestParam(value = "location", required = false) String location,
            @RequestParam(value = "description", required = false) String description,
            @RequestParam(value = "status", required = false) String status,
            @RequestParam(value = "hourlyPrice", required = false) String hourlyPrice,
            @RequestParam(value = "image", required = false) MultipartFile image) {
        try {
            java.util.Optional<Machine> opt = machineService.findById(id);
            if (opt.isEmpty()) {
                return "redirect:/machines?error=" + java.net.URLEncoder.encode("Máquina no encontrada", java.nio.charset.StandardCharsets.UTF_8);
            }
            Machine m = opt.get();
            if (name != null && !name.isBlank()) {
                m.setName(name);
            }
            if (location != null) {
                m.setLocation(location);
            }
            if (description != null) {
                m.setDescription(description);
            }
            if (status != null) {
                try {
                    m.setStatus(MachineStatus.valueOf(status));
                } catch (IllegalArgumentException ignored) {
                }
            }
            if (hourlyPrice != null && !hourlyPrice.isBlank()) {
                try {
                    m.setHourlyPrice(new java.math.BigDecimal(hourlyPrice));
                } catch (NumberFormatException ignored) {
                }
            }
            m = machineService.save(m);

            // Si llega nueva imagen válida, borrar anteriores y guardar la nueva
            if (image != null && !image.isEmpty() && m.getId() != null) {
                String contentType = image.getContentType();
                long size = image.getSize();
                if (size <= 2_000_000 && contentType != null && (contentType.equals("image/jpeg") || contentType.equals("image/png") || contentType.equals("image/gif"))) {
                    // Borrar anteriores (cualquier extensión soportada)
                    deleteMachineImages(m.getId());
                    String ext = contentType.equals("image/png") ? ".png" : contentType.equals("image/gif") ? ".gif" : ".jpg";
                    try {
                        Path dir = getUploadsDir();
                        Files.createDirectories(dir);
                        Path target = dir.resolve("machine-" + m.getId() + ext);
                        try (var in = image.getInputStream()) {
                            Files.copy(in, target, StandardCopyOption.REPLACE_EXISTING);
                        }
                    } catch (java.io.IOException ignored) {
                    }
                }
            }
            return "redirect:/machines";
        } catch (Exception ex) {
            return "redirect:/admin/modify-machine?id=" + id + "&error=" + java.net.URLEncoder.encode("Error actualizando máquina", java.nio.charset.StandardCharsets.UTF_8);
        }
    }

    private void deleteMachineImages(Long id) {
        if (id == null) {
            return;
        }
        String[] exts = {".jpg", ".png", ".gif"};
        for (String ext : exts) {
            try {
                Path p = getUploadsDir().resolve("machine-" + id + ext);
                Files.deleteIfExists(p);
            } catch (IOException ignored) {
            }
        }
    }

    private Path getUploadsDir() {
        // Ruta preferida: módulo del proyecto
        Path moduleDir = Path.of("ProyectoMDAI", "src", "main", "resources", "uploads", "machines");
        if (Files.exists(moduleDir.getParent() != null ? moduleDir.getParent().getParent() : moduleDir)) {
            return moduleDir;
        }
        // Alternativa: cuando el cwd ya es el módulo
        Path localDir = Path.of("src", "main", "resources", "uploads", "machines");
        return localDir;
    }
}
