package com.uex.fablab.data.services;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.uex.fablab.data.model.Product;
import com.uex.fablab.data.model.SubProduct;
import com.uex.fablab.data.repository.ProductRepository;
import com.uex.fablab.data.repository.SubProductRepository;

/**
 * Servicio de productos y subproductos.
 */
@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final SubProductRepository subProductRepository;

    /**
     * Constructor.
     * @param productRepository repositorio de productos
     * @param subProductRepository repositorio de subproductos
     */
    public ProductService(ProductRepository productRepository, SubProductRepository subProductRepository) {
        this.productRepository = productRepository;
        this.subProductRepository = subProductRepository;
    }

    /** Lista todos los productos. */
    public List<Product> findAll() {
        return productRepository.findAll();
    }

    /** Busca producto por id. */
    public Optional<Product> findById(Long id) {
        return productRepository.findById(id);
    }

    /** Guarda producto. */
    @Transactional
    public Product save(Product product) {
        return productRepository.save(product);
    }

    /** Elimina producto por id. */
    @Transactional
    public void deleteById(Long id) {
        productRepository.deleteById(id);
    }

    // SubProduct methods

    /** Lista subproductos de un producto. */
    public List<SubProduct> findSubProductsByProductId(Long productId) {
        return subProductRepository.findByProductId(productId);
    }

    /** Busca subproducto por id. */
    public Optional<SubProduct> findSubProductById(Long id) {
        return subProductRepository.findById(id);
    }

    /** Guarda subproducto. */
    @Transactional
    public SubProduct saveSubProduct(SubProduct subProduct) {
        return subProductRepository.save(subProduct);
    }

    /** Elimina subproducto por id. */
    @Transactional
    public void deleteSubProductById(Long id) {
        subProductRepository.deleteById(id);
    }
}
