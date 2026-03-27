package net.start.controller;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import net.start.model.Ordermenu;
import net.start.model.Product;
import net.start.model.Tables;
import net.start.service.CategoriesService; // 🌟 1. นำเข้า CategoriesService
import net.start.service.OrdermenuService;
import net.start.service.ProductService;
import net.start.service.TablesService;

@Controller
@RequestMapping("/orders")
public class OrderController {

    @Autowired
    private TablesService tablesService;

    @Autowired
    private ProductService productService;

    @Autowired
    private OrdermenuService ordermenuService;

    @Autowired
    private CategoriesService categoriesService; // 🌟 2. Inject CategoriesService

    // สำหรับการเข้าหน้าสั่งอาหารใหม่แบบยังไม่ระบุโต๊ะ (ถ้ามีการใช้งาน)
    @GetMapping("/new")
    public String showOrderForm(
            @RequestParam(value = "categoryId", required = false) Integer categoryId, // 🌟 รับ categoryId
            Model model) {
        
        List<Tables> tables = tablesService.findAll();
        
        // 🌟 ดึงสินค้าเฉพาะที่ "พร้อมขาย" และ "ตรงกับหมวดหมู่ที่เลือก" (ถ้าไม่ได้เลือก ดึงทั้งหมด)
        List<Product> products = productService.findAvailableByCategoryId(categoryId);

        model.addAttribute("tables", tables);
        model.addAttribute("products", products);
        
        // 🌟 ส่งข้อมูลหมวดหมู่ไปสร้างปุ่ม
        model.addAttribute("categories", categoriesService.findAll());
        model.addAttribute("activeCategoryId", categoryId);
        
        return "orders/form";
    }
    
    // ... (ส่วนของ /history และ /history/{id} คงเดิม ไม่มีการแก้ไข) ...
    @GetMapping("/history")
    public String showOrderHistory(
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            Model model) {
        
        Page<Ordermenu> orderPage = ordermenuService.getPaidOrderHistoryPaginated(PageRequest.of(page, size));
        BigDecimal grandTotal = ordermenuService.calculateTotalRevenue();

        Map<Integer, Map<String, Integer>> summaryMap = orderPage.getContent().stream()
            .collect(Collectors.toMap(
                Ordermenu::getOrderId, 
                order -> ordermenuService.getGroupedDetails(order)
            ));

        model.addAttribute("orders", orderPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", orderPage.getTotalPages());
        model.addAttribute("totalItems", orderPage.getTotalElements());
        model.addAttribute("pageSize", size);
        model.addAttribute("grandTotal", grandTotal);
        model.addAttribute("summaryMap", summaryMap);

        return "orders/history";
    }

    @GetMapping("/history/{id}")
    public String showOrderDetail(@PathVariable("id") Integer id, Model model) {
        Ordermenu order = ordermenuService.findById(id);
        model.addAttribute("order", order);
        model.addAttribute("groupedDetails", ordermenuService.getGroupedDetails(order));
        return "orders/detail";
    }
    
    // 🌟 3. อัปเดตเมธอดหลักที่ใช้เปิดหน้าสั่งอาหาร (ระบุโต๊ะแล้ว)
    @GetMapping("/{id}")
    public String showOrderFormByTable(
            @PathVariable("id") Integer id, 
            @RequestParam(value = "categoryId", required = false) Integer categoryId, // 🌟 รับ categoryId
            Model model) {
        
        model.addAttribute("selectedTableId", id);
        
        // 🌟 ดึงสินค้าเฉพาะที่ "พร้อมขาย" และ "ตรงกับหมวดหมู่ที่เลือก"
        model.addAttribute("products", productService.findAvailableByCategoryId(categoryId));
        
        model.addAttribute("aggregatedOrders", ordermenuService.getAggregatedActiveOrders(id));
        
        // 🌟 ส่งข้อมูลหมวดหมู่ไปสร้างปุ่มบนหน้าเว็บ
        model.addAttribute("categories", categoriesService.findAll());
        model.addAttribute("activeCategoryId", categoryId);
        
        return "orders/form";
    }
    

    @PostMapping("/save")
    public String saveOrder(@RequestParam("tableId") Integer tableId,
            @RequestParam Map<String, String> requestParams,
            RedirectAttributes redirectAttributes) {
        Map<Integer, Integer> quantities = new LinkedHashMap<>();

        for (Map.Entry<String, String> entry : requestParams.entrySet()) {
            String key = entry.getKey();
            if (!key.startsWith("qty_")) {
                continue;
            }

            String value = entry.getValue();
            if (value == null || value.isBlank()) {
                continue;
            }

            int quantity = Integer.parseInt(value);
            if (quantity <= 0) {
                continue;
            }

            int productId = Integer.parseInt(key.substring(4));
            quantities.put(productId, quantity);
        }

        try {
            ordermenuService.createOrder(tableId, quantities);
            redirectAttributes.addFlashAttribute("successMessage", "บันทึกคำสั่งซื้อเรียบร้อยแล้ว");
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }

        return "redirect:/orders/" + tableId;
    }
}