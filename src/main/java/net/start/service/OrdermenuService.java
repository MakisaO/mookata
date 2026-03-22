package net.start.service;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
		Tables table = tablesRepository.findById(tableId).orElse(null);

		Ordermenu ordermenu = new Ordermenu();
		ordermenu.setTables(table);
		ordermenu.setOrderDate(Timestamp.from(Instant.now()));
		ordermenu.setOrderStatus("pending");
		ordermenu.setTotalAmount(BigDecimal.ZERO);
		ordermenu = ordermenuRepository.save(ordermenu);

		BigDecimal totalAmount = BigDecimal.ZERO;
		boolean hasItem = false;

		for (Map.Entry<Integer, Integer> entry : quantities.entrySet()) {
			Integer quantity = entry.getValue();
			if (quantity == null || quantity <= 0) {
				continue;
			}

			Product product = productRepository.findById(entry.getKey()).orElseThrow();

			OrderDetail detail = new OrderDetail();
			detail.setOrdermenu(ordermenu);
			detail.setProduct(product);
			detail.setQuantity(quantity);
			detail.setUnitPrice(product.getProductPrice());
			detail.setItemStatus("ordered");
			orderDetailRepository.save(detail);

			totalAmount = totalAmount.add(product.getProductPrice().multiply(BigDecimal.valueOf(quantity)));
			hasItem = true;
		}

		if (!hasItem) {
			throw new IllegalArgumentException("กรุณาเลือกเมนูอย่างน้อย 1 รายการ");
		}

		ordermenu.setTotalAmount(totalAmount);
		table.setStatus("unavailable");
		tablesRepository.save(table);
		return ordermenuRepository.save(ordermenu);
	}
	
	public List<Ordermenu> getActiveOrdersByTable(Integer tableId) {
	    return ordermenuRepository.findByTables_TableIdAndOrderStatusNot(tableId, "paid");
	}
}
