package com.uex.fablab.data.services;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.uex.fablab.data.model.Course;
import com.uex.fablab.data.repository.CourseRepository;

@Service
@Transactional
public class CourseService {
    private final CourseRepository repo;
    public CourseService(CourseRepository repo) { this.repo = repo; }
    public List<Course> listAll() { return repo.findAll(); }
    public Optional<Course> findById(Long id) { return repo.findById(id); }
    public Course save(Course c) { return repo.save(c); }
    public boolean delete(Long id) { if (!repo.existsById(id)) return false; repo.deleteById(id); return true; }
}
