package com.uex.fablab.repository;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import com.uex.fablab.model.Course;
import com.uex.fablab.model.Inscription;
import com.uex.fablab.model.User;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class InscriptionRepositoryCrudTest {

    @Autowired
    private InscriptionRepository inscriptionRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CourseRepository courseRepository;

    private Inscription newInscription(User u, Course c) {
        Inscription i = new Inscription();
        i.setUser(u);
        i.setCourse(c);
        i.setDate(LocalDate.now());
        return i;
    }

    @Test
    @DisplayName("Create Inscription")
    void createInscription() {
        User u = new User();
        u.setName("Stu");
        u.setEmail("stu" + System.nanoTime() + "@example.com");
        u.setPassword("x");
        u.setAdmin(false);
        u = userRepository.save(u);

        Course c = new Course();
        c.setName("C1");
        c.setDescription("d");
        c.setCapacity(5);
        c.setStartDate(LocalDate.now());
        c.setEndDate(LocalDate.now().plusDays(1));
        c = courseRepository.save(c);

        Inscription saved = inscriptionRepository.save(newInscription(u, c));
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getUser().getId()).isEqualTo(u.getId());
    }
}
