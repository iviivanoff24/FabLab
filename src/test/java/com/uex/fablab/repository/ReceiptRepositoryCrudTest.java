package com.uex.fablab.repository;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import com.uex.fablab.model.PaymentMethod;
import com.uex.fablab.model.Receipt;
import com.uex.fablab.model.User;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class ReceiptRepositoryCrudTest {

    @Autowired
    private ReceiptRepository receiptRepository;

    @Autowired
    private UserRepository userRepository;

    private Receipt newReceipt(User u) {
        Receipt r = new Receipt();
        r.setUser(u);
        r.setTotalPrice(new BigDecimal("12.34"));
        r.setDate(java.time.LocalDateTime.now());
        r.setMetodoPago(PaymentMethod.Efectivo);
        r.setConcepto("test");
        return r;
    }

    @Test
    @DisplayName("Create and Read Receipt")
    void createAndReadReceipt() {
    User u = new User();
    u.setName("U1");
    u.setEmail("u1" + System.nanoTime() + "@example.com");
    u.setPassword("p");
    u.setAdmin(false);
    u = userRepository.save(u);

    Receipt saved = receiptRepository.save(newReceipt(u));
        assertThat(saved.getId()).isNotNull();

        Receipt found = receiptRepository.findById(saved.getId()).orElse(null);
        assertThat(found).isNotNull();
        assertThat(found.getUser().getId()).isEqualTo(u.getId());
    }
}
