package net.start.dto;

import java.math.BigDecimal;
import java.util.List;

public record ProductSalesData(
        Integer productId,
        String productName,
        String productDetail,
        Integer totalSold,
        BigDecimal totalRevenue,
        List<ProductSalesEntry> salesHistory) {
}
