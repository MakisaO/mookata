package net.start.dto;

import java.math.BigDecimal;

public record ProductSalesEntry(
        Integer orderId,
        Integer tableId,
        String orderDate,
        Integer quantity,
        BigDecimal unitPrice,
        BigDecimal lineTotal) {
}
