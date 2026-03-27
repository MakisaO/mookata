package net.start.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import net.start.dto.CheckoutPageData;
import net.start.dto.OrderItemSummary;
import net.start.model.Ordermenu;
import net.start.model.Payment;
import net.start.model.Promotion;
import net.start.model.Tables;
import net.start.repository.OrdermenuRepository;
import net.start.repository.PaymentRepository;
import net.start.repository.PromotionRepository;
import net.start.repository.TablesRepository;

@Service
public class PaymentService {

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private OrdermenuRepository ordermenuRepository;

    @Autowired
    private TablesRepository tablesRepository;

    @Autowired
    private PromotionRepository promotionRepository;

    @Transactional
    public void processTablePayment(Integer tableId) {
        Tables table = tablesRepository.findById(tableId).orElseThrow(() -> new IllegalArgumentException("Invalid Table ID"));
        List<Ordermenu> activeOrders = ordermenuRepository.findByTables_TableIdAndOrderStatusNot(tableId, "paid");
        
        if (activeOrders.isEmpty()) {
            throw new IllegalArgumentException("No active orders for this table.");
        }

        CheckoutPageData checkout = getCheckoutPageData(tableId);

        Payment payment = new Payment();
        // Link the payment to the first active order for simplicity
        payment.setOrdermenu(activeOrders.get(0));
        payment.setAmount(checkout.finalTotal());
        payment.setPaymentTime(Timestamp.from(Instant.now()));
        paymentRepository.save(payment);

        for (Ordermenu order : activeOrders) {
            order.setOrderStatus("paid");
            ordermenuRepository.save(order);
        }

        table.setStatus("available");
        tablesRepository.save(table);
    }

    @Transactional(readOnly = true)
    public CheckoutPageData getCheckoutPageData(Integer tableId) {
        List<Ordermenu> activeOrders = ordermenuRepository.findByTables_TableIdAndOrderStatusNot(tableId, "paid");

        if (activeOrders.isEmpty()) {
            throw new IllegalArgumentException("No active orders for this table.");
        }

        List<OrderItemSummary> aggregatedOrders = new java.util.ArrayList<>();
        java.util.Map<Integer, OrderItemSummary> summaryMap = new java.util.LinkedHashMap<>();
        for (Ordermenu order : activeOrders) {
            for (net.start.model.OrderDetail detail : order.getOrderDetails()) {
                OrderItemSummary summary = summaryMap.computeIfAbsent(
                        detail.getProduct().getProductId(),
                        key -> new OrderItemSummary(detail.getProduct(), 0, detail.getUnitPrice()));
                summary.setQuantity(summary.getQuantity() + detail.getQuantity());
            }
        }
        aggregatedOrders.addAll(summaryMap.values());

        BigDecimal originalTotal = aggregatedOrders.stream()
                .map(OrderItemSummary::getTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal discount = BigDecimal.ZERO;
        List<String> messages = new ArrayList<>();
        Instant now = Instant.now();

        for (Promotion promotion : promotionRepository.findAll()) {
            if (promotion.getStartDate() != null && promotion.getStartDate().toInstant().isAfter(now)) {
                continue;
            }
            if (promotion.getEndDate() != null && promotion.getEndDate().toInstant().isBefore(now)) {
                continue;
            }
            if (promotion.getMinspend() != null && originalTotal.compareTo(promotion.getMinspend()) < 0) {
                continue;
            }

            if ("percent".equalsIgnoreCase(promotion.getType()) && promotion.getPercent() != null) {
                BigDecimal percentDiscount = originalTotal
                        .multiply(promotion.getPercent())
                        .divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
                discount = discount.add(percentDiscount);
                messages.add("ส่วนลด " + promotion.getPercent() + "% [" + promotion.getName() + "]");
            } else if ("baht".equalsIgnoreCase(promotion.getType()) && promotion.getValue() != null) {
                discount = discount.add(promotion.getValue());
                messages.add("ส่วนลด " + promotion.getValue() + " บาท [" + promotion.getName() + "]");
            } else if ("add".equalsIgnoreCase(promotion.getType()) && promotion.getFreeProduct() != null) {
                messages.add("แถมฟรี " + promotion.getFreeProduct().getProductName() + " x"
                        + (promotion.getQuantity() != null ? promotion.getQuantity() : 1)
                        + " [" + promotion.getName() + "]");
            }
        }

        BigDecimal finalTotal = originalTotal.subtract(discount);
        if (finalTotal.compareTo(BigDecimal.ZERO) < 0) {
            finalTotal = BigDecimal.ZERO;
        }

        return new CheckoutPageData(tableId, aggregatedOrders, originalTotal, discount, finalTotal, messages);
    }

    @Transactional
    public void resetTableStatus(Integer tableId) {
        Tables table = tablesRepository.findById(tableId).orElseThrow(() -> new IllegalArgumentException("Invalid Table ID"));
        List<Ordermenu> activeOrders = ordermenuRepository.findByTables_TableIdAndOrderStatusNot(tableId, "paid");
        
        if (!activeOrders.isEmpty()) {
            throw new IllegalArgumentException("Cannot reset table with active orders. Please proceed to checkout.");
        }

        table.setStatus("available");
        tablesRepository.save(table);
    }
}
