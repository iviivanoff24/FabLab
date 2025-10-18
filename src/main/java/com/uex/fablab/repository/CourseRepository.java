package com.uex.fablab.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.uex.fablab.model.Course;

public interface CourseRepository extends JpaRepository<Course, Long> {
}
