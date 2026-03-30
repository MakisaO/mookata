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
import net.start.service.PartnerCouponService;
import java.util.Collections;
import java.util.Random;
import java.util.stream.Collectors;

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

    @Autowired
    private PartnerCouponService partnerCouponService;

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

        List<Promotion> promos = promotionService.findAll();
        Map<String, Object> internalReward = paymentService.calculateInternalDiscounts(originalTotal, promos);
        
        BigDecimal discount = (BigDecimal) internalReward.get("discount");
        BigDecimal finalTotal = (BigDecimal) internalReward.get("finalTotal");
        List<String> messages = (List<String>) internalReward.get("messages");

        response.put("status", "success");
        response.put("tableId", tableId);
        response.put("aggregatedOrders", aggregatedOrders);
        response.put("originalTotal", originalTotal);
        response.put("discount", discount);
        response.put("finalTotal", finalTotal);
        response.put("promoMessages", messages);
        response.put("allPromotions", promos);

        // Fetch Partner Promotions (Service handled)
        try {
            List<Map<String, Object>> partnerPromoData = partnerCouponService.getEligiblePartnerRewards(finalTotal.doubleValue());
            response.put("partnerPromotions", partnerPromoData);
        } catch (Exception e) {
            logger.error("Error fetching partner promos", e);
            response.put("partnerPromotions", new ArrayList<>());
        }

        return ResponseEntity.ok(response);
    }

    @PostMapping("/save/table/{tableId}")
    public ResponseEntity<Map<String, Object>> processTablePayment(
            @PathVariable("tableId") Integer tableId,
            @RequestBody Map<String, Object> payload) {
            
        Map<String, Object> response = new HashMap<>();
        
        try {
            Object finalTotalObj = payload.get("finalTotal");
            BigDecimal finalTotal = (finalTotalObj != null) ? new BigDecimal(finalTotalObj.toString()) : BigDecimal.ZERO;
            
            // 1. Process Internal Payment
            paymentService.processTablePayment(tableId, finalTotal);
            
            // 2. Process Partner Coupon (Service handled)
            try {
                PartnerCouponService.CouponData coupon = partnerCouponService.issueRandomQualifiedCoupon(finalTotal.doubleValue());
                if (coupon != null) {
                    response.put("partnerCoupon", coupon);
                }
            } catch (Exception e) {
                logger.error("Error issuing partner coupon", e);
            }
            
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
