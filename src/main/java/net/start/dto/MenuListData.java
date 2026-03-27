package net.start.dto;

import java.util.List;

import net.start.model.Product;

public record MenuListData(
        List<Product> products,
        Integer currentPage,
        Integer totalPages,
        Long totalItems,
        String keyword,
        String sortField,
        String sortDir) {
}
