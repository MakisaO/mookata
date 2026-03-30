package net.start.service;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import net.start.model.Ordermenu;
import net.start.model.Payment;
import net.start.model.Tables;
import net.start.repository.OrdermenuRepository;
import net.start.repository.PaymentRepository;
import net.start.repository.TablesRepository;

@Service
public class PaymentService {

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private OrdermenuRepository ordermenuRepository;

    @Autowired
    private TablesRepository tablesRepository;

    @Transactional
    public void processTablePayment(Integer tableId, BigDecimal finalAmount) {
        Tables table = tablesRepository.findById(tableId).orElseThrow(() -> new IllegalArgumentException("Invalid Table ID"));
        List<Ordermenu> activeOrders = ordermenuRepository.findByTables_TableIdAndOrderStatusNot(tableId, "paid");
        
        if (activeOrders.isEmpty()) {
            throw new IllegalArgumentException("No active orders for this table.");
        }

        Payment payment = new Payment();
        // Link the payment to the first active order for simplicity
        payment.setOrdermenu(activeOrders.get(0));
        payment.setAmount(finalAmount);
        payment.setPaymentTime(Timestamp.from(Instant.now()));
        paymentRepository.save(payment);

        for (Ordermenu order : activeOrders) {
            order.setOrderStatus("paid");
            ordermenuRepository.save(order);
        }

        table.setStatus("available");
        tablesRepository.save(table);
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
    /**
     * Internal logic to calculate rewards for our own shop.
     * Moved from PaymentController to keep the controller slim.
     */
    public Map<String, Object> calculateInternalDiscounts(BigDecimal originalTotal, List<net.start.model.Promotion> promos) {
        BigDecimal discount = BigDecimal.ZERO;
        List<String> messages = new ArrayList<>();

        for (net.start.model.Promotion p : promos) {
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

        Map<String, Object> result = new HashMap<>();
        result.put("discount", discount);
        result.put("finalTotal", finalTotal);
        result.put("messages", messages);
        return result;
    }
}
