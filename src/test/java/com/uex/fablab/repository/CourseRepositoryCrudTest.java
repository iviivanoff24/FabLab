package com.uex.fablab.repository;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import com.uex.fablab.data.model.Course;
import com.uex.fablab.data.repository.CourseRepository;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class CourseRepositoryCrudTest {

    @Autowired
    private CourseRepository courseRepository;

    private Course newCourse(String name) {
        Course c = new Course();
        c.setName(name);
        c.setDescription("desc");
        c.setCapacity(10);
        c.setStartDate(LocalDate.now());
        c.setEndDate(LocalDate.now().plusDays(7));
        return c;
    }

    @Test
    @DisplayName("Create and Read Course")
    void createAndReadCourse() {
        Course saved = courseRepository.save(newCourse("Intro"));
        assertThat(saved.getId()).isNotNull();

        Course found = courseRepository.findById(saved.getId()).orElse(null);
        assertThat(found).isNotNull();
        assertThat(found.getName()).isEqualTo("Intro");
    }
}
