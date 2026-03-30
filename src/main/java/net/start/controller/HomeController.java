package net.start.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class HomeController {

    // ================= HOME =================
    @GetMapping("/")
    public String index() {
        return "index";
    }

    // ================= KITCHEN =================
    @GetMapping("/kitchen")
    public String kitchen() {
        return "kitchen/dashboard";
    }

    // ================= MENU =================
    @GetMapping("/menu")
    public String menuList() {
        return "menu/list";
    }

    @GetMapping("/menu/new")
    public String menuNew() {
        return "menu/form";
    }

    @GetMapping("/menu/edit/{id}")
    public String menuEdit(@PathVariable("id") int id, Model model) {
        model.addAttribute("menuId", id);
        return "menu/form";
    }

    // ================= ORDERS =================
    @GetMapping("/orders/history")
    public String orderHistory() {
        return "orders/history";
    }

    @GetMapping("/orders/history/{id}")
    public String orderDetail(@PathVariable("id") int id, Model model) {
        model.addAttribute("orderId", id);
        return "orders/detail";
    }

    @GetMapping("/orders/{id}")
    public String orderForm(@PathVariable("id") int id, Model model) {
        model.addAttribute("orderId", id);
        return "orders/form";
    }

    // ================= TABLES =================
    @GetMapping("/tables")
    public String tablesList() {
        return "tables/list";
    }

    @GetMapping("/tables/new")
    public String tablesNew() {
        return "tables/form";
    }

    @GetMapping("/tables/edit/{id}")
    public String tablesEdit(@PathVariable("id") int id, Model model) {
        model.addAttribute("tableId", id);
        return "tables/form";
    }

    // ================= PROMOTIONS =================
    @GetMapping("/promotions")
    public String promotions() {
        return "promotions/list";
    }

    @GetMapping("/promotions/new")
    public String promotionNew() {
        return "promotions/form";
    }

    @GetMapping("/promotions/coupons")
    public String couponManagement() {
        return "promotions/coupons";
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
    public String promotionEdit(@PathVariable("id") int id, Model model) {
        model.addAttribute("promotionId", id);
        return "promotions/form";
    }

    // ================= PAYMENTS =================
    @GetMapping("/payments/checkout/table/{id}")
    public String paymentCheckout(@PathVariable("id") Integer id, Model model) {
        model.addAttribute("tableId", id);
        return "payments/checkout";
    }
}