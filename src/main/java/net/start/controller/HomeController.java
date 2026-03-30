package net.start.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller // สำคัญ: ต้องใช้ @Controller เพื่อคืนค่าเป็นหน้า View (HTML)
public class HomeController {

    @GetMapping("/") // เข้าหน้าแรกด้วย URL: http://localhost:8080/
    public String index() {
        return "index";
    }

    // เพิ่มหน้าต่างสำหรับเข้าหน้า Kitchen Dashboard
    @GetMapping("/kitchen")
    public String kitchen() {
        return "kitchen/dashboard"; // ดึงไฟล์จาก src/main/resources/templates/kitchen/dashboard.html
    }

    @GetMapping("/menu")
    public String menuList() {
        return "menu/list";
    }

    @GetMapping("/menu/new")
    public String menuNew() {
        return "menu/form";
    }

    @GetMapping("/menu/edit/{id}")
    public String menuEdit() {
        return "menu/form";
    }

    @GetMapping("/orders/history")
    public String orderHistory() {
        return "orders/history";
    }

    @GetMapping("/orders/history/{id}")
    public String orderDetail() {
        return "orders/detail";
    }

    @GetMapping("/orders/{id}")
    public String orderForm() {
        return "orders/form"; // ดึงไฟล์จาก src/main/resources/templates/orders/form.html
    }

    @GetMapping("/tables")
    public String tablesList() {
        return "tables/list";
    }

    @GetMapping("/tables/new")
    public String tablesNew() {
        return "tables/form";
    }

    @GetMapping("/tables/edit/{id}")
    public String tablesEdit() {
        return "tables/form";
    }
    @GetMapping("/promotions")
    public String promotions() {
        return "promotions/list";
    }

    @GetMapping("/promotions/new")
    public String promotionNew() {
        return "promotions/form";
    }

    @GetMapping("/summary")
    public String summary() {
        return "summary";
    }

    @GetMapping("/summary/product/{id}")
    public String summaryProduct() {
        return "summary/product_sales";
    }

    @GetMapping("/promotions/edit/{id}")
    public String promotionEdit() {
        return "promotions/form";
    }
    
    @GetMapping("/payments/checkout/table/{id}")
    public String paymentCheckout(@PathVariable("id") Integer id, Model model) {
        model.addAttribute("tableId", id);
        return "payments/checkout";
    }
}
