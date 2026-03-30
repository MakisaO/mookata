package net.start.controller;

import java.util.List;
import java.util.LinkedHashMap;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.start.model.OrderDetail;
import net.start.model.Ordermenu;
import net.start.repository.OrderDetailRepository;

@RestController
@RequestMapping("/api/kitchen") // แนะนำให้เติม /api เพื่อแยกกับ Route ของ Frontend
@CrossOrigin(origins = "*")
public class KitchenController {

    private static final Logger logger = LoggerFactory.getLogger(KitchenController.class);

    @Autowired
    private OrderDetailRepository orderDetailRepository;

    @GetMapping("/dashboard")
    public ResponseEntity<Map<String, Object>> viewKitchenDashboard() {
        // Fetch items that are ordered or cooking
        List<OrderDetail> pendingItems = orderDetailRepository.findByItemStatusInOrderByDetailIdAsc(List.of("ordered", "cooking"));
        
        // Group by Ordermenu (each round)
        Map<Ordermenu, List<OrderDetail>> groupedOrders = pendingItems.stream()
                .collect(Collectors.groupingBy(OrderDetail::getOrdermenu, 
                         LinkedHashMap::new, Collectors.toList()));

        // Prepare response list for JSON serialization
        List<Map<String, Object>> roundsData = new ArrayList<>();

        for (Map.Entry<Ordermenu, List<OrderDetail>> entry : groupedOrders.entrySet()) {
            Ordermenu ordermenu = entry.getKey();
            List<OrderDetail> items = entry.getValue();
            
            boolean hasOrdered = items.stream().anyMatch(i -> "ordered".equals(i.getItemStatus()));
            boolean allOrdered = items.stream().allMatch(i -> "ordered".equals(i.getItemStatus()));
            
            String massActionLabel;
            if (allOrdered) {
                massActionLabel = "👨‍🍳 เริ่มทำทั้งหมด";
            } else if (hasOrdered) {
                massActionLabel = "⏩ ปรับเป็นกำลังทำทั้งหมด";
            } else {
                massActionLabel = "🛎️ เสิร์ฟทั้งหมด";
            }

            // จัด โครงสร้างใหม่ให้อ่านง่ายเมื่อแปลงเป็น JSON
            Map<String, Object> roundInfo = new HashMap<>();
            roundInfo.put("orderMenu", ordermenu);
            roundInfo.put("items", items);
            roundInfo.put("massActionLabel", massActionLabel);
            
            roundsData.add(roundInfo);
        }
                         
        Map<String, Object> response = new HashMap<>();
        response.put("pendingItemsCount", pendingItems.size());
        response.put("rounds", roundsData);

        return ResponseEntity.ok(response);
    }

    @PutMapping("/cook/{id}")
    @Transactional
    public ResponseEntity<Map<String, String>> markCooking(@PathVariable("id") Integer id) {
        OrderDetail detail = orderDetailRepository.findById(id).orElseThrow();
        logger.info("Kitchen: Starting to cook detail ID: {}, current status: {}", id, detail.getItemStatus());
        
        detail.setItemStatus("cooking");
        orderDetailRepository.save(detail);
        
        return ResponseEntity.ok(Map.of(
            "status", "success",
            "message", "สถานะ: กำลังทำ",
            "updatedId", String.valueOf(id)
        ));
    }

    @PutMapping("/serve/{id}")
    @Transactional
    public ResponseEntity<Map<String, String>> markServed(@PathVariable("id") Integer id) {
        OrderDetail detail = orderDetailRepository.findById(id).orElseThrow();
        logger.info("Kitchen: Serving detail ID: {}, current status: {}", id, detail.getItemStatus());
        
        detail.setItemStatus("served");
        orderDetailRepository.save(detail);
        
        return ResponseEntity.ok(Map.of(
            "status", "success",
            "message", "สถานะ: เสิร์ฟแล้ว",
            "updatedId", String.valueOf(id)
        ));
    }

    @PutMapping("/mass-update/{orderId}")
    @Transactional
    public ResponseEntity<Map<String, Object>> massUpdateRound(@PathVariable("orderId") Integer orderId) {
        // Fetch only pending items for this specific order round
        List<OrderDetail> items = orderDetailRepository.findByItemStatusInOrderByDetailIdAsc(List.of("ordered", "cooking"))
                .stream()
                .filter(item -> item.getOrdermenu().getOrderId().equals(orderId))
                .collect(Collectors.toList());

        if (items.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of(
                "status", "error", 
                "message", "ไม่พบรายการที่ต้องอัปเดต"
            ));
        }

        boolean hasOrdered = items.stream().anyMatch(i -> "ordered".equals(i.getItemStatus()));

        String targetStatus;
        String message;

        if (hasOrdered) {
            targetStatus = "cooking";
            message = "อัปเดตรายการทั้งหมดเป็น: กำลังทำ";
            items.stream().filter(i -> "ordered".equals(i.getItemStatus())).forEach(i -> i.setItemStatus(targetStatus));
        } else {
            targetStatus = "served";
            message = "อัปเดตรายการทั้งหมดเป็น: เสิร์ฟแล้ว";
            items.forEach(i -> i.setItemStatus(targetStatus));
        }

        orderDetailRepository.saveAll(items);
        
        return ResponseEntity.ok(Map.of(
            "status", "success",
            "message", message,
            "updatedCount", items.size(),
            "newStatus", targetStatus
        ));
    }
}