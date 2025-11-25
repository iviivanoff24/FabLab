package com.uex.fablab.data.services;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.uex.fablab.data.model.Course;
import com.uex.fablab.data.repository.CourseRepository;

/**
 * Servicio de cursos.
 */
@Service
@Transactional
public class CourseService {
    private final CourseRepository repo;
    public CourseService(CourseRepository repo) { this.repo = repo; }
    /** Lista todos los cursos. */
    public List<Course> listAll() { return repo.findAll(); }
    /** Busca curso por id. */
    public Optional<Course> findById(Long id) { return repo.findById(id); }
    /** Guarda curso. */
    public Course save(Course c) { return repo.save(c); }
    /** Elimina curso por id. */
    public boolean delete(Long id) { if (!repo.existsById(id)) return false; repo.deleteById(id); return true; }
}
