package net.start.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import net.start.model.Product;

public interface ProductRepository extends JpaRepository<Product, Integer> {

    List<Product> findByCategoriesCategoriesId(Integer categoriesId);

    List<Product> findByProductStatus(String productStatus);

}
