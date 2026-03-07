package net.start.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import net.start.model.Product;
import net.start.repository.ProductRepository;

@Service
public class ProductService {

    @Autowired
    ProductRepository productRepository;

    public List<Product> findAll() {
        return productRepository.findAll();
    }

    public Product findById(int id) {
        return productRepository.findById(id).orElseThrow();
    }

    public List<Product> findByCategoryId(int categoryId) {
        return productRepository.findByCategoriesCategoriesId(categoryId);
    }

    public List<Product> findAvailable() {
        return productRepository.findByProductStatus("available");
    }

    public void save(Product product) {
        productRepository.save(product);
    }

    public void deleteById(int id) {
        productRepository.deleteById(id);
    }

}
