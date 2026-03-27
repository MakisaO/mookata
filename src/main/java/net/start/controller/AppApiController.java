package net.start.controller;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import net.start.dto.ApiMessageResponse;
import net.start.dto.CheckoutPageData;
import net.start.dto.KitchenDashboardData;
import net.start.dto.KitchenItemData;
import net.start.dto.KitchenRoundData;
import net.start.dto.MenuListData;
import net.start.dto.OrderCreateRequest;
import net.start.dto.OrderDetailData;
import net.start.dto.OrderDetailLine;
import net.start.dto.OrderHistoryData;
import net.start.dto.OrderHistoryEntry;
import net.start.dto.OrderPageData;
import net.start.dto.ProductSalesData;
import net.start.dto.ProductSalesEntry;
import net.start.dto.SummaryDashboardData;
import net.start.dto.SummaryProductStat;
import net.start.dto.TableRequest;
import net.start.model.OrderDetail;
import net.start.model.Ordermenu;
import net.start.model.Product;
import net.start.model.Promotion;
import net.start.model.Tables;
import net.start.service.CategoriesService;
import net.start.service.PromotionService;
import net.start.service.OrdermenuService;
import net.start.service.PaymentService;
import net.start.service.ProductService;
import net.start.service.TablesService;
import net.start.repository.OrderDetailRepository;

@RestController
@RequestMapping("/api")
public class AppApiController {

    @Autowired
    private TablesService tablesService;

    @Autowired
    private ProductService productService;

    @Autowired
    private CategoriesService categoriesService;

    @Autowired
    private OrdermenuService ordermenuService;

    @Autowired
    private PromotionService promotionService;

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private OrderDetailRepository orderDetailRepository;

    @GetMapping("/tables")
    public ResponseEntity<List<Tables>> getTables() {
        return ResponseEntity.ok(tablesService.findAll());
    }

    @GetMapping("/tables/{id}")
    public ResponseEntity<?> getTable(@PathVariable("id") Integer id) {
        try {
            return ResponseEntity.ok(tablesService.findById(id));
        } catch (Exception ex) {
            return ResponseEntity.badRequest().body(new ApiMessageResponse("Table not found"));
        }
    }

    @PostMapping("/tables")
    public ResponseEntity<?> createTable(@RequestBody TableRequest request) {
        Tables table = new Tables();
        table.setStatus(request.getStatus());
        return ResponseEntity.status(HttpStatus.CREATED).body(tablesService.save(table));
    }

    @PostMapping("/tables/{id}")
    public ResponseEntity<?> updateTable(@PathVariable("id") Integer id, @RequestBody TableRequest request) {
        Tables table = tablesService.findById(id);
        table.setStatus(request.getStatus());
        return ResponseEntity.ok(tablesService.save(table));
    }

    @PostMapping("/tables/{id}/delete")
    public ResponseEntity<ApiMessageResponse> deleteTable(@PathVariable("id") Integer id) {
        tablesService.deleteById(id);
        return ResponseEntity.ok(new ApiMessageResponse("Deleted table"));
    }

    @GetMapping("/menu")
    public ResponseEntity<MenuListData> getMenu(
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "sortField", defaultValue = "productId") String sortField,
            @RequestParam(value = "sortDir", defaultValue = "asc") String sortDir) {
        Page<Product> productPage = productService.findPaginated(keyword, page, 10, sortField, sortDir);
        return ResponseEntity.ok(new MenuListData(
                productPage.getContent(),
                page,
                productPage.getTotalPages(),
                productPage.getTotalElements(),
                keyword,
                sortField,
                sortDir));
    }

    @GetMapping("/categories")
    public ResponseEntity<?> getCategories() {
        return ResponseEntity.ok(categoriesService.findAll());
    }

    @PostMapping("/products")
    public ResponseEntity<?> createProduct(@RequestBody Product product) {
        productService.save(product);
        return ResponseEntity.status(HttpStatus.CREATED).body(product);
    }

    @PostMapping("/products/{id}")
    public ResponseEntity<?> updateProduct(@PathVariable("id") Integer id, @RequestBody Product productRequest) {
        Product product = productService.findById(id);
        product.setProductName(productRequest.getProductName());
        product.setProductDetail(productRequest.getProductDetail());
        product.setProductPrice(productRequest.getProductPrice());
        product.setProductStatus(productRequest.getProductStatus());
        product.setCategories(productRequest.getCategories());
        productService.save(product);
        return ResponseEntity.ok(product);
    }

    @PostMapping("/products/{id}/delete")
    public ResponseEntity<ApiMessageResponse> deleteProduct(@PathVariable("id") Integer id) {
        productService.deleteById(id);
        return ResponseEntity.ok(new ApiMessageResponse("Deleted product"));
    }

    @GetMapping("/orders/table/{tableId}")
    public ResponseEntity<?> getOrderPageData(@PathVariable("tableId") Integer tableId) {
        try {
            OrderPageData response = new OrderPageData(
                    tableId,
                    productService.findAvailable(),
                    ordermenuService.getAggregatedActiveOrders(tableId));
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(new ApiMessageResponse(ex.getMessage()));
        }
    }

    @PostMapping("/orders")
    public ResponseEntity<ApiMessageResponse> createOrder(@RequestBody OrderCreateRequest request) {
        try {
            ordermenuService.createOrder(request.getTableId(), request.getQuantities());
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(new ApiMessageResponse("บันทึกรายการอาหารเรียบร้อยแล้ว"));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(new ApiMessageResponse(ex.getMessage()));
        }
    }

    @GetMapping("/orders/history")
    public ResponseEntity<OrderHistoryData> getOrderHistory(
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size) {
        Page<Ordermenu> orderPage = ordermenuService.getPaidOrderHistoryPaginated(PageRequest.of(page, size));
        List<OrderHistoryEntry> entries = orderPage.getContent().stream().map(order -> {
            Map<String, Integer> grouped = ordermenuService.getGroupedDetails(order);
            List<String> items = grouped.entrySet().stream()
                    .map(entry -> entry.getKey() + " x" + entry.getValue())
                    .collect(Collectors.toList());
            return new OrderHistoryEntry(
                    order.getOrderId(),
                    order.getTables() != null ? order.getTables().getTableId() : null,
                    order.getOrderDate() != null ? order.getOrderDate().toString() : "",
                    order.getTotalAmount(),
                    items);
        }).collect(Collectors.toList());

        return ResponseEntity.ok(new OrderHistoryData(
                page,
                orderPage.getTotalPages(),
                orderPage.getTotalElements(),
                size,
                ordermenuService.calculateTotalRevenue(),
                entries));
    }

    @GetMapping("/orders/history/{id}")
    public ResponseEntity<?> getOrderDetail(@PathVariable("id") Integer id) {
        try {
            Ordermenu order = ordermenuService.findById(id);
            List<OrderDetailLine> items = order.getOrderDetails().stream()
                    .map(detail -> new OrderDetailLine(
                            detail.getProduct().getProductName(),
                            detail.getQuantity(),
                            detail.getUnitPrice(),
                            detail.getUnitPrice().multiply(java.math.BigDecimal.valueOf(detail.getQuantity()))))
                    .collect(Collectors.toList());

            return ResponseEntity.ok(new OrderDetailData(
                    order.getOrderId(),
                    order.getTables() != null ? order.getTables().getTableId() : null,
                    order.getOrderDate() != null ? order.getOrderDate().toString() : "",
                    order.getOrderStatus(),
                    order.getTotalAmount(),
                    items));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(new ApiMessageResponse(ex.getMessage()));
        }
    }

    @PostMapping("/promotions")
    public ResponseEntity<?> createPromotion(@RequestBody Promotion promotion) {
        promotionService.save(promotion);
        return ResponseEntity.status(HttpStatus.CREATED).body(promotion);
    }

    @PostMapping("/promotions/{id}")
    public ResponseEntity<?> updatePromotion(@PathVariable("id") Integer id, @RequestBody Promotion promotion) {
        promotion.setPromotionId(id);
        promotionService.save(promotion);
        return ResponseEntity.ok(promotion);
    }

    @PostMapping("/promotions/{id}/delete")
    public ResponseEntity<ApiMessageResponse> deletePromotion(@PathVariable("id") Integer id) {
        promotionService.deleteById(id);
        return ResponseEntity.ok(new ApiMessageResponse("Deleted promotion"));
    }

    @GetMapping("/kitchen")
    public ResponseEntity<KitchenDashboardData> getKitchenDashboard() {
        List<OrderDetail> pendingItems = orderDetailRepository.findByItemStatusInOrderByDetailIdAsc(List.of("ordered", "cooking"));
        Map<Ordermenu, List<OrderDetail>> grouped = pendingItems.stream().collect(Collectors.groupingBy(
                OrderDetail::getOrdermenu,
                java.util.LinkedHashMap::new,
                Collectors.toList()));

        List<KitchenRoundData> rounds = grouped.entrySet().stream().map(entry -> {
            List<OrderDetail> items = entry.getValue();
            boolean hasOrdered = items.stream().anyMatch(item -> "ordered".equals(item.getItemStatus()));
            boolean allOrdered = items.stream().allMatch(item -> "ordered".equals(item.getItemStatus()));
            String actionLabel = allOrdered ? "Start All"
                    : hasOrdered ? "Move Ordered To Cooking"
                    : "Serve All";

            List<KitchenItemData> itemData = items.stream().map(item -> new KitchenItemData(
                    item.getDetailId(),
                    item.getProduct() != null ? item.getProduct().getProductName() : "",
                    item.getQuantity(),
                    item.getItemStatus())).collect(Collectors.toList());

            return new KitchenRoundData(
                    entry.getKey().getOrderId(),
                    entry.getKey().getTables() != null ? entry.getKey().getTables().getTableId() : null,
                    entry.getKey().getOrderDate() != null ? entry.getKey().getOrderDate().toString() : "",
                    actionLabel,
                    itemData);
        }).collect(Collectors.toList());

        return ResponseEntity.ok(new KitchenDashboardData(rounds));
    }

    @PostMapping("/kitchen/items/{detailId}/cook")
    public ResponseEntity<ApiMessageResponse> markCooking(@PathVariable("detailId") Integer detailId) {
        OrderDetail detail = orderDetailRepository.findById(detailId).orElseThrow(() -> new IllegalArgumentException("Item not found"));
        detail.setItemStatus("cooking");
        orderDetailRepository.save(detail);
        return ResponseEntity.ok(new ApiMessageResponse("Updated item to cooking"));
    }

    @PostMapping("/kitchen/items/{detailId}/serve")
    public ResponseEntity<ApiMessageResponse> markServed(@PathVariable("detailId") Integer detailId) {
        OrderDetail detail = orderDetailRepository.findById(detailId).orElseThrow(() -> new IllegalArgumentException("Item not found"));
        detail.setItemStatus("served");
        orderDetailRepository.save(detail);
        return ResponseEntity.ok(new ApiMessageResponse("Updated item to served"));
    }

    @PostMapping("/kitchen/orders/{orderId}/mass-update")
    public ResponseEntity<ApiMessageResponse> massUpdateKitchenOrder(@PathVariable("orderId") Integer orderId) {
        List<OrderDetail> items = orderDetailRepository.findByItemStatusInOrderByDetailIdAsc(List.of("ordered", "cooking"))
                .stream()
                .filter(item -> item.getOrdermenu().getOrderId().equals(orderId))
                .collect(Collectors.toList());

        if (items.isEmpty()) {
            return ResponseEntity.ok(new ApiMessageResponse("No pending items"));
        }

        boolean hasOrdered = items.stream().anyMatch(item -> "ordered".equals(item.getItemStatus()));
        if (hasOrdered) {
            items.stream().filter(item -> "ordered".equals(item.getItemStatus())).forEach(item -> item.setItemStatus("cooking"));
            orderDetailRepository.saveAll(items);
            return ResponseEntity.ok(new ApiMessageResponse("Moved ordered items to cooking"));
        }

        items.forEach(item -> item.setItemStatus("served"));
        orderDetailRepository.saveAll(items);
        return ResponseEntity.ok(new ApiMessageResponse("Moved items to served"));
    }

    @GetMapping("/payments/checkout/table/{tableId}")
    public ResponseEntity<?> getCheckoutPageData(@PathVariable("tableId") Integer tableId) {
        try {
            return ResponseEntity.ok(paymentService.getCheckoutPageData(tableId));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(new ApiMessageResponse(ex.getMessage()));
        }
    }

    @PostMapping("/payments/checkout/table/{tableId}")
    public ResponseEntity<ApiMessageResponse> processPayment(@PathVariable("tableId") Integer tableId) {
        try {
            paymentService.processTablePayment(tableId);
            return ResponseEntity.ok(new ApiMessageResponse("ชำระเงินเรียบร้อยแล้ว"));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(new ApiMessageResponse(ex.getMessage()));
        }
    }

    @PostMapping("/tables/{tableId}/reset")
    public ResponseEntity<ApiMessageResponse> resetTable(@PathVariable("tableId") Integer tableId) {
        try {
            paymentService.resetTableStatus(tableId);
            return ResponseEntity.ok(new ApiMessageResponse("คืนสถานะโต๊ะเรียบร้อยแล้ว"));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(new ApiMessageResponse(ex.getMessage()));
        }
    }

    @GetMapping("/summary")
    public ResponseEntity<SummaryDashboardData> getSummaryDashboard() {
        List<SummaryProductStat> topProducts = ordermenuService.getTopSellingProducts().entrySet().stream()
                .map(entry -> new SummaryProductStat(entry.getKey().getProductId(), entry.getKey().getProductName(), entry.getValue()))
                .collect(Collectors.toList());

        List<SummaryProductStat> leastProducts = ordermenuService.getLeastSellingProducts().entrySet().stream()
                .map(entry -> new SummaryProductStat(entry.getKey().getProductId(), entry.getKey().getProductName(), entry.getValue()))
                .collect(Collectors.toList());

        return ResponseEntity.ok(new SummaryDashboardData(
                ordermenuService.calculateTotalRevenue(),
                ordermenuService.getPaidOrderHistory().size(),
                topProducts,
                leastProducts));
    }

    @GetMapping("/summary/product/{id}")
    public ResponseEntity<ProductSalesData> getProductSales(@PathVariable("id") Integer id) {
        Product product = productService.findById(id);
        List<OrderDetail> history = ordermenuService.getProductSalesHistory(id);
        List<ProductSalesEntry> entries = history.stream().map(detail -> new ProductSalesEntry(
                detail.getOrdermenu().getOrderId(),
                detail.getOrdermenu().getTables() != null ? detail.getOrdermenu().getTables().getTableId() : null,
                detail.getOrdermenu().getOrderDate() != null ? detail.getOrdermenu().getOrderDate().toString() : "",
                detail.getQuantity(),
                detail.getUnitPrice(),
                detail.getUnitPrice().multiply(java.math.BigDecimal.valueOf(detail.getQuantity()))))
                .collect(Collectors.toList());

        int totalSold = history.stream().mapToInt(OrderDetail::getQuantity).sum();
        java.math.BigDecimal totalRevenue = history.stream()
                .map(detail -> detail.getUnitPrice().multiply(java.math.BigDecimal.valueOf(detail.getQuantity())))
                .reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add);

        return ResponseEntity.ok(new ProductSalesData(
                product.getProductId(),
                product.getProductName(),
                product.getProductDetail(),
                totalSold,
                totalRevenue,
                entries));
    }
}
