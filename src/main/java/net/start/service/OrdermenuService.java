package net.start.service;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import net.start.dto.OrderItemSummary;
import net.start.model.OrderDetail;
import net.start.model.Ordermenu;
import net.start.model.Product;
import net.start.model.Tables;
import net.start.repository.OrderDetailRepository;
import net.start.repository.OrdermenuRepository;
import net.start.repository.ProductRepository;
import net.start.repository.TablesRepository;

@Service
public class OrdermenuService {

	@Autowired
	private OrdermenuRepository ordermenuRepository;

	@Autowired
	private OrderDetailRepository orderDetailRepository;

	@Autowired
	private ProductRepository productRepository;

	@Autowired
	private TablesRepository tablesRepository;

	@Transactional
	public Ordermenu createOrder(Integer tableId, Map<Integer, Integer> quantities) {
	    Tables table = tablesRepository.findById(tableId)
	            .orElseThrow(() -> new IllegalArgumentException("ไม่พบโต๊ะที่ระบุ"));

	    Ordermenu ordermenu;

	    // Check for an existing active order first
	    ordermenu = ordermenuRepository.findFirstByTables_TableIdAndOrderStatusNotOrderByOrderDateDesc(tableId, "paid")
	            .orElse(null);

	    if (ordermenu == null) {
	        // If no active order exists, we must create one regardless of the current table status
	        // (This handles manually set 'unavailable' or 'reserved' statuses)
	        ordermenu = new Ordermenu();
	        ordermenu.setTables(table);
	        ordermenu.setOrderDate(Timestamp.from(Instant.now()));
	        ordermenu.setOrderStatus("pending");
	        ordermenu.setTotalAmount(BigDecimal.ZERO);
	        
	        // Ensure table status is set to 'unavailable' if it wasn't already
	        if (!"unavailable".equalsIgnoreCase(table.getStatus())) {
	            table.setStatus("unavailable");
	            tablesRepository.save(table);
	        }
	        
	        ordermenu = ordermenuRepository.save(ordermenu);
	    }

	    BigDecimal currentTotal = ordermenu.getTotalAmount();
	    boolean hasItem = false;

	    for (Map.Entry<Integer, Integer> entry : quantities.entrySet()) {
	        Integer quantity = entry.getValue();
	        if (quantity == null || quantity <= 0) continue;

	        Product product = productRepository.findById(entry.getKey()).orElseThrow();

	        OrderDetail detail = new OrderDetail();
	        detail.setOrdermenu(ordermenu);
	        detail.setProduct(product);
	        detail.setQuantity(quantity);
	        detail.setUnitPrice(product.getProductPrice());
	        detail.setItemStatus("ordered");
	        orderDetailRepository.save(detail);

	        currentTotal = currentTotal.add(product.getProductPrice().multiply(BigDecimal.valueOf(quantity)));
	        hasItem = true;
	    }

	    if (!hasItem) {
	        throw new IllegalArgumentException("กรุณาเลือกเมนูอย่างน้อย 1 รายการ");
	    }

	    ordermenu.setTotalAmount(currentTotal);
	    return ordermenuRepository.save(ordermenu);
	}
	
	public List<Ordermenu> getActiveOrdersByTable(Integer tableId) {
	    return ordermenuRepository.findByTables_TableIdAndOrderStatusNot(tableId, "paid");
	}
	
	public List<OrderItemSummary> getAggregatedActiveOrders(Integer tableId) {
        List<Ordermenu> activeOrders = getActiveOrdersByTable(tableId);
        Map<Integer, OrderItemSummary> summaryMap = new LinkedHashMap<>();
        
        for (Ordermenu order : activeOrders) {
            for (OrderDetail detail : order.getOrderDetails()) {
                Product p = detail.getProduct();
                OrderItemSummary summary = summaryMap.computeIfAbsent(p.getProductId(), 
                    k -> new OrderItemSummary(p, 0, detail.getUnitPrice()));
                summary.setQuantity(summary.getQuantity() + detail.getQuantity());
            }
        }
        return new ArrayList<>(summaryMap.values());
    }

    public List<Ordermenu> getPaidOrderHistory() {
        return ordermenuRepository.findByOrderStatusOrderByOrderDateDesc("paid");
    }

    public Ordermenu findById(Integer id) {
        return ordermenuRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("ไม่พบออเดอร์ที่ระบุ: " + id));
    }

    public Page<Ordermenu> getPaidOrderHistoryPaginated(Pageable pageable) {
        return ordermenuRepository.findByOrderStatusOrderByOrderDateDesc("paid", pageable);
    }
    
    public BigDecimal calculateGrandTotal(List<Ordermenu> completedOrders) {
        return completedOrders.stream()
                .map(Ordermenu::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public BigDecimal calculateTotalRevenue() {
        List<Ordermenu> allPaid = ordermenuRepository.findByOrderStatusOrderByOrderDateDesc("paid");
        return allPaid.stream()
                .map(Ordermenu::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
    
    public Map<String, Integer> getGroupedDetails(Ordermenu order) {
        return order.getOrderDetails().stream()
                .collect(Collectors.groupingBy(
                    detail -> detail.getProduct().getProductName(),
                    Collectors.summingInt(OrderDetail::getQuantity)
                ));
    }

    public Map<String, Integer> getTopSellingProducts() {
        List<Ordermenu> allPaid = ordermenuRepository.findByOrderStatusOrderByOrderDateDesc("paid");
        return allPaid.stream()
                .flatMap(order -> order.getOrderDetails().stream())
                .collect(Collectors.groupingBy(
                    detail -> detail.getProduct().getProductName(),
                    Collectors.summingInt(OrderDetail::getQuantity)
                )).entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .limit(5)
                .collect(Collectors.toMap(
                    Map.Entry::getKey,
                    Map.Entry::getValue,
                    (e1, e2) -> e1,
                    LinkedHashMap::new
                ));
    }
}
