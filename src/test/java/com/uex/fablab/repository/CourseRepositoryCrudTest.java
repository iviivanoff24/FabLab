package com.uex.fablab.repository;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import com.uex.fablab.model.Course;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class CourseRepositoryCrudTest {

    @Autowired
    private CourseRepository courseRepository;

    private Course newCourse(String name, String desc, int capacity, LocalDate s, LocalDate e) {
        Course c = new Course();
        c.setName(name);
        c.setDescription(desc);
        c.setCapacity(capacity);
        c.setStartDate(s);
        c.setEndDate(e);
        return c;
    }

    @Test
    @DisplayName("Create and Read Course")
    void createAndReadCourse() {
        Course saved = courseRepository.save(newCourse("Impresión 3D", "Curso básico", 12,
                LocalDate.now().plusDays(7), LocalDate.now().plusDays(14)));
        assertThat(saved.getId()).isNotNull();

        Optional<Course> found = courseRepository.findById(saved.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("Impresión 3D");
        assertThat(found.get().getCapacity()).isEqualTo(12);
    }

    @Test
    @DisplayName("Update Course fields")
    void updateCourse() {
        Course saved = courseRepository.save(newCourse("Láser", "Corte láser", 8,
                LocalDate.now().plusDays(3), LocalDate.now().plusDays(6)));
        saved.setCapacity(10);
        saved.setDescription("Corte láser avanzado");
        Course updated = courseRepository.save(saved);

        assertThat(updated.getCapacity()).isEqualTo(10);
        assertThat(updated.getDescription()).isEqualTo("Corte láser avanzado");
    }

    @Test
    @DisplayName("Delete Course")
    void deleteCourse() {
        Course saved = courseRepository.save(newCourse("Fresado CNC", "Intro", 6,
                LocalDate.now().plusDays(10), LocalDate.now().plusDays(12)));
        Long id = saved.getId();
        courseRepository.deleteById(id);
        assertThat(courseRepository.findById(id)).isEmpty();
    }
}
