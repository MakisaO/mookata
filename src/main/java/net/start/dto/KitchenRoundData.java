package net.start.dto;

import java.util.List;

public record KitchenRoundData(
        Integer orderId,
        Integer tableId,
        String orderTime,
        String massActionLabel,
        List<KitchenItemData> items) {
}
