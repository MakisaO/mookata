package net.start.dto;

import java.math.BigDecimal;
import java.util.List;

public record OrderDetailData(
        Integer orderId,
        Integer tableId,
        String orderDate,
        String orderStatus,
        BigDecimal totalAmount,
        List<OrderDetailLine> items) {
}
