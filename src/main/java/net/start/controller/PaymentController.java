package net.start.controller;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import net.start.dto.OrderItemSummary;
import net.start.model.Ordermenu;
import net.start.model.Promotion;
import net.start.service.OrdermenuService;
import net.start.service.PaymentService;
import net.start.service.PromotionService;

@Controller
@RequestMapping("/payments")
public class PaymentController {

    @Autowired
    private OrdermenuService ordermenuService;

    @Autowired
    private PromotionService promotionService;

    @Autowired
    private PaymentService paymentService;

    @GetMapping("/checkout/table/{tableId}")
    public String showTableCheckout(@PathVariable("tableId") Integer tableId, Model model) {
        List<Ordermenu> activeOrders = ordermenuService.getActiveOrdersByTable(tableId);
        
        if (activeOrders.isEmpty()) {
            return "redirect:/tables";
        }
        
        List<OrderItemSummary> aggregatedOrders = ordermenuService.getAggregatedActiveOrders(tableId);
        
        BigDecimal originalTotal = BigDecimal.ZERO;
        for (OrderItemSummary summary : aggregatedOrders) {
            originalTotal = originalTotal.add(summary.getTotalPrice() != null ? summary.getTotalPrice() : BigDecimal.ZERO);
        }

        BigDecimal discount = BigDecimal.ZERO;
        List<String> messages = new ArrayList<>();

        List<Promotion> promos = promotionService.findAll();
        for (Promotion p : promos) {
            if (p.getMinspend() != null && originalTotal.compareTo(p.getMinspend()) >= 0) {
                if ("discount".equalsIgnoreCase(p.getType()) && p.getValue() != null) {
                    discount = discount.add(p.getValue());
                    messages.add("✅ ได้รับส่วนลด " + p.getValue() + " บาท (" + p.getName() + ")");
                } else if ("free_item".equalsIgnoreCase(p.getType()) && p.getFreeProduct() != null) {
                    messages.add("🎁 แถมฟรี: " + p.getFreeProduct().getProductName() + " (" + p.getName() + ")");
                }
            }
        }

        BigDecimal finalTotal = originalTotal.subtract(discount);
        if (finalTotal.compareTo(BigDecimal.ZERO) < 0) {
            finalTotal = BigDecimal.ZERO;
        }

        model.addAttribute("tableId", tableId);
        model.addAttribute("aggregatedOrders", aggregatedOrders);
        model.addAttribute("originalTotal", originalTotal);
        model.addAttribute("discount", discount);
        model.addAttribute("finalTotal", finalTotal);
        model.addAttribute("promoMessages", messages);
        model.addAttribute("allPromotions", promos);

        return "payments/checkout";
    }

    @PostMapping("/save/table/{tableId}")
    public String processTablePayment(@PathVariable("tableId") Integer tableId,
            @RequestParam("finalTotal") BigDecimal finalTotal, 
            RedirectAttributes redirectAttributes) {

        try {
            paymentService.processTablePayment(tableId, finalTotal);
            redirectAttributes.addFlashAttribute("successMessage", "ชำระเงินโต๊ะ " + tableId + " เรียบร้อยแล้ว");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "เกิดข้อผิดพลาดในการชำระเงิน: " + e.getMessage());
            return "redirect:/payments/checkout/table/" + tableId;
        }

        return "redirect:/tables";
    }
}
