package net.start.dto;

import java.util.LinkedHashMap;
import java.util.Map;

public class OrderCreateRequest {

    private Integer tableId;
    private Map<Integer, Integer> quantities = new LinkedHashMap<>();

    public Integer getTableId() {
        return tableId;
    }

    public void setTableId(Integer tableId) {
        this.tableId = tableId;
    }

    public Map<Integer, Integer> getQuantities() {
        return quantities;
    }

    public void setQuantities(Map<Integer, Integer> quantities) {
        this.quantities = quantities;
    }
}
