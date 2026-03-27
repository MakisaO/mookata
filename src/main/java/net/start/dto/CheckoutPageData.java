package net.start.dto;

import java.math.BigDecimal;
import java.util.List;

public record CheckoutPageData(
        Integer tableId,
        List<OrderItemSummary> aggregatedOrders,
        BigDecimal originalTotal,
        BigDecimal discount,
        BigDecimal finalTotal,
        List<String> promoMessages) {
}
