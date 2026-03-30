package net.start.controller;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import net.start.dto.OrderItemSummary;
import net.start.model.Ordermenu;
import net.start.model.Promotion;
import net.start.service.OrdermenuService;
import net.start.service.PaymentService;
import net.start.service.PromotionService;

@RestController
@RequestMapping("/api/payments")
@CrossOrigin(origins = "*")
public class PaymentController {

    private static final Logger logger = LoggerFactory.getLogger(PaymentController.class);

    @Autowired
    private OrdermenuService ordermenuService;

    @Autowired
    private PromotionService promotionService;

    @Autowired
    private PaymentService paymentService;

    @GetMapping("/checkout/table/{tableId}")
    public ResponseEntity<Map<String, Object>> showTableCheckout(@PathVariable("tableId") Integer tableId) {
        List<Ordermenu> activeOrders = ordermenuService.getActiveOrdersByTable(tableId);
        
        Map<String, Object> response = new HashMap<>();

        if (activeOrders.isEmpty()) {
            response.put("status", "error");
            response.put("message", "โต๊ะนี้ยังไม่มีรายการอาหาร");
            return ResponseEntity.badRequest().body(response);
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
            // Basic validation: must be within date range (optional, but good practice)
            
            if (p.getMinspend() != null && originalTotal.compareTo(p.getMinspend()) >= 0) {
                if ("percent".equalsIgnoreCase(p.getType()) && p.getValue() != null) {
                    BigDecimal percentDiscount = originalTotal.multiply(p.getValue()).divide(new BigDecimal(100));
                    discount = discount.add(percentDiscount);
                    messages.add("✅ ได้รับส่วนลด " + p.getValue() + "% (-" + percentDiscount + " ฿) [" + p.getName() + "]");
                } else if ("baht".equalsIgnoreCase(p.getType()) && p.getValue() != null) {
                    discount = discount.add(p.getValue());
                    messages.add("✅ ได้รับส่วนลด " + p.getValue() + " บาท [" + p.getName() + "]");
                } else if ("add".equalsIgnoreCase(p.getType()) && p.getFreeProduct() != null) {
                    messages.add("🎁 แถมฟรี: " + p.getFreeProduct().getProductName() + " x" + p.getQuantity() + " [" + p.getName() + "]");
                }
            }
        }

        BigDecimal finalTotal = originalTotal.subtract(discount);
        if (finalTotal.compareTo(BigDecimal.ZERO) < 0) {
            finalTotal = BigDecimal.ZERO;
        }

        response.put("status", "success");
        response.put("tableId", tableId);
        response.put("aggregatedOrders", aggregatedOrders);
        response.put("originalTotal", originalTotal);
        response.put("discount", discount);
        response.put("finalTotal", finalTotal);
        response.put("promoMessages", messages);
        response.put("allPromotions", promos);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/save/table/{tableId}")
    public ResponseEntity<Map<String, Object>> processTablePayment(
            @PathVariable("tableId") Integer tableId,
            @RequestBody Map<String, Object> payload) {
            
        Map<String, Object> response = new HashMap<>();
        
        try {
            // Retrieve finalTotal from JSON body safely
            Object finalTotalObj = payload.get("finalTotal");
            BigDecimal finalTotal = BigDecimal.ZERO;
            if (finalTotalObj != null) {
                finalTotal = new BigDecimal(finalTotalObj.toString());
            }
            
            paymentService.processTablePayment(tableId, finalTotal);
            
            response.put("status", "success");
            response.put("message", "ชำระเงินโต๊ะ " + tableId + " เรียบร้อยแล้ว");
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Payment process error", e);
            response.put("status", "error");
            response.put("message", "เกิดข้อผิดพลาดในการชำระเงิน: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PostMapping("/reset/{tableId}")
    public ResponseEntity<Map<String, Object>> resetTable(@PathVariable("tableId") Integer tableId) {
        Map<String, Object> response = new HashMap<>();
        try {
            paymentService.resetTableStatus(tableId);
            response.put("status", "success");
            response.put("message", "คืนโต๊ะ " + tableId + " เรียบร้อยแล้ว");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Reset table error", e);
            response.put("status", "error");
            response.put("message", "เกิดข้อผิดพลาด: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
}
