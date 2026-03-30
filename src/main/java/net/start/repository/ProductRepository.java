package net.start.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import net.start.model.Product;

public interface ProductRepository extends JpaRepository<Product, Integer> {

    List<Product> findByCategoriesCategoriesId(Integer categoriesId);

    List<Product> findByProductStatus(String productStatus);

    Page<Product> findByProductNameContainingIgnoreCase(String keyword, Pageable pageable);

    // 🌟 เพิ่ม Query ใหม่ตัวนี้เข้าไป
    @Query("SELECT p FROM Product p WHERE " +
           "(:keyword IS NULL OR LOWER(p.productName) LIKE LOWER(CONCAT('%', :keyword, '%'))) AND " +
           "(:categoryId IS NULL OR p.categories.categoriesId = :categoryId)")
    Page<Product> searchAndFilter(@Param("keyword") String keyword, @Param("categoryId") Integer categoryId, Pageable pageable);

 // 🌟 1. เพิ่ม Query นี้ สำหรับหน้า "สั่งอาหาร" โดยเฉพาะ 🌟
    @Query("SELECT p FROM Product p WHERE p.productStatus = 'available' AND " +
           "(:categoryId IS NULL OR p.categories.categoriesId = :categoryId)")
    List<Product> findAvailableAndFilterByCategory(@Param("categoryId") Integer categoryId);
}