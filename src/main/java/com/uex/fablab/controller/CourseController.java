package com.uex.fablab.controller;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.uex.fablab.data.model.Course;
import com.uex.fablab.data.model.Inscription;
import com.uex.fablab.data.model.ReceiptStatus;
import com.uex.fablab.data.model.User;
import com.uex.fablab.data.repository.ReceiptRepository;
import com.uex.fablab.data.services.CourseService;
import com.uex.fablab.data.services.InscriptionService;
import com.uex.fablab.data.services.UserService;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

/**
 * Controlador para la gestión de cursos.
 *
 * <p>Expone tanto API REST (endpoints bajo <code>/api/courses</code>) como
 * las páginas HTML y formularios administrativos relacionados con cursos
 * (listado, detalle, alta, modificación y eliminación). Delega la lógica de
 * negocio y persistencia a los servicios inyectados (<code>CourseService</code>,
 * <code>InscriptionService</code>, <code>UserService</code>).</p>
 *
 * <p>Comportamiento importante:
 * - Las vistas Thymeleaf utilizan los métodos que llenan el {@code Model}.
 * - Los endpoints REST devuelven objetos {@code Course} y códigos HTTP
 *   apropiados (201, 200, 404, 204).
 * - Las operaciones que gestionan imágenes validan tamaño y tipo y las
 *   almacenan en el directorio de uploads del proyecto.</p>
 */
@Controller
@Validated
public class CourseController {
    private final CourseService courseService;
    private final InscriptionService inscriptionService;
    private final UserService userService;
    private final ReceiptRepository receiptRepository;

    public CourseController(CourseService courseService, InscriptionService inscriptionService, UserService userService, ReceiptRepository receiptRepository) {
        this.courseService = courseService;
        this.inscriptionService = inscriptionService;
        this.userService = userService;
        this.receiptRepository = receiptRepository;
    }

    @GetMapping("/api/courses")
    @ResponseBody
    public List<Course> all() {
        return courseService.listAll();
    }

    // --- HTML views and admin form handlers (moved here) ---
    /**
     * Página con el listado de cursos.
     *
     * @param session sesión HTTP (se consulta atributo USER_ADMIN)
     * @param model modelo Thymeleaf donde se añaden atributos
     * @return nombre de la vista que renderiza el listado
     */
    @GetMapping("/courses")
    public String coursesPage(HttpSession session, Model model) {
        boolean isAdmin = Boolean.TRUE.equals(session.getAttribute("USER_ADMIN"));
        var list = courseService.listAll();
        Object userId = session.getAttribute("USER_ID");
        Long currentUserId = userId instanceof Long ? (Long) userId : null;

        // Construir mapa de estados por curso para la vista: "Inscrito", "No disponible", "Plazas_completas", "Disponible"
        Map<Long, String> courseStatus = new HashMap<>();
        for (var c : list) {
            boolean enrolled = false;
            if (currentUserId != null) {
                for (Inscription ins : inscriptionService.listAll()) {
                    if (ins.getCourse() != null && ins.getCourse().getId() != null && ins.getCourse().getId().equals(c.getId())
                            && ins.getUser() != null && ins.getUser().getId() != null && ins.getUser().getId().equals(currentUserId)) {
                        enrolled = true; break;
                    }
                }
            }
            boolean started = c.getStartDate() != null && !c.getStartDate().isAfter(LocalDate.now());
            boolean spotsFull = c.getCapacity() != null && c.getInscriptions() != null && c.getInscriptions().size() >= c.getCapacity();

            String status;
            if (enrolled) status = "Inscrito";
            else if (started) status = "No_disponible";
            else if (spotsFull) status = "Plazas_completas";
            else status = "Disponible";
            courseStatus.put(c.getId(), status);
        }

        model.addAttribute("courses", list);
        model.addAttribute("isAdmin", isAdmin);
        model.addAttribute("courseStatus", courseStatus);
        Map<Long,String> imageUrls = new HashMap<>();
        for (Course c : list) { imageUrls.put(c.getId(), resolveImageUrl(c.getId())); }
        model.addAttribute("courseImageUrls", imageUrls);
        return "courses";
    }

    @GetMapping("/admin/courses/new")
    public String newCoursePage() { return "admin/add-course"; }

    /**
     * Página de modificación de un curso (administradores).
     *
     * @param id id del curso
     * @param session sesión HTTP
     * @param model modelo de la vista
     * @return vista de modificación o redirección con mensaje de error
     */
    @GetMapping("/admin/modify-course")
    public String modifyCoursePage(@RequestParam("id") Long id, HttpSession session, Model model) {
        var opt = courseService.findById(id);
        if (opt.isEmpty()) {
            return "redirect:/courses?error=" + java.net.URLEncoder.encode("Curso no encontrado", java.nio.charset.StandardCharsets.UTF_8);
        }
        Course course = opt.get();
        boolean isAdmin = Boolean.TRUE.equals(session.getAttribute("USER_ADMIN"));
        model.addAttribute("course", course);
        model.addAttribute("courseId", id);
        model.addAttribute("isAdmin", isAdmin);
        model.addAttribute("courseImageUrl", resolveImageUrl(course.getId()));
        return "admin/modify-course";
    }

    /**
     * Detalle público de un curso.
     *
     * @param id id del curso
     * @param session sesión HTTP (para identificar usuario y permisos)
     * @param successParam parámetro opcional que indica éxito de operación
     * @param canceledParam parámetro opcional que indica cancelación
     * @param errorParam parámetro opcional con texto de error
     * @param model modelo de la vista
     * @return nombre de la vista de detalle o redirección si no existe
     */
    @GetMapping("/courses/{id}")
        public String courseDetailsPage(@PathVariable("id") Long id, HttpSession session,
            @RequestParam(value = "success", required = false) String successParam,
            @RequestParam(value = "canceled", required = false) String canceledParam,
            @RequestParam(value = "error", required = false) String errorParam,
            Model model) {
        Object userId = session.getAttribute("USER_ID");
        var opt = courseService.findById(id);
        if (opt.isEmpty()) {
            return "redirect:/courses?error=" + java.net.URLEncoder.encode("Curso no encontrado", java.nio.charset.StandardCharsets.UTF_8);
        }
        Course course = opt.get();
        Long currentUserId = null;
        boolean isAdmin = false;
        if (userId instanceof Long aLong) {
            currentUserId = aLong;
            var uopt = userService.findById(currentUserId);
            if (uopt.isPresent()) isAdmin = uopt.get().isAdmin();
        }
        boolean enrolled = false;
        int enrolledCount = course.getInscriptions() != null ? course.getInscriptions().size() : 0;
        if (currentUserId != null) {
            for (Inscription ins : inscriptionService.listAll()) {
                if (ins.getCourse() != null && ins.getCourse().getId() != null && ins.getCourse().getId().equals(id)
                        && ins.getUser() != null && ins.getUser().getId() != null && ins.getUser().getId().equals(currentUserId)) {
                    enrolled = true; break;
                }
            }
        }
        // Disponible sólo si hay plazas y el curso aún no ha empezado
        boolean notStarted = course.getStartDate() == null || course.getStartDate().isAfter(LocalDate.now());
        boolean available = course.getCapacity() != null && course.getCapacity() > enrolledCount && notStarted;
        // Motivo por el que no está disponible (para mostrar en la vista)
        String notAvailableReason = null;
        if (!available && !enrolled) {
            if (course.getStartDate() != null && !course.getStartDate().isAfter(LocalDate.now())) {
                notAvailableReason = "El curso ya ha empezado";
            } else if (course.getCapacity() == null) {
                notAvailableReason = "Plazas no configuradas";
            } else if (course.getCapacity() <= enrolledCount) {
                notAvailableReason = "No quedan plazas";
            }
        }
        if (successParam != null) model.addAttribute("messageSuccess", "Inscripción realizada correctamente.");
        if (canceledParam != null) model.addAttribute("messageSuccess", "Inscripción cancelada correctamente.");
        if (errorParam != null) model.addAttribute("messageError", errorParam);
        model.addAttribute("isAdmin", isAdmin);
        model.addAttribute("currentUserId", currentUserId);
        model.addAttribute("course", course);
        model.addAttribute("courseImageUrl", resolveImageUrl(course.getId()));
        model.addAttribute("enrolled", enrolled);
        model.addAttribute("available", available);
        model.addAttribute("notAvailableReason", notAvailableReason);
        model.addAttribute("enrolledCount", enrolledCount);
        return "course-details";
    }

    @PostMapping("/courses/{id}/cancel")
        /**
         * Cancela una inscripción en un curso.
         * <p>Los administradores pueden cancelar inscripciones de otros usuarios
         * mediante parámetros; los usuarios normales solo pueden cancelar su propia
         * inscripción.</p>
         *
         * @param id id del curso
         * @param inscriptionId id de la inscripción a borrar (opcional)
         * @param userId id del usuario objetivo (opcional, admin)
         * @param session sesión HTTP
         * @return redirección con indicador de resultado
         */
        public String cancelInscription(@PathVariable("id") Long id,
            @RequestParam(value = "inscriptionId", required = false) Long inscriptionId,
            @RequestParam(value = "userId", required = false) Long userId,
            HttpSession session) {
        Object sid = session.getAttribute("USER_ID");
        Long currentUid = sid instanceof Long ? (Long) sid : null;
        boolean isAdmin = Boolean.TRUE.equals(session.getAttribute("USER_ADMIN"));
        var copt = courseService.findById(id);
        if (copt.isEmpty()) {
            return "redirect:/courses?error=" + java.net.URLEncoder.encode("Curso no encontrado", java.nio.charset.StandardCharsets.UTF_8);
        }
        try {
            if (isAdmin) {
                if (inscriptionId != null) {
                    inscriptionService.delete(inscriptionId);
                    return "redirect:/courses/" + id + "?canceled=1";
                }
                if (userId != null) {
                    var found = inscriptionService.listAll().stream().filter(i -> i.getCourse() != null && i.getCourse().getId() != null && i.getCourse().getId().equals(id)
                            && i.getUser() != null && i.getUser().getId() != null && i.getUser().getId().equals(userId)).findFirst();
                    if (found.isPresent()) {
                        inscriptionService.delete(found.get().getId());
                        return "redirect:/courses/" + id + "?canceled=1";
                    }
                    return "redirect:/courses/" + id + "?error=" + java.net.URLEncoder.encode("Inscripción no encontrada", java.nio.charset.StandardCharsets.UTF_8);
                }
                // Admin without params: nothing to do
                return "redirect:/courses/" + id + "?error=" + java.net.URLEncoder.encode("Parámetros insuficientes", java.nio.charset.StandardCharsets.UTF_8);
            } else {
                // regular user: can cancel only their own inscription
                if (currentUid == null) {
                    return "redirect:/login?error=" + java.net.URLEncoder.encode("Necesitas iniciar sesión", java.nio.charset.StandardCharsets.UTF_8);
                }
                var found = inscriptionService.listAll().stream().filter(i -> i.getCourse() != null && i.getCourse().getId() != null && i.getCourse().getId().equals(id)
                        && i.getUser() != null && i.getUser().getId() != null && i.getUser().getId().equals(currentUid)).findFirst();
                if (found.isPresent()) {
                    inscriptionService.delete(found.get().getId());
                    return "redirect:/courses/" + id + "?canceled=1";
                }
                return "redirect:/courses/" + id + "?error=" + java.net.URLEncoder.encode("No tienes una inscripción en este curso", java.nio.charset.StandardCharsets.UTF_8);
            }
        } catch (Exception ex) {
            return "redirect:/courses/" + id + "?error=" + java.net.URLEncoder.encode("Error cancelando inscripción", java.nio.charset.StandardCharsets.UTF_8);
        }
    }

    @PostMapping("/courses/{id}/inscribe")
    /**
     * Inscribe al usuario autenticado en el curso indicado si hay plazas y no
     * ha comenzado.
     *
     * @param id id del curso
     * @param session sesión HTTP (contiene {@code USER_ID})
     * @return redirección con resultado (success o error)
     */
    public String inscribeCourse(@PathVariable("id") Long id, HttpSession session) {
        Object userId = session.getAttribute("USER_ID");
        if (!(userId instanceof Long)) {
            return "redirect:/login?error=" + java.net.URLEncoder.encode("Necesitas iniciar sesión para inscribirte", java.nio.charset.StandardCharsets.UTF_8);
        }
        Long uid = (Long) userId;
        var uopt = userService.findById(uid);
        if (uopt.isEmpty()) {
            return "redirect:/login?error=" + java.net.URLEncoder.encode("Usuario inválido", java.nio.charset.StandardCharsets.UTF_8);
        }
        var copt = courseService.findById(id);
        if (copt.isEmpty()) {
            return "redirect:/courses?error=" + java.net.URLEncoder.encode("Curso no encontrado", java.nio.charset.StandardCharsets.UTF_8);
        }
        Course course = copt.get();
        int enrolledCount = course.getInscriptions() != null ? course.getInscriptions().size() : 0;
        // No permitir inscripción si el curso ya ha empezado
        if (course.getStartDate() != null && !course.getStartDate().isAfter(LocalDate.now())) {
            return "redirect:/courses/" + id + "?error=" + java.net.URLEncoder.encode("El curso ya ha empezado", java.nio.charset.StandardCharsets.UTF_8);
        }
        if (course.getCapacity() != null && course.getCapacity() <= enrolledCount) {
            return "redirect:/courses/" + id + "?error=" + java.net.URLEncoder.encode("No quedan plazas", java.nio.charset.StandardCharsets.UTF_8);
        }
        User user = uopt.get();
        // Comprobar si ya está inscrito
        boolean already = inscriptionService.listAll().stream().anyMatch(i -> i.getCourse() != null && i.getCourse().getId() != null && i.getCourse().getId().equals(id)
                && i.getUser() != null && i.getUser().getId() != null && i.getUser().getId().equals(uid));
        if (already) {
            return "redirect:/courses/" + id + "?error=" + java.net.URLEncoder.encode("Ya estás inscrito", java.nio.charset.StandardCharsets.UTF_8);
        }
        // Si existe un recibo pendiente para este curso, bloquear nueva inscripción.
        // Considera recibos con course asociado o cuando el concepto contiene el nombre del curso (por seguridad).
        String courseName = course.getName() != null ? course.getName() : "";
        boolean pendingCash = user.getReceipts().stream().anyMatch(r ->
            r.getEstadoRecibo() == ReceiptStatus.Pendiente
            && (
                (r.getCourse() != null && r.getCourse().getId() != null && r.getCourse().getId().equals(id))
                || (r.getConcepto() != null && !r.getConcepto().isBlank() && r.getConcepto().contains(courseName))
            )
        );
        if (pendingCash) {
            return "redirect:/courses/" + id + "?error=" + java.net.URLEncoder.encode("Tienes un pago pendiente para este curso. Espera a que se anule o confirme.", java.nio.charset.StandardCharsets.UTF_8);
        }
        try {
            Inscription ins = new Inscription();
            ins.setUser(user);
            ins.setCourse(course);
            ins.setDate(java.time.LocalDate.now());
            inscriptionService.save(ins);
            return "redirect:/courses/" + id + "?success=1";
        } catch (Exception ex) {
            return "redirect:/courses/" + id + "?error=" + java.net.URLEncoder.encode("Error al inscribirse", java.nio.charset.StandardCharsets.UTF_8);
        }
    }

    @PostMapping("/admin/courses")
    public String createCourse(
            @RequestParam("title") String title,
            @RequestParam(value = "description", required = false) String description,
            @RequestParam(value = "startDate", required = false) String startDate,
            @RequestParam(value = "endDate", required = false) String endDate,
            @RequestParam(value = "capacity", required = false) String capacity,
            @RequestParam(value = "price", required = false) String price,
            @RequestParam(value = "image", required = false) MultipartFile image) {
        try {
            if (image != null && !image.isEmpty() && image.getSize() > 2_000_000) {
                return "redirect:/admin/courses/new?error=" + java.net.URLEncoder.encode("La imagen supera 2 MB", java.nio.charset.StandardCharsets.UTF_8);
            }
            Course c = new Course();
            c.setName(title);
            if (description != null) c.setDescription(description);
            if (capacity != null && !capacity.isBlank()) { try { c.setCapacity(Integer.valueOf(capacity)); } catch (NumberFormatException ignored) {} }
            if (startDate != null && !startDate.isBlank()) { try { c.setStartDate(java.time.LocalDate.parse(startDate)); } catch (Exception ignored) {} }
            if (endDate != null && !endDate.isBlank()) { try { c.setEndDate(java.time.LocalDate.parse(endDate)); } catch (Exception ignored) {} }
            if (price != null && !price.isBlank()) { try { c.setPrecio(Double.valueOf(price)); } catch (NumberFormatException ignored) {} }
            c = courseService.save(c);
            if (image != null && !image.isEmpty() && c.getId() != null) {
                String contentType = image.getContentType(); long size = image.getSize();
                if (size <= 2_000_000 && contentType != null && (contentType.equals("image/jpeg") || contentType.equals("image/png") || contentType.equals("image/gif"))) {
                    String ext = contentType.equals("image/png") ? ".png" : contentType.equals("image/gif") ? ".gif" : ".jpg";
                    try { Path dir = getUploadsDir(); Files.createDirectories(dir); Path target = dir.resolve("course-" + c.getId() + ext); try (var in = image.getInputStream()) { Files.copy(in, target, StandardCopyOption.REPLACE_EXISTING); } } catch (IOException ignored) {}
                }
            }
            return "redirect:/courses";
        } catch (Exception ex) {
            return "redirect:/admin/courses/new?error=" + java.net.URLEncoder.encode("Error creando curso", java.nio.charset.StandardCharsets.UTF_8);
        }
    }

    /**
     * Actualiza un curso desde el formulario administrativo y gestiona la
     * posible subida/actualización de la imagen asociada.
     *
     * @param id id del curso a actualizar
     * @param title título (opcional)
     * @param description descripción (opcional)
     * @param startDate fecha inicio (ISO, opcional)
     * @param endDate fecha fin (ISO, opcional)
     * @param capacity plazas (opcional)
     * @param price precio (opcional)
     * @param image imagen a subir (opcional)
     * @return redirección al listado o a la página de edición con error
     */

    @PostMapping("/admin/courses/{id}")
    public String updateCourseFromForm(@PathVariable("id") Long id,
            @RequestParam(value = "title", required = false) String title,
            @RequestParam(value = "description", required = false) String description,
            @RequestParam(value = "startDate", required = false) String startDate,
            @RequestParam(value = "endDate", required = false) String endDate,
            @RequestParam(value = "capacity", required = false) String capacity,
            @RequestParam(value = "price", required = false) String price,
            @RequestParam(value = "image", required = false) MultipartFile image) {
        try {
            var opt = courseService.findById(id);
            if (opt.isEmpty()) return "redirect:/courses?error=" + java.net.URLEncoder.encode("Curso no encontrado", java.nio.charset.StandardCharsets.UTF_8);
            Course c = opt.get();
            if (title != null && !title.isBlank()) c.setName(title);
            if (description != null) c.setDescription(description);
            if (capacity != null && !capacity.isBlank()) { try { c.setCapacity(Integer.valueOf(capacity)); } catch (NumberFormatException ignored) {} }
            if (startDate != null && !startDate.isBlank()) { try { c.setStartDate(java.time.LocalDate.parse(startDate)); } catch (Exception ignored) {} }
            if (endDate != null && !endDate.isBlank()) { try { c.setEndDate(java.time.LocalDate.parse(endDate)); } catch (Exception ignored) {} }
            if (price != null && !price.isBlank()) { try { c.setPrecio(Double.valueOf(price)); } catch (NumberFormatException ignored) {} }
            c = courseService.save(c);
            if (image != null && !image.isEmpty() && c.getId() != null) {
                String contentType = image.getContentType(); long size = image.getSize();
                if (size <= 2_000_000 && contentType != null && (contentType.equals("image/jpeg") || contentType.equals("image/png") || contentType.equals("image/gif"))) {
                    deleteCourseImages(c.getId());
                    String ext = contentType.equals("image/png") ? ".png" : contentType.equals("image/gif") ? ".gif" : ".jpg";
                    try { Path dir = getUploadsDir(); Files.createDirectories(dir); Path target = dir.resolve("course-" + c.getId() + ext); try (var in = image.getInputStream()) { Files.copy(in, target, StandardCopyOption.REPLACE_EXISTING); } } catch (IOException ignored) {}
                }
            }
            return "redirect:/courses";
        } catch (Exception ex) {
            return "redirect:/admin/modify-course?id=" + id + "&error=" + java.net.URLEncoder.encode("Error actualizando curso", java.nio.charset.StandardCharsets.UTF_8);
        }
    }

    /**
     * Elimina un curso y sus imágenes asociadas (si existieran).
     *
     * @param id id del curso
     * @return redirección al listado de cursos
     */
    @PostMapping("/admin/courses/{id}/delete")
    public String deleteCourseFromForm(@PathVariable("id") Long id) {
        try { courseService.delete(id); deleteCourseImages(id); } catch (Exception ignored) {}
        return "redirect:/courses";
    }

    /** API REST: crea un curso desde JSON y devuelve 201 Created. */
    @PostMapping("/api/courses")
    @ResponseBody
    public ResponseEntity<Course> create(@Valid @RequestBody Course course) {
        Course saved = courseService.save(course);
        return ResponseEntity.created(URI.create("/api/courses/" + saved.getId())).body(saved);
    }

    /** API REST: obtiene un curso por su id (200) o 404 si no existe. */
    @GetMapping("/api/courses/{id}")
    @ResponseBody
    public ResponseEntity<Course> one(@PathVariable Long id) {
        return courseService.findById(id).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    /** API REST: actualiza campos de un curso existente. */
    @PutMapping("/api/courses/{id}")
    @ResponseBody
    public ResponseEntity<Course> update(@PathVariable Long id, @Valid @RequestBody Course input) {
        return courseService.findById(id).map(existing -> {
            existing.setName(input.getName());
            existing.setDescription(input.getDescription());
            existing.setCapacity(input.getCapacity());
            existing.setStartDate(input.getStartDate());
            existing.setEndDate(input.getEndDate());
            existing.setPrecio(input.getPrecio());
            existing.setEstado(input.getEstado());
            Course saved = courseService.save(existing);
            return ResponseEntity.ok(saved);
        }).orElse(ResponseEntity.notFound().build());
    }

    /** API REST: elimina un curso por id (204) o 404 si no existe. */
    @DeleteMapping("/api/courses/{id}")
    @ResponseBody
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        if (!courseService.delete(id)) return ResponseEntity.notFound().build();
        return ResponseEntity.noContent().build();
    }
    
    private String resolveImageUrl(Long id) {
        if (id == null) return "/img/logo.png";
        String[] exts = {".jpg",".png",".gif"};
        for (String ext: exts) {
            Path p = getUploadsDir().resolve("course-" + id + ext);
            if (Files.exists(p)) return "/img/upload/courses/course-" + id + ext;
        }
        return "/img/curso.png";
    }
    /**
     * Elimina los archivos de imagen asociados a un curso (si existen).
     *
     * @param id id del curso
     */
    private void deleteCourseImages(Long id) {
        if (id == null) return;
        String[] exts = {".jpg",".png",".gif"};
        for (String ext: exts) {
            try { Files.deleteIfExists(getUploadsDir().resolve("course-" + id + ext)); } catch (IOException ignored) {}
        }
    }

    /**
     * Obtiene el directorio de uploads para imágenes de cursos. Intenta la
     * ruta del módulo y, si no existe, usa la ruta local del proyecto.
     *
     * @return {@code Path} al directorio de uploads
     */
    private Path getUploadsDir() {
        Path moduleDir = Path.of("ProyectoMDAI","src","main","resources","templates","img","upload","courses");
        if (Files.exists(moduleDir.getParent() != null ? moduleDir.getParent().getParent() : moduleDir)) return moduleDir;
        Path localDir = Path.of("src","main","resources","templates","img","upload","courses");
        return localDir;
    }
}
