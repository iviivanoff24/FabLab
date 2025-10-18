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

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class ProductRepositoryCrudTest {

    @Autowired
    private ProductRepository productRepository;

    private Product newProduct(String name, String description, BigDecimal price, int stock) {
        Product p = new Product();
        p.setName(name);
        p.setDescription(description);
        p.setPrice(price);
        p.setStock(stock);
        return p;
    }

    @Test
    @DisplayName("Create and Read Product")
    void createAndReadProduct() {
        Product saved = productRepository.save(newProduct("PLA 1kg", "Filamento PLA", new BigDecimal("19.99"), 10));
        assertThat(saved.getId()).isNotNull();

        Optional<Product> found = productRepository.findById(saved.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("PLA 1kg");
        assertThat(found.get().getPrice()).isEqualByComparingTo("19.99");
        assertThat(found.get().getStock()).isEqualTo(10);
    }

    @Test
    @DisplayName("Update Product fields")
    void updateProduct() {
        Product saved = productRepository.save(newProduct("Resina", "Resina UV", new BigDecimal("24.50"), 5));
        saved.setStock(7);
        saved.setPrice(new BigDecimal("22.00"));
        Product updated = productRepository.save(saved);

        assertThat(updated.getStock()).isEqualTo(7);
        assertThat(updated.getPrice()).isEqualByComparingTo("22.00");
    }

    @Test
    @DisplayName("Delete Product")
    void deleteProduct() {
        Product saved = productRepository.save(newProduct("MDF 3mm", "Tablero MDF", new BigDecimal("3.25"), 100));
        Long id = saved.getId();
        productRepository.deleteById(id);
        assertThat(productRepository.findById(id)).isEmpty();
    }
}
