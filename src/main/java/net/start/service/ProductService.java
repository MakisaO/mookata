package net.start.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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
    
    // 🌟 อัปเดตเมธอดนี้ ให้รับ Integer categoryId เพิ่ม และเรียกใช้ searchAndFilter
    public Page<Product> findPaginated(String keyword, Integer categoryId, int pageNo, int pageSize, String sortField, String sortDir) {
        org.springframework.data.domain.Sort sort = sortDir.equalsIgnoreCase(org.springframework.data.domain.Sort.Direction.ASC.name()) ? 
            org.springframework.data.domain.Sort.by(sortField).ascending() : 
            org.springframework.data.domain.Sort.by(sortField).descending();
        
        Pageable pageable = PageRequest.of(pageNo - 1, pageSize, sort);
        
        // ถ้า keyword เป็นค่าว่าง ("") ให้แปลงเป็น null เพื่อให้ Query ทำงานได้ถูกต้อง
        String finalKeyword = (keyword != null && !keyword.isEmpty()) ? keyword : null;
        
        // โยนงานให้ Repository จัดการ
        return productRepository.searchAndFilter(finalKeyword, categoryId, pageable);
    }
 // 🌟 2. เพิ่มเมธอดนี้ สำหรับหน้า "สั่งอาหาร" โดยเฉพาะ 🌟
    public List<Product> findAvailableByCategoryId(Integer categoryId) {
        return productRepository.findAvailableAndFilterByCategory(categoryId);
    }

}