package net.start.dto;

import java.math.BigDecimal;

public record OrderDetailLine(
        String productName,
        Integer quantity,
        BigDecimal unitPrice,
        BigDecimal lineTotal) {
}
