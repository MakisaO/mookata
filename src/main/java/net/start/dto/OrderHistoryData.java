package net.start.dto;

import java.math.BigDecimal;
import java.util.List;

public record OrderHistoryData(
        Integer currentPage,
        Integer totalPages,
        Long totalItems,
        Integer pageSize,
        BigDecimal grandTotal,
        List<OrderHistoryEntry> orders) {
}
