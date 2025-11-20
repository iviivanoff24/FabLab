package com.uex.fablab.controller;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.uex.fablab.data.model.Machine;
import com.uex.fablab.data.model.MachineStatus;
import com.uex.fablab.data.repository.MachineRepository;

import jakarta.servlet.http.HttpSession;

@Controller
public class MachinesController {

    private static final String MARKER = "<!-- ADMIN_ONLY_MARKER -->";
    private static final String MACHINES_MARKER = "<!-- MACHINES_LIST_MARKER -->";

    private static final String ADMIN_CARD = ""
            + "<div class=\"card text-bg-warning mb-3 mx-auto\" style=\"max-width: 18rem; max-height: 10rem;\">"
            + "  <a href=\"/admin/add-machine.html\" class=\"text-decoration-none text-dark\">"
            + "    <div class=\"card-body d-flex flex-column align-items-center justify-content-center py-3\">"
            + "      <img src=\"img/add.png\" alt=\"Añadir máquina\" width=\"48\" height=\"48\" class=\"mb-2\" />"
            + "      <h5 class=\"card-title text-dark m-0\">Nueva Máquina</h5>"
            + "    </div>"
            + "  </a>"
            + "</div>";

    private final MachineRepository machineRepository;

    public MachinesController(MachineRepository machineRepository) {
        this.machineRepository = machineRepository;
    }

    @GetMapping({"/machines", "/machines.html"})
    public ResponseEntity<String> machines(HttpSession session) throws java.io.IOException {
        boolean isAdmin = Boolean.TRUE.equals(session.getAttribute("USER_ADMIN"));
        var resource = new ClassPathResource("templates/machines.html");
        String html = new String(resource.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        // Inyectar lista de máquinas
        String cards = buildMachinesCards(isAdmin);
        html = html.replace(MACHINES_MARKER, cards);
        if (isAdmin) {
            html = html.replace(MARKER, ADMIN_CARD);
        } else {
            html = html.replace(MARKER, "");
        }
        return ResponseEntity.ok().contentType(MediaType.TEXT_HTML).body(html);
    }

    // API: obtener datos de máquina por id (para pre-rellenar formulario de edición)
    @GetMapping("/api/machines/{id}")
    public ResponseEntity<?> getMachineById(@PathVariable("id") Long id) {
        var opt = machineRepository.findById(id);
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

    private String buildMachinesCards(boolean isAdmin) {
        java.util.List<Machine> list = machineRepository.findAll();
        StringBuilder sb = new StringBuilder();
        if (list.isEmpty()) {
            sb.append("<div class=\"alert alert-info mt-3\">No hay máquinas registradas aún.</div>");
            return sb.toString();
        }
        for (Machine m : list) {
            String name = safe(m.getName());
            String desc = safe(m.getDescription());
            if (desc.isBlank()) {
                desc = "Sin descripción.";
            }
            java.math.BigDecimal price = m.getHourlyPrice();
            String priceStr = price != null ? String.format(java.util.Locale.US, "%.2f", price) : "0.00";
            String imgUrl = resolveImageUrl(m.getId());
            MachineStatus st = m.getStatus();
            String statusLabel = (st == MachineStatus.En_mantenimiento) ? "En mantenimiento" : (st == MachineStatus.Disponible ? "Disponible" : "Sin estado");
            String statusClass = (st == MachineStatus.En_mantenimiento) ? "text-bg-warning" : (st == MachineStatus.Disponible ? "text-bg-success" : "text-bg-secondary");
            sb.append("<div class=\"card\">")
                    .append("  <div class=\"face face1\">")
                    .append("    <div class=\"content\">")
                    .append("      <img src=\"")
                    .append(imgUrl)
                    .append("\" />")
                    .append("      <h3>")
                    .append(name)
                    .append("</h3>")
                    .append("    </div>")
                    .append("  </div>")
                    .append("  <div class=\"face face2\">")
                    .append("    <div class=\"content\">")
                    .append("      <p>")
                    .append(desc)
                    .append("</p>")
                    .append("      <p class=\"mb-1\"><span class=\"badge text-bg-primary\">€")
                    .append(priceStr)
                    .append("/h</span></p>")
                    .append("      <p class=\"mb-1\"><span class=\"badge ")
                    .append(statusClass)
                    .append("\">")
                    .append(statusLabel)
                    .append("</span></p>")
                    // Acciones (Reservar, Editar, Borrar) en una sola fila
                    .append("      <div class='d-flex flex-wrap align-items-center gap-2 mt-1'>")
                    .append("        <a href='#' class='btn btn-sm btn-warning'>Reservar</a>");
            if (isAdmin) {
                sb.append("<a href='/admin/modify-machine.html?id=").append(m.getId()).append("' class='btn btn-sm btn-outline-primary'><i class='bi bi-pencil-square'></i> Editar</a>");
            }
            sb.append("      </div>");
            sb.append("    </div>")
                    .append("  </div>")
                    .append("</div>");
        }
        return sb.toString();
    }

    private String resolveImageUrl(Long id) {
        if (id == null) {
            return "/img/logo.png";
        }
        String[] exts = {".jpg", ".png", ".gif"};
        for (String ext : exts) {
            // Ruta física dentro del módulo resources: .../templates/img/upload/machines
            java.nio.file.Path p = getUploadsDir().resolve("machine-" + id + ext);
            if (java.nio.file.Files.exists(p)) {
                // URL pública: /img/upload/machines/...
                return "/img/upload/machines/machine-" + id + ext;
            }
        }
        return "/img/logo.png";
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

    @GetMapping({"/admin/add-machine", "/admin/add-machine.html"})
    public ResponseEntity<org.springframework.core.io.Resource> addMachinePage(HttpSession session) {
        boolean isAdmin = Boolean.TRUE.equals(session.getAttribute("USER_ADMIN"));
        if (!isAdmin) {
            // Reutiliza login con mensaje
            return ResponseEntity.status(302)
                    .header("Location", "/login?error=" + java.net.URLEncoder.encode("Solo administradores", java.nio.charset.StandardCharsets.UTF_8))
                    .build();
        }
        var res = new ClassPathResource("templates/admin/add-machine.html");
        if (!res.exists()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok().contentType(MediaType.TEXT_HTML).body(res);
    }

    @GetMapping({"/admin/modify-machine", "/admin/modify-machine.html"})
    public ResponseEntity<org.springframework.core.io.Resource> modifyMachinePage(HttpSession session,
            @RequestParam("id") Long id) {
        boolean isAdmin = Boolean.TRUE.equals(session.getAttribute("USER_ADMIN"));
        if (!isAdmin) {
            return ResponseEntity.status(302)
                    .header("Location", "/login?error=" + java.net.URLEncoder.encode("Solo administradores", java.nio.charset.StandardCharsets.UTF_8))
                    .build();
        }
        var res = new ClassPathResource("templates/admin/modify-machine.html");
        if (!res.exists()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok().contentType(MediaType.TEXT_HTML).body(res);
    }

    // Simple creación de máquina. En un proyecto real se validaría y se usaría DTO.
    @PostMapping("/admin/machines")
    public String createMachine(HttpSession session,
            @RequestParam("name") String name,
            @RequestParam(value = "location", required = false) String location,
            @RequestParam(value = "description", required = false) String description,
            @RequestParam(value = "status", required = false) String status,
            @RequestParam(value = "hourlyPrice", required = false) String hourlyPrice,
            @RequestParam(value = "image", required = false) MultipartFile image) {
        boolean isAdmin = Boolean.TRUE.equals(session.getAttribute("USER_ADMIN"));
        if (!isAdmin) {
            return "redirect:/login?error=" + java.net.URLEncoder.encode("Solo administradores", java.nio.charset.StandardCharsets.UTF_8);
        }
        // Guardar usando repositorio inyectado
        try {
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
            m = machineRepository.save(m);

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

    @PostMapping("/admin/machines/{id}/delete")
    public String deleteMachine(HttpSession session, @PathVariable("id") Long id) {
        boolean isAdmin = Boolean.TRUE.equals(session.getAttribute("USER_ADMIN"));
        if (!isAdmin) {
            return "redirect:/login?error=" + java.net.URLEncoder.encode("Solo administradores", java.nio.charset.StandardCharsets.UTF_8);
        }
        try {
            // Eliminar archivos de imagen asociados si existen
            deleteMachineImages(id);
            machineRepository.deleteById(id);
        } catch (Exception ignored) {
        }
        return "redirect:/machines";
    }

    // Actualización de máquina (incluye reemplazo de imagen: borra la anterior si se sube una nueva)
    @PostMapping("/admin/machines/{id}")
    public String updateMachine(HttpSession session,
            @PathVariable("id") Long id,
            @RequestParam(value = "name", required = false) String name,
            @RequestParam(value = "location", required = false) String location,
            @RequestParam(value = "description", required = false) String description,
            @RequestParam(value = "status", required = false) String status,
            @RequestParam(value = "hourlyPrice", required = false) String hourlyPrice,
            @RequestParam(value = "image", required = false) MultipartFile image) {
        boolean isAdmin = Boolean.TRUE.equals(session.getAttribute("USER_ADMIN"));
        if (!isAdmin) {
            return "redirect:/login?error=" + java.net.URLEncoder.encode("Solo administradores", java.nio.charset.StandardCharsets.UTF_8);
        }
        try {
            java.util.Optional<Machine> opt = machineRepository.findById(id);
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
            m = machineRepository.save(m);

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
            } catch (Exception ignored) {
            }
        }
    }

    private Path getUploadsDir() {
        // Ruta preferida: módulo del proyecto
        Path moduleDir = Path.of("ProyectoMDAI", "src", "main", "resources", "templates", "img", "upload", "machines");
        if (Files.exists(moduleDir.getParent() != null ? moduleDir.getParent().getParent() : moduleDir)) {
            return moduleDir;
        }
        // Alternativa: cuando el cwd ya es el módulo
        Path localDir = Path.of("src", "main", "resources", "templates", "img", "upload", "machines");
        return localDir;
    }
}
