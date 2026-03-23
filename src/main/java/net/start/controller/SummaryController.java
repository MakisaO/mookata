package net.start.controller;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import net.start.model.Ordermenu;
import net.start.service.OrdermenuService;

@Controller
@RequestMapping("/summary")
public class SummaryController {

    @Autowired
    private OrdermenuService ordermenuService;

    @GetMapping("")
    public String viewSummary(Model model) {
        List<Ordermenu> allPaidOrders = ordermenuService.getPaidOrderHistory();
        BigDecimal totalRevenue = ordermenuService.calculateTotalRevenue();
        int totalOrders = allPaidOrders.size();
        
        Map<String, Integer> topProducts = ordermenuService.getTopSellingProducts();

        model.addAttribute("totalRevenue", totalRevenue);
        model.addAttribute("totalOrders", totalOrders);
        model.addAttribute("topProducts", topProducts);
        
        return "summary"; // templates/summary.html
    }
}
