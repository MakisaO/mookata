package net.start.controller;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import net.start.model.Ordermenu;
import net.start.model.Product;
import net.start.model.Tables;
import net.start.service.OrdermenuService;
import net.start.service.ProductService;
import net.start.service.TablesService;

@Controller
@RequestMapping("/orders")
public class OrderController {

	@Autowired
	private TablesService tablesService;

	@Autowired
	private ProductService productService;

	@Autowired
	private OrdermenuService ordermenuService;

	@GetMapping("/new")
	public String showOrderForm(Model model) {
		List<Tables> tables = tablesService.findAll();
		List<Product> products = productService.findAvailable();

		model.addAttribute("tables",tables);
		model.addAttribute("products", products);
		return "orders/form";
	}
	
	@GetMapping("/{id}")
	public String showOrderFormByTable(@PathVariable("id") Integer id, Model model) {
	    model.addAttribute("selectedTableId", id);
	    model.addAttribute("products", productService.findAvailable());
	    
	    List<Ordermenu> activeOrders = ordermenuService.getActiveOrdersByTable(id);
	    model.addAttribute("pastOrders", activeOrders);
	    
	    return "orders/form";
	}

	@PostMapping("/save")
	public String saveOrder(@RequestParam("tableId") Integer tableId,
			@RequestParam Map<String, String> requestParams,
			RedirectAttributes redirectAttributes) {
		Map<Integer, Integer> quantities = new LinkedHashMap<>();

		for (Map.Entry<String, String> entry : requestParams.entrySet()) {
			String key = entry.getKey();
			if (!key.startsWith("qty_")) {
				continue;
			}

			String value = entry.getValue();
			if (value == null || value.isBlank()) {
				continue;
			}

			int quantity = Integer.parseInt(value);
			if (quantity <= 0) {
				continue;
			}

			int productId = Integer.parseInt(key.substring(4));
			quantities.put(productId, quantity);
		}

		try {
			ordermenuService.createOrder(tableId, quantities);
			redirectAttributes.addFlashAttribute("successMessage", "บันทึกคำสั่งซื้อเรียบร้อยแล้ว");
		} catch (IllegalArgumentException ex) {
			redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
		}

		return "redirect:/orders/new";
	}
}
