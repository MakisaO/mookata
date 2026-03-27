package net.start.controller;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import net.start.model.OrderDetail;
import net.start.model.Ordermenu;
import net.start.model.Product;
import net.start.service.OrdermenuService;
import net.start.service.ProductService;

@Controller
@RequestMapping("/summary")
public class SummaryController {

    @Autowired
    private OrdermenuService ordermenuService;

    @Autowired
    private ProductService productService;

    @GetMapping("")
    public String viewSummary(Model model) {
        BigDecimal totalRevenue = ordermenuService.calculateTotalRevenue();
        List<Ordermenu> allPaidOrders = ordermenuService.getPaidOrderHistory();
        int totalOrders = allPaidOrders.size();
        Map<Product, Integer> topProducts = ordermenuService.getTopSellingProducts();
        Map<Product, Integer> leastProducts = ordermenuService.getLeastSellingProducts();

        model.addAttribute("totalRevenue", totalRevenue);
        model.addAttribute("totalOrders", totalOrders);
        model.addAttribute("topProducts", topProducts);
        model.addAttribute("leastProducts", leastProducts);
        return "app";
    }

    @GetMapping("/product/{id}")
    public String viewProductSales(@PathVariable("id") Integer id, Model model) {
        Product product = productService.findById(id);
        List<OrderDetail> salesHistory = ordermenuService.getProductSalesHistory(id);

        int totalSold = salesHistory.stream().mapToInt(OrderDetail::getQuantity).sum();
        BigDecimal totalRevenue = salesHistory.stream()
                .map(d -> d.getUnitPrice().multiply(BigDecimal.valueOf(d.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        model.addAttribute("product", product);
        model.addAttribute("salesHistory", salesHistory);
        model.addAttribute("totalSold", totalSold);
        model.addAttribute("totalRevenue", totalRevenue);
        return "app";
    }
}
