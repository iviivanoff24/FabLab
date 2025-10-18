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
import com.uex.fablab.model.Inscription;
import com.uex.fablab.model.User;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class InscriptionRepositoryCrudTest {

    @Autowired private InscriptionRepository inscriptionRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private CourseRepository courseRepository;

    private User newUser(String name, String email) {
        User u = new User();
        u.setName(name);
        u.setEmail(email);
        u.setPassword("secret");
        u.setAdmin(false);
        return u;
    }

    private Course newCourse(String name) {
        Course c = new Course();
        c.setName(name);
        c.setDescription("desc");
        c.setCapacity(5);
        c.setStartDate(LocalDate.now().plusDays(1));
        c.setEndDate(LocalDate.now().plusDays(2));
        return c;
    }

    @Test
    @DisplayName("Create and Read Inscription")
    void createAndReadInscription() {
        User user = userRepository.save(newUser("Hugo", "hugo@example.com"));
        Course course = courseRepository.save(newCourse("Arduino"));

        Inscription ins = new Inscription();
        ins.setUser(user);
        ins.setCourse(course);
        ins.setDate(LocalDate.now());
        Inscription saved = inscriptionRepository.save(ins);
        assertThat(saved.getId()).isNotNull();

        Optional<Inscription> found = inscriptionRepository.findById(saved.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getUser().getId()).isEqualTo(user.getId());
        assertThat(found.get().getCourse().getId()).isEqualTo(course.getId());
    }

    @Test
    @DisplayName("Update Inscription fields")
    void updateInscription() {
        User user = userRepository.save(newUser("Ivan", "ivan@example.com"));
        Course c1 = courseRepository.save(newCourse("Corte láser"));
        Course c2 = courseRepository.save(newCourse("Impresión 3D"));

        Inscription ins = new Inscription();
        ins.setUser(user);
        ins.setCourse(c1);
        ins.setDate(LocalDate.now());
        ins = inscriptionRepository.save(ins);

        ins.setCourse(c2);
        ins.setDate(LocalDate.now().plusDays(1));
        Inscription updated = inscriptionRepository.save(ins);
        assertThat(updated.getCourse().getId()).isEqualTo(c2.getId());
    }

    @Test
    @DisplayName("Delete Inscription")
    void deleteInscription() {
        User user = userRepository.save(newUser("Julia", "julia@example.com"));
        Course course = courseRepository.save(newCourse("Fresado"));

        Inscription ins = new Inscription();
        ins.setUser(user);
        ins.setCourse(course);
        ins.setDate(LocalDate.now());
        Inscription saved = inscriptionRepository.save(ins);
        Long id = saved.getId();
        inscriptionRepository.deleteById(id);
        assertThat(inscriptionRepository.findById(id)).isEmpty();
    }
}
