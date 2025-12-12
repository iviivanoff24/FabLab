package com.uex.fablab.data.services;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.uex.fablab.data.model.Product;
import com.uex.fablab.data.model.SubProduct;
import com.uex.fablab.data.repository.ProductRepository;
import com.uex.fablab.data.repository.SubProductRepository;

@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final SubProductRepository subProductRepository;

    public ProductService(ProductRepository productRepository, SubProductRepository subProductRepository) {
        this.productRepository = productRepository;
        this.subProductRepository = subProductRepository;
    }

    public List<Product> findAll() {
        return productRepository.findAll();
    }

    public Optional<Product> findById(Long id) {
        return productRepository.findById(id);
    }

    @Transactional
    public Product save(Product product) {
        return productRepository.save(product);
    }

    @Transactional
    public void deleteById(Long id) {
        productRepository.deleteById(id);
    }

    // SubProduct methods

    public List<SubProduct> findSubProductsByProductId(Long productId) {
        return subProductRepository.findByProductId(productId);
    }

    public Optional<SubProduct> findSubProductById(Long id) {
        return subProductRepository.findById(id);
    }

    @Transactional
    public SubProduct saveSubProduct(SubProduct subProduct) {
        return subProductRepository.save(subProduct);
    }

    @Transactional
    public void deleteSubProductById(Long id) {
        subProductRepository.deleteById(id);
    }
}
