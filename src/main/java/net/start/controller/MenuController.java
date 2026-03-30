package net.start.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import net.start.model.Product;
import net.start.model.Categories;
import net.start.service.ProductService;
import net.start.service.CategoriesService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/menu") // เปลี่ยนเป็น /api/menu เพื่อให้ชัดเจนว่าเป็น REST API
public class MenuController {

    @Autowired
    private ProductService productService;

    @Autowired
    private CategoriesService categoriesService;

    // 1. Read: แสดงรายการเมนูทั้งหมด แบบแบ่งหน้า ค้นหา และกรองตามหมวดหมู่
    @GetMapping({"", "/"})
    public ResponseEntity<Map<String, Object>> listMenu(
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "categoryId", required = false) Integer categoryId, // 🌟 รับค่า categoryId เพื่อกรอง
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "sortField", defaultValue = "productId") String sortField,
            @RequestParam(value = "sortDir", defaultValue = "asc") String sortDir) {
        
        int pageSize = 10;
        
        // 🌟 อัปเดตการเรียกใช้ Service ให้ส่ง categoryId ไปด้วย
        Page<Product> productPage = productService.findPaginated(keyword, categoryId, page, pageSize, sortField, sortDir);
        
        Map<String, Object> response = new HashMap<>(); // ส่งคืนเป็น JSON Object
        response.put("products", productPage.getContent());
        response.put("currentPage", page);
        response.put("totalPages", productPage.getTotalPages());
        response.put("totalItems", productPage.getTotalElements());
        response.put("keyword", keyword);
        response.put("sortField", sortField);
        response.put("sortDir", sortDir);

        // ส่งรายการหมวดหมู่ไปด้วยสำหรับสร้างปุ่มผ่าน UI แบบ JSON
        response.put("categories", categoriesService.findAll());
        response.put("activeCategoryId", categoryId);

        return ResponseEntity.ok(response);
    }

    // 2. Read: แสดงข้อมูลเมนูเดี่ยว (สำหรับดึงไปแสดงในหน้า Edit บน Frontend)
    @GetMapping("/{id}")
    public ResponseEntity<Product> getMenuById(@PathVariable("id") int id) {
        Product existingProduct = productService.findById(id);
        if (existingProduct != null) {
            return ResponseEntity.ok(existingProduct);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    // 3. Create: สร้างเมนูใหม่ (รับข้อมูลเป็น JSON)
    @PostMapping({"", "/"})
    public ResponseEntity<Product> createMenu(@RequestBody Product product) {
        productService.save(product);
        return ResponseEntity.ok(product);
    }

    // 4. Update: รับข้อมูลการแก้ไขและบันทึกทับ (รับข้อมูลเป็น JSON)
    @PutMapping("/{id}")
    public ResponseEntity<Product> updateMenu(@PathVariable("id") int id, @RequestBody Product productDetails) {
        Product existingProduct = productService.findById(id);

        if (existingProduct == null) {
            return ResponseEntity.notFound().build();
        }

        existingProduct.setProductName(productDetails.getProductName());
        existingProduct.setProductPrice(productDetails.getProductPrice());
        existingProduct.setProductStatus(productDetails.getProductStatus());
        existingProduct.setProductDetail(productDetails.getProductDetail());
        existingProduct.setCategories(productDetails.getCategories());

        productService.save(existingProduct);
        return ResponseEntity.ok(existingProduct);
    }

    // 5. Delete: ลบเมนู
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Boolean>> deleteMenu(@PathVariable("id") int id) {
        productService.deleteById(id);
        Map<String, Boolean> response = new HashMap<>();
        response.put("deleted", Boolean.TRUE);
        return ResponseEntity.ok(response);
    }

    // 6. Get Categories: สำหรับดึงหมวดหมู่ไปทำ Dropdown บน Frontend
    @GetMapping("/categories")
    public ResponseEntity<List<Categories>> getCategories() {
        return ResponseEntity.ok(categoriesService.findAll());
    }
}
