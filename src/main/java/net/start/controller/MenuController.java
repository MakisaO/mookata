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
import net.start.service.ProductService;

@Controller
@RequestMapping("/menu") // กำหนด Path หลักให้เป็น /menu
public class MenuController {

    @Autowired
    private ProductService productService;

    // 1. Read: แสดงรายการเมนูทั้งหมด แบบแบ่งหน้าและค้นหา
    @GetMapping("")
    public String listMenu(
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "page", defaultValue = "1") int page,
            Model model) {
        
        int pageSize = 10;
        Page<Product> productPage = productService.findPaginated(keyword, page, pageSize);
        
        model.addAttribute("products", productPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", productPage.getTotalPages());
        model.addAttribute("totalItems", productPage.getTotalElements());
        model.addAttribute("keyword", keyword);

        return "menu/list";
    }

    // 2. Create: แสดงหน้าฟอร์มเพิ่มเมนูใหม่
    @GetMapping("/new")
    public String showCreateForm(Model model) {
        model.addAttribute("product", new Product());
        // ต้องมีไฟล์ที่: src/main/resources/templates/menu/form.html
        return "menu/form";
    }

    // 3. Create/Save: รับข้อมูลจากฟอร์มและบันทึกลงฐานข้อมูล
    @PostMapping("/save")
    public String saveMenu(@ModelAttribute("product") Product product) {
        productService.save(product);
        return "redirect:/menu"; // บันทึกเสร็จให้วิ่งกลับไปหน้า list
    }

    // 4. Update: แสดงฟอร์มแก้ไขเมนูเดิม
    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable("id") int id, Model model) {
        Product existingProduct = productService.findById(id);
        model.addAttribute("product", existingProduct);
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