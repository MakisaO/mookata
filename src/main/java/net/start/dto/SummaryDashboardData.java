package net.start.dto;

import java.math.BigDecimal;
import java.util.List;

public record SummaryDashboardData(
        BigDecimal totalRevenue,
        Integer totalOrders,
        List<SummaryProductStat> topProducts,
        List<SummaryProductStat> leastProducts) {
}
