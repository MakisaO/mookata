package net.start.service;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;

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
}
