package net.start.controller;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import net.start.model.OrderDetail;
import net.start.model.Ordermenu;
import net.start.repository.OrderDetailRepository;

@Controller
@RequestMapping("/kitchen")
public class KitchenController {

    private static final Logger logger = LoggerFactory.getLogger(KitchenController.class);

    @Autowired
    private OrderDetailRepository orderDetailRepository;

    @GetMapping("")
    public String viewKitchenDashboard(Model model) {
        List<OrderDetail> pendingItems = orderDetailRepository.findByItemStatusInOrderByDetailIdAsc(List.of("ordered", "cooking"));
        java.util.Map<Ordermenu, List<OrderDetail>> groupedOrders = pendingItems.stream()
                .collect(Collectors.groupingBy(OrderDetail::getOrdermenu, LinkedHashMap::new, Collectors.toList()));

        java.util.Map<Integer, String> massActionLabels = new java.util.HashMap<>();
        for (java.util.Map.Entry<Ordermenu, List<OrderDetail>> entry : groupedOrders.entrySet()) {
            List<OrderDetail> items = entry.getValue();
            boolean hasOrdered = items.stream().anyMatch(i -> "ordered".equals(i.getItemStatus()));
            boolean allOrdered = items.stream().allMatch(i -> "ordered".equals(i.getItemStatus()));

            if (allOrdered) {
                massActionLabels.put(entry.getKey().getOrderId(), "Start All");
            } else if (hasOrdered) {
                massActionLabels.put(entry.getKey().getOrderId(), "Move Ordered To Cooking");
            } else {
                massActionLabels.put(entry.getKey().getOrderId(), "Serve All");
            }
        }

        model.addAttribute("pendingItems", pendingItems);
        model.addAttribute("groupedOrders", groupedOrders);
        model.addAttribute("massActionLabels", massActionLabels);
        return "app";
    }

    @GetMapping("/cook/{id}")
    @Transactional
    public String markCooking(@PathVariable("id") Integer id, RedirectAttributes redirectAttributes) {
        OrderDetail detail = orderDetailRepository.findById(id).orElseThrow();
        logger.info("Kitchen: Starting to cook detail ID: {}, current status: {}", id, detail.getItemStatus());
        detail.setItemStatus("cooking");
        orderDetailRepository.save(detail);
        redirectAttributes.addFlashAttribute("successMessage", "Item updated to cooking");
        return "redirect:/kitchen";
    }

    @GetMapping("/serve/{id}")
    @Transactional
    public String markServed(@PathVariable("id") Integer id, RedirectAttributes redirectAttributes) {
        OrderDetail detail = orderDetailRepository.findById(id).orElseThrow();
        logger.info("Kitchen: Serving detail ID: {}, current status: {}", id, detail.getItemStatus());
        detail.setItemStatus("served");
        orderDetailRepository.save(detail);
        redirectAttributes.addFlashAttribute("successMessage", "Item updated to served");
        return "redirect:/kitchen";
    }

    @GetMapping("/mass-update/{orderId}")
    @Transactional
    public String massUpdateRound(@PathVariable("orderId") Integer orderId, RedirectAttributes redirectAttributes) {
        List<OrderDetail> items = orderDetailRepository.findByItemStatusInOrderByDetailIdAsc(List.of("ordered", "cooking"))
                .stream()
                .filter(item -> item.getOrdermenu().getOrderId().equals(orderId))
                .collect(Collectors.toList());

        if (items.isEmpty()) {
            return "redirect:/kitchen";
        }

        boolean hasOrdered = items.stream().anyMatch(i -> "ordered".equals(i.getItemStatus()));
        if (hasOrdered) {
            items.stream().filter(i -> "ordered".equals(i.getItemStatus())).forEach(i -> i.setItemStatus("cooking"));
            orderDetailRepository.saveAll(items);
            redirectAttributes.addFlashAttribute("successMessage", "Moved ordered items to cooking");
        } else {
            items.forEach(i -> i.setItemStatus("served"));
            orderDetailRepository.saveAll(items);
            redirectAttributes.addFlashAttribute("successMessage", "Moved items to served");
        }

        return "redirect:/kitchen";
    }
}
