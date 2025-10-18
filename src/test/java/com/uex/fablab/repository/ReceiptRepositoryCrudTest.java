package com.uex.fablab.repository;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import com.uex.fablab.model.Product;
import com.uex.fablab.model.Receipt;
import com.uex.fablab.model.ReceiptItem;
import com.uex.fablab.model.User;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class ReceiptRepositoryCrudTest {

    @Autowired
    private ReceiptRepository receiptRepository;
    @Autowired
    private ReceiptItemRepository receiptItemRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ProductRepository productRepository;

    private User newUser(String name, String email) {
        User u = new User();
        u.setName(name);
        u.setEmail(email);
        u.setPassword("secret");
        u.setAdmin(false);
        return u;
    }

    private Product newProduct(String name, String price) {
        Product p = new Product();
        p.setName(name);
        p.setDescription("desc");
        p.setPrice(new BigDecimal(price));
        p.setStock(100);
        return p;
    }

    @Test
    @DisplayName("Create and Read Receipt with Items")
    void createAndReadReceipt() {
        User user = userRepository.save(newUser("Luis", "luis@example.com"));
        Product p1 = productRepository.save(newProduct("PLA", "10.00"));
        Product p2 = productRepository.save(newProduct("Resina", "20.00"));

        Receipt r = new Receipt();
        r.setUser(user);
        r.setTotalPrice(new BigDecimal("50.00"));

        ReceiptItem i1 = new ReceiptItem();
        i1.setProduct(p1);
        i1.setQuantity(2);
        i1.setPrice(new BigDecimal("10.00"));
        r.addItem(i1);

        ReceiptItem i2 = new ReceiptItem();
        i2.setProduct(p2);
        i2.setQuantity(1);
        i2.setPrice(new BigDecimal("30.00"));
        r.addItem(i2);

        Receipt saved = receiptRepository.save(r);
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getItems()).hasSize(2);

        Optional<Receipt> found = receiptRepository.findById(saved.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getItems()).hasSize(2);
        assertThat(found.get().getTotalPrice()).isEqualByComparingTo("50.00");
    }

    @Test
    @DisplayName("Update ReceiptItem and Receipt total")
    void updateReceipt() {
        User user = userRepository.save(newUser("Marta", "marta@example.com"));
        Product p = productRepository.save(newProduct("PLA", "10.00"));

        Receipt r = new Receipt();
        r.setUser(user);
        r.setTotalPrice(new BigDecimal("10.00"));

        ReceiptItem i = new ReceiptItem();
        i.setProduct(p);
        i.setQuantity(1);
        i.setPrice(new BigDecimal("10.00"));
        r.addItem(i);

        Receipt saved = receiptRepository.save(r);
        saved.setTotalPrice(new BigDecimal("20.00"));
        saved.getItems().get(0).setQuantity(2);
        Receipt updated = receiptRepository.save(saved);
        assertThat(updated.getTotalPrice()).isEqualByComparingTo("20.00");
        assertThat(updated.getItems().get(0).getQuantity()).isEqualTo(2);
    }

    @Test
    @DisplayName("Delete Receipt cascades items")
    void deleteReceipt() {
        User user = userRepository.save(newUser("Nora", "nora@example.com"));
        Product p = productRepository.save(newProduct("PLA", "10.00"));

        Receipt r = new Receipt();
        r.setUser(user);
        r.setTotalPrice(new BigDecimal("10.00"));
        ReceiptItem i = new ReceiptItem();
        i.setProduct(p);
        i.setQuantity(1);
        i.setPrice(new BigDecimal("10.00"));
        r.addItem(i);
        Receipt saved = receiptRepository.save(r);
        Long id = saved.getId();

        receiptRepository.deleteById(id);
        assertThat(receiptRepository.findById(id)).isEmpty();
        // items deberían ser eliminados por orphanRemoval
        assertThat(receiptItemRepository.findAll()).isEmpty();
    }
}
