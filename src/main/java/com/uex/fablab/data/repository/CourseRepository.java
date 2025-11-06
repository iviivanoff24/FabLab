package com.uex.fablab.data.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.uex.fablab.data.model.Course;

public interface CourseRepository extends JpaRepository<Course, Long> {
}
