package net.start.dto;

import java.util.List;

import net.start.model.Product;

public record OrderPageData(
        Integer tableId,
        List<Product> products,
        List<OrderItemSummary> aggregatedOrders) {
}
