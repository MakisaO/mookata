package net.start.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import net.start.model.OrderDetail;
import net.start.repository.OrderDetailRepository;

import java.util.LinkedHashMap;
import java.util.stream.Collectors;
import net.start.model.Ordermenu;

@Controller
@RequestMapping("/kitchen")
public class KitchenController {

    @Autowired
    private OrderDetailRepository orderDetailRepository;

    @GetMapping("")
    public String viewKitchenDashboard(Model model) {
        // Fetch items that are ordered or cooking
        List<OrderDetail> pendingItems = orderDetailRepository.findByItemStatusInOrderByDetailIdAsc(List.of("ordered", "cooking"));
        
        // Group by Ordermenu (each round)
        java.util.Map<Ordermenu, List<OrderDetail>> groupedOrders = pendingItems.stream()
                .collect(Collectors.groupingBy(OrderDetail::getOrdermenu, 
                         LinkedHashMap::new, Collectors.toList()));
                         
        model.addAttribute("pendingItems", pendingItems);
        model.addAttribute("groupedOrders", groupedOrders);
        return "kitchen/dashboard";
    }

    @GetMapping("/cook/{id}")
    public String markCooking(@PathVariable("id") Integer id, RedirectAttributes redirectAttributes) {
        OrderDetail detail = orderDetailRepository.findById(id).orElseThrow();
        detail.setItemStatus("cooking");
        orderDetailRepository.save(detail);
        redirectAttributes.addFlashAttribute("successMessage", "สถานะ: กำลังทำ");
        return "redirect:/kitchen";
    }

    @GetMapping("/serve/{id}")
    public String markServed(@PathVariable("id") Integer id, RedirectAttributes redirectAttributes) {
        OrderDetail detail = orderDetailRepository.findById(id).orElseThrow();
        detail.setItemStatus("served");
        orderDetailRepository.save(detail);
        redirectAttributes.addFlashAttribute("successMessage", "สถานะ: เสิร์ฟแล้ว");
        return "redirect:/kitchen";
    }
}
