package com.uex.fablab.repository;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;

import com.uex.fablab.model.User;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class UserRepositoryCrudTest {

    @Autowired
    private UserRepository userRepository;

    private User newUser(String name, String email, String password, boolean admin) {
        User u = new User();
        u.setName(name);
        u.setEmail(email);
        u.setPassword(password);
        u.setAdmin(admin);
        return u;
    }

    @Test
    @DisplayName("Create and Read User")
    void createAndReadUser() {
        User saved = userRepository.save(newUser("Alice", "alice@example.com", "secret", false));
        assertThat(saved.getId()).isNotNull();

        Optional<User> found = userRepository.findById(saved.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("Alice");
        assertThat(found.get().getEmail()).isEqualTo("alice@example.com");
        assertThat(found.get().isAdmin()).isFalse();
    }

    @Test
    @DisplayName("Update User fields")
    void updateUser() {
        User saved = userRepository.save(newUser("Bob", "bob@example.com", "secret", false));
        saved.setName("Bobby");
        saved.setAdmin(true);
        User updated = userRepository.save(saved);

        assertThat(updated.getName()).isEqualTo("Bobby");
        assertThat(updated.isAdmin()).isTrue();
    }

    @Test
    @DisplayName("Delete User")
    void deleteUser() {
        User saved = userRepository.save(newUser("Charlie", "charlie@example.com", "secret", false));
        Long id = saved.getId();
        userRepository.deleteById(id);
        assertThat(userRepository.findById(id)).isEmpty();
    }

    @Test
    @DisplayName("Unique email constraint")
    void uniqueEmailConstraint() {
        userRepository.save(newUser("Dana", "dana@example.com", "secret", false));
        String email = "dana" + System.nanoTime() + "@example.com";
        userRepository.save(newUser("Dana", email, "secret", false));
        DataIntegrityViolationException ex = assertThrows(DataIntegrityViolationException.class, () -> {
            userRepository.saveAndFlush(newUser("Dany", email, "secret", false));
        });
        assertThat(ex).isNotNull();
    }
}
