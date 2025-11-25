package com.uex.fablab.controller;

import java.net.URI;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.uex.fablab.data.model.Course;
import com.uex.fablab.data.services.CourseService;

import jakarta.validation.Valid;

/**
 * API REST de cursos.
 * Permite listar, crear, consultar, actualizar y eliminar cursos.
 */
@RestController
@RequestMapping("/courses")
@Validated
public class CourseController {
    private final CourseService courseService;

    public CourseController(CourseService courseService) {
        this.courseService = courseService;
    }

    @GetMapping
    public List<Course> all() {
        return courseService.listAll();
    }

    @PostMapping
    public ResponseEntity<Course> create(@Valid @RequestBody Course course) {
        Course saved = courseService.save(course);
        return ResponseEntity.created(URI.create("/courses/" + saved.getId())).body(saved);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Course> one(@PathVariable Long id) {
        return courseService.findById(id).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
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

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        if (!courseService.delete(id)) return ResponseEntity.notFound().build();
        return ResponseEntity.noContent().build();
    }
}
