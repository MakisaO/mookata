package net.start.dto;

import java.math.BigDecimal;
import java.util.List;

public record OrderHistoryEntry(
        Integer orderId,
        Integer tableId,
        String orderDate,
        BigDecimal totalAmount,
        List<String> items) {
}
