package net.start.controller;

import java.util.List;
import java.util.LinkedHashMap;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
        // Fetch items that are ordered or cooking
        List<OrderDetail> pendingItems = orderDetailRepository.findByItemStatusInOrderByDetailIdAsc(List.of("ordered", "cooking"));
        
        // Group by Ordermenu (each round)
        java.util.Map<Ordermenu, List<OrderDetail>> groupedOrders = pendingItems.stream()
                .collect(Collectors.groupingBy(OrderDetail::getOrdermenu, 
                         LinkedHashMap::new, Collectors.toList()));

        // Calculate mass action labels for each order round
        java.util.Map<Integer, String> massActionLabels = new java.util.HashMap<>();
        for (java.util.Map.Entry<Ordermenu, List<OrderDetail>> entry : groupedOrders.entrySet()) {
            List<OrderDetail> items = entry.getValue();
            boolean hasOrdered = items.stream().anyMatch(i -> "ordered".equals(i.getItemStatus()));
            boolean allOrdered = items.stream().allMatch(i -> "ordered".equals(i.getItemStatus()));
            
            if (allOrdered) {
                massActionLabels.put(entry.getKey().getOrderId(), "👨‍🍳 เริ่มทำทั้งหมด");
            } else if (hasOrdered) {
                massActionLabels.put(entry.getKey().getOrderId(), "⏩ ปรับเป็นกำลังทำทั้งหมด");
            } else {
                massActionLabels.put(entry.getKey().getOrderId(), "🛎️ เสิร์ฟทั้งหมด");
            }
        }
                         
        model.addAttribute("pendingItems", pendingItems);
        model.addAttribute("groupedOrders", groupedOrders);
        model.addAttribute("massActionLabels", massActionLabels);
        return "kitchen/dashboard";
    }

    @GetMapping("/cook/{id}")
    @Transactional
    public String markCooking(@PathVariable("id") Integer id, RedirectAttributes redirectAttributes) {
        OrderDetail detail = orderDetailRepository.findById(id).orElseThrow();
        logger.info("Kitchen: Starting to cook detail ID: {}, current status: {}", id, detail.getItemStatus());
        detail.setItemStatus("cooking");
        orderDetailRepository.save(detail);
        redirectAttributes.addFlashAttribute("successMessage", "สถานะ: กำลังทำ");
        return "redirect:/kitchen";
    }

    @GetMapping("/serve/{id}")
    @Transactional
    public String markServed(@PathVariable("id") Integer id, RedirectAttributes redirectAttributes) {
        OrderDetail detail = orderDetailRepository.findById(id).orElseThrow();
        logger.info("Kitchen: Serving detail ID: {}, current status: {}", id, detail.getItemStatus());
        detail.setItemStatus("served");
        orderDetailRepository.save(detail);
        redirectAttributes.addFlashAttribute("successMessage", "สถานะ: เสิร์ฟแล้ว");
        return "redirect:/kitchen";
    }

    @GetMapping("/mass-update/{orderId}")
    @Transactional
    public String massUpdateRound(@PathVariable("orderId") Integer orderId, RedirectAttributes redirectAttributes) {
        // Fetch only pending items for this specific order round
        List<OrderDetail> items = orderDetailRepository.findByItemStatusInOrderByDetailIdAsc(List.of("ordered", "cooking"))
                .stream()
                .filter(item -> item.getOrdermenu().getOrderId().equals(orderId))
                .collect(Collectors.toList());

        if (items.isEmpty()) {
            return "redirect:/kitchen";
        }

        boolean hasOrdered = items.stream().anyMatch(i -> "ordered".equals(i.getItemStatus()));

        String targetStatus;
        String message;

        if (hasOrdered) {
            // If there's anything "ordered", the mass action is to move to "cooking"
            // This applies even if some are already "cooking" (brings the "ordered" ones up to "cooking")
            targetStatus = "cooking";
            message = "อัปเดตรายการทั้งหมดเป็น: กำลังทำ";
            items.stream().filter(i -> "ordered".equals(i.getItemStatus())).forEach(i -> i.setItemStatus(targetStatus));
        } else {
            // If nothing is "ordered" (all are "cooking"), move everything to "served"
            targetStatus = "served";
            message = "อัปเดตรายการทั้งหมดเป็น: เสิร์ฟแล้ว";
            items.forEach(i -> i.setItemStatus(targetStatus));
        }

        orderDetailRepository.saveAll(items);
        redirectAttributes.addFlashAttribute("successMessage", message);
        return "redirect:/kitchen";
    }
}
