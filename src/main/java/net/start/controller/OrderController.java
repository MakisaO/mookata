package net.start.controller;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import net.start.model.Ordermenu;
import net.start.model.Product;
import net.start.model.Tables;
import net.start.service.OrdermenuService;
import net.start.service.ProductService;
import net.start.service.TablesService;

@RestController
@RequestMapping("/api/orders") // เปลี่ยน Path เป็น /api/orders
public class OrderController {

	@Autowired
	private TablesService tablesService;

	@Autowired
	private ProductService productService;

	@Autowired
	private OrdermenuService ordermenuService;

	// สร้าง DTO ภายใน (Inner Class) สำหรับรับค่าตอนบันทึกคำสั่งซื้อเป็น JSON
	public static class OrderRequest {
		private Integer tableId;
		private Map<Integer, Integer> quantities; // Key: productId, Value: quantity

		public Integer getTableId() {
			return tableId;
		}

		public void setTableId(Integer tableId) {
			this.tableId = tableId;
		}

		public Map<Integer, Integer> getQuantities() {
			return quantities;
		}

		public void setQuantities(Map<Integer, Integer> quantities) {
			this.quantities = quantities;
		}
	}

	// 1. ดึงข้อมูลสำหรับหน้าฟอร์มสร้างออเดอร์ (โต๊ะทั้งหมด + สินค้าทั้งหมด)
	@GetMapping("/new")
	public ResponseEntity<Map<String, Object>> showOrderForm() {
		List<Tables> tables = tablesService.findAll();
		List<Product> products = productService.findAvailable();

		Map<String, Object> response = new HashMap<>();
		response.put("tables", tables);
		response.put("products", products);

		return ResponseEntity.ok(response);
	}

	// 2. ดึงประวัติการสั่งซื้อ (หน้า History)
	@GetMapping("/history")
	public ResponseEntity<Map<String, Object>> showOrderHistory(
			@RequestParam(value = "page", defaultValue = "0") int page,
			@RequestParam(value = "size", defaultValue = "10") int size) {

		Page<Ordermenu> orderPage = ordermenuService.getPaidOrderHistoryPaginated(PageRequest.of(page, size));
		BigDecimal grandTotal = ordermenuService.calculateTotalRevenue();

		// สร้าง Map สำหรับเก็บข้อมูลที่ Group แล้วของทุก Order ในหน้านี้
		Map<Integer, Map<String, Integer>> summaryMap = orderPage.getContent().stream()
				.collect(Collectors.toMap(
						Ordermenu::getOrderId,
						order -> ordermenuService.getGroupedDetails(order)));

		Map<String, Object> response = new HashMap<>();
		response.put("orders", orderPage.getContent());
		response.put("currentPage", page);
		response.put("totalPages", orderPage.getTotalPages());
		response.put("totalItems", orderPage.getTotalElements());
		response.put("pageSize", size);
		response.put("grandTotal", grandTotal);
		response.put("summaryMap", summaryMap);

		return ResponseEntity.ok(response);
	}

	// 3. ดูรายละเอียดของออเดอร์ที่เคยสั่ง (ผ่าน ID)
	@GetMapping("/history/{id}")
	public ResponseEntity<Map<String, Object>> showOrderDetail(@PathVariable("id") Integer id) {
		Ordermenu order = ordermenuService.findById(id);
		if (order == null) {
			return ResponseEntity.notFound().build();
		}

		Map<String, Object> response = new HashMap<>();
		response.put("order", order);
		response.put("groupedDetails", ordermenuService.getGroupedDetails(order));

		return ResponseEntity.ok(response);
	}

	// 4. ดึงข้อมูลประวัติและสินค้าของโต๊ะที่จะสั่งอาหาร
	@GetMapping("/{id}")
	public ResponseEntity<Map<String, Object>> showOrderFormByTable(@PathVariable("id") Integer id) {
		Map<String, Object> response = new HashMap<>();
		response.put("selectedTableId", id);
		response.put("products", productService.findAvailable());
		response.put("aggregatedOrders", ordermenuService.getAggregatedActiveOrders(id));

		return ResponseEntity.ok(response);
	}

	// 5. บันทึกคำสั่งซื้อ
	@PostMapping("/save")
	public ResponseEntity<Map<String, Object>> saveOrder(@RequestBody OrderRequest orderRequest) {
		Map<String, Object> response = new HashMap<>();

		if (orderRequest.getTableId() == null || orderRequest.getQuantities() == null
				|| orderRequest.getQuantities().isEmpty()) {
			response.put("success", false);
			response.put("message", "ข้อมูลคำสั่งซื้อไม่ถูกต้อง");
			return ResponseEntity.badRequest().body(response);
		}

		// คัดมาเฉพาะสินค้าที่เลือกจำนวนมากกว่า 0
		Map<Integer, Integer> validQuantities = new LinkedHashMap<>();
		for (Map.Entry<Integer, Integer> entry : orderRequest.getQuantities().entrySet()) {
			if (entry.getValue() > 0) {
				validQuantities.put(entry.getKey(), entry.getValue());
			}
		}

		if (validQuantities.isEmpty()) {
			response.put("success", false);
			response.put("message", "กรุณาระบุจำนวนสินค้าอย่างน้อย 1 รายการ");
			return ResponseEntity.badRequest().body(response);
		}

		try {
			ordermenuService.createOrder(orderRequest.getTableId(), validQuantities);
			response.put("success", true);
			response.put("message", "บันทึกคำสั่งซื้อเรียบร้อยแล้ว");
			return ResponseEntity.ok(response);
		} catch (IllegalArgumentException ex) {
			response.put("success", false);
			response.put("message", ex.getMessage());
			return ResponseEntity.badRequest().body(response);
		}
	}
}
