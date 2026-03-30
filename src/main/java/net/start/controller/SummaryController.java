package net.start.controller;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import net.start.model.Ordermenu;
import net.start.model.OrderDetail;
import net.start.model.Product;
import net.start.service.OrdermenuService;
import net.start.service.ProductService;

@RestController
@RequestMapping("/api/summary")
public class SummaryController {

    @Autowired
    private OrdermenuService ordermenuService;

    @Autowired
    private ProductService productService;

    @GetMapping("")
    public ResponseEntity<Map<String, Object>> getSummary() {
        BigDecimal totalRevenue = ordermenuService.calculateTotalRevenue();
        List<Ordermenu> allPaidOrders = ordermenuService.getPaidOrderHistory();
        int totalOrders = allPaidOrders.size();
        
        Map<Product, Integer> topProductsMap = ordermenuService.getTopSellingProducts();
        Map<Product, Integer> leastProductsMap = ordermenuService.getLeastSellingProducts();

        List<Map<String, Object>> topProducts = topProductsMap.entrySet().stream()
            .map(entry -> {
                Map<String, Object> item = new HashMap<>();
                item.put("product", entry.getKey());
                item.put("quantity", entry.getValue());
                return item;
            }).toList();

        List<Map<String, Object>> leastProducts = leastProductsMap.entrySet().stream()
            .map(entry -> {
                Map<String, Object> item = new HashMap<>();
                item.put("product", entry.getKey());
                item.put("quantity", entry.getValue());
                return item;
            }).toList();

        Map<String, Object> response = new HashMap<>();
        response.put("totalRevenue", totalRevenue);
        response.put("totalOrders", totalOrders);
        response.put("topProducts", topProducts);
        response.put("leastProducts", leastProducts);
        
        return ResponseEntity.ok(response);
    }

    @GetMapping("/product/{id}")
    public ResponseEntity<Map<String, Object>> getProductSales(@PathVariable("id") Integer id) {
        Product product = productService.findById(id);
        List<OrderDetail> salesHistory = ordermenuService.getProductSalesHistory(id);
        List<Map<String, Object>> salesHistoryResponse = salesHistory.stream()
            .map(detail -> {
                Map<String, Object> item = new HashMap<>();
                item.put("orderDate", detail.getOrdermenu().getOrderDate());
                item.put("tableId", detail.getOrdermenu().getTables() != null
                        ? detail.getOrdermenu().getTables().getTableId()
                        : null);
                item.put("orderId", detail.getOrdermenu().getOrderId());
                item.put("quantity", detail.getQuantity());
                item.put("unitPrice", detail.getUnitPrice());
                item.put("lineTotal", detail.getUnitPrice().multiply(BigDecimal.valueOf(detail.getQuantity())));
                return item;
            }).toList();

        int totalSold = salesHistory.stream().mapToInt(OrderDetail::getQuantity).sum();
        BigDecimal totalRevenue = salesHistory.stream()
                .map(d -> d.getUnitPrice().multiply(BigDecimal.valueOf(d.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Map<String, Object> response = new HashMap<>();
        response.put("product", product);
        response.put("salesHistory", salesHistoryResponse);
        response.put("totalSold", totalSold);
        response.put("totalRevenue", totalRevenue);

        return ResponseEntity.ok(response);
    }
}
