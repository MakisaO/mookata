package net.start.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import net.start.model.Product;
import net.start.model.Categories;
import net.start.service.ProductService;
import net.start.service.CategoriesService;

@Controller
@RequestMapping("/menu") // กำหนด Path หลักให้เป็น /menu
public class MenuController {

    @Autowired
    private ProductService productService;

    @Autowired
    private CategoriesService categoriesService;

 // 1. Read: แสดงรายการเมนูทั้งหมด แบบแบ่งหน้า ค้นหา และกรองตามหมวดหมู่
    @GetMapping("")
    public String listMenu(
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "categoryId", required = false) Integer categoryId, // 🌟 เพิ่มการรับค่า categoryId
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "sortField", defaultValue = "productId") String sortField,
            @RequestParam(value = "sortDir", defaultValue = "asc") String sortDir,
            Model model) {
        
        int pageSize = 10;
        
        // 🌟 อัปเดตการเรียกใช้ Service ให้ส่ง categoryId ไปด้วย
        Page<Product> productPage = productService.findPaginated(keyword, categoryId, page, pageSize, sortField, sortDir);
        
        model.addAttribute("products", productPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", productPage.getTotalPages());
        model.addAttribute("totalItems", productPage.getTotalElements());
        model.addAttribute("keyword", keyword);
        model.addAttribute("sortField", sortField);
        model.addAttribute("sortDir", sortDir);
        model.addAttribute("reverseSortDir", sortDir.equals("asc") ? "desc" : "asc");

        // 🌟 2 บรรทัดนี้สำคัญมาก สำหรับสร้างปุ่มหมวดหมู่บนหน้าเว็บ
        model.addAttribute("categories", categoriesService.findAll()); // ส่งรายการหมวดหมู่ทั้งหมดไปสร้างปุ่ม
        model.addAttribute("activeCategoryId", categoryId);            // ส่ง ID หมวดหมู่ปัจจุบันไปทำสีปุ่มให้เข้มขึ้น

        return "menu/list";
    }

    // 2. Create: แสดงหน้าฟอร์มเพิ่มเมนูใหม่
    @GetMapping("/new")
    public String showCreateForm(Model model) {
        Product product = new Product();
        product.setCategories(new Categories()); // Initialize to avoid null pointer in view
        model.addAttribute("product", product);
        model.addAttribute("categories", categoriesService.findAll());
        // ต้องมีไฟล์ที่: src/main/resources/templates/menu/form.html
        return "menu/form";
    }

    // 3. Create/Save: รับข้อมูลจากฟอร์มและบันทึกลงฐานข้อมูล
    @PostMapping("/save")
    public String saveMenu(@ModelAttribute("product") Product product) {
        // If categories was bound only by ID, ensure it's not a detached/empty object if needed
        // But usually JPA handles it if it has an ID
        productService.save(product);
        return "redirect:/menu"; // บันทึกเสร็จให้วิ่งกลับไปหน้า list
    }

    // 4. Update: แสดงฟอร์มแก้ไขเมนูเดิม
    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable("id") int id, Model model) {
        Product existingProduct = productService.findById(id);
        if (existingProduct.getCategories() == null) {
            existingProduct.setCategories(new Categories());
        }
        model.addAttribute("product", existingProduct);
        model.addAttribute("categories", categoriesService.findAll());
        return "menu/form"; // ใช้ไฟล์ form.html ร่วมกัน
    }

    // 5. Update/Save: รับข้อมูลการแก้ไขและบันทึกทับ
    @PostMapping("/update/{id}")
    public String updateMenu(@PathVariable("id") int id, @ModelAttribute("product") Product productDetails) {
        Product existingProduct = productService.findById(id);

        // แก้ให้ตรงกับ Product.java
        existingProduct.setProductName(productDetails.getProductName());
        existingProduct.setProductPrice(productDetails.getProductPrice());
        existingProduct.setProductStatus(productDetails.getProductStatus());
        existingProduct.setProductDetail(productDetails.getProductDetail());
        existingProduct.setCategories(productDetails.getCategories());

        productService.save(existingProduct);
        return "redirect:/menu";
    }
    // เพิ่มตัวนี้ใน MenuController.java

    @GetMapping("/delete/{id}")
    public String deleteMenu(@PathVariable("id") int id) {
        productService.deleteById(id);
        return "redirect:/menu";
    }
}