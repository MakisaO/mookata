package net.start.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.ResponseEntity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Service to interact with the Partner Ice Cream Shop Promotion API.
 */
@Service
public class PartnerCouponService {

    private final String BASE_URL = "https://zvenzen-production.up.railway.app/api/v1/partner";
    private final RestTemplate restTemplate = new RestTemplate();

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class PartnerPromotion {
        private Integer id;
        private String name;
        private Double discountValue;
        private Double minOrderAmount;
        private Integer couponsRemaining;
        private String productName;
        private String optionName;

        // Getters and Setters
        public Integer getId() { return id; }
        public void setId(Integer id) { this.id = id; }

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public Double getDiscountValue() { return discountValue; }
        public void setDiscountValue(Double discountValue) { this.discountValue = discountValue; }

        public Double getMinOrderAmount() { return minOrderAmount; }
        public void setMinOrderAmount(Double minOrderAmount) { this.minOrderAmount = minOrderAmount; }

        public Integer getCouponsRemaining() { return couponsRemaining; }
        public void setCouponsRemaining(Integer couponsRemaining) { this.couponsRemaining = couponsRemaining; }

        public String getProductName() { return productName; }
        public void setProductName(String productName) { this.productName = productName; }

        public String getOptionName() { return optionName; }
        public void setOptionName(String optionName) { this.optionName = optionName; }
        
        public String getDisplayName() {
            StringBuilder sb = new StringBuilder(name);
            if (productName != null && !productName.isEmpty()) {
                sb.append(" (").append(productName);
                if (optionName != null && !optionName.isEmpty()) {
                    sb.append(" - ").append(optionName);
                }
                sb.append(")");
            }
            return sb.toString();
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class PartnerResponse {
        private boolean success;
        private List<PartnerPromotion> data;

        public boolean isSuccess() { return success; }
        public void setSuccess(boolean success) { this.success = success; }

        public List<PartnerPromotion> getData() { return data; }
        public void setData(List<PartnerPromotion> data) { this.data = data; }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class CouponIssueResponse {
        private boolean success;
        private CouponData data;

        public boolean isSuccess() { return success; }
        public void setSuccess(boolean success) { this.success = success; }

        public CouponData getData() { return data; }
        public void setData(CouponData data) { this.data = data; }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class CouponData {
        private String code;
        private String promotionName;

        public String getCode() { return code; }
        public void setCode(String code) { this.code = code; }

        public String getPromotionName() { return promotionName; }
        public void setPromotionName(String promotionName) { this.promotionName = promotionName; }
    }

    /**
     * Get promotions that are available and the user qualifies for based on the 50% rule.
     */
    public List<PartnerPromotion> getAvailablePromotions(double ourTotalAmount) {
        try {
            ResponseEntity<PartnerResponse> response = restTemplate.getForEntity(BASE_URL + "/promotions", PartnerResponse.class);
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null && response.getBody().isSuccess()) {
                List<PartnerPromotion> allPromos = response.getBody().getData();
                if (allPromos == null || allPromos.isEmpty()) return Collections.emptyList();
                
                // Filter: couponsRemaining > 0 
                // AND ourTotalAmount >= (partnerMinAmount * 0.5) 
                // AND ourTotalAmount >= 100 (Our Shop Minimum Floor)
                return allPromos.stream()
                    .filter(p -> p.getCouponsRemaining() != null && p.getCouponsRemaining() > 0)
                    .filter(p -> ourTotalAmount >= 100 && ourTotalAmount >= (p.getMinOrderAmount() * 0.5))
                    .collect(Collectors.toList());
            }
        } catch (Exception e) {
            System.err.println("Error fetching partner promotions: " + e.getMessage());
        }
        return Collections.emptyList();
    }

    /**
     * Get ALL active promotions from partner regardless of our current total.
     * Useful for showing what COULD be earned.
     */
    public List<PartnerPromotion> getAllActivePromotions() {
        try {
            ResponseEntity<PartnerResponse> response = restTemplate.getForEntity(BASE_URL + "/promotions", PartnerResponse.class);
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null && response.getBody().isSuccess()) {
                List<PartnerPromotion> data = response.getBody().getData();
                return data != null ? data.stream()
                        .filter(p -> p.getCouponsRemaining() != null && p.getCouponsRemaining() > 0)
                        .collect(Collectors.toList()) : Collections.emptyList();
            }
        } catch (Exception e) {
            System.err.println("Error fetching partner promotions: " + e.getMessage());
        }
        return Collections.emptyList();
    }

    /**
     * Helper to determine our minimum floor for a partner promo.
     */
    private double calculateOurMinAmount(Double partnerMin) {
        return Math.max(100.0, (partnerMin != null ? partnerMin : 0) * 0.5);
    }

    /**
     * Logic to get promotions for UI display with eligibility status.
     * Moved from Controller to Service.
     */
    public List<Map<String, Object>> getEligiblePartnerRewards(double ourTotalAmount) {
        List<PartnerPromotion> allPromos = getAllActivePromotions();
        return allPromos.stream().map(p -> {
            Map<String, Object> m = new HashMap<>();
            double ourMin = calculateOurMinAmount(p.getMinOrderAmount());
            m.put("id", p.getId());
            m.put("name", p.getName());
            m.put("partnerMinAmount", p.getMinOrderAmount());
            m.put("ourMinAmount", ourMin);
            m.put("productName", p.getProductName());
            m.put("optionName", p.getOptionName());
            m.put("isEligible", ourTotalAmount >= ourMin);
            return m;
        }).collect(Collectors.toList());
    }

    /**
     * Final logic to pick and issue a partner coupon after payment.
     * Handles the shuffle and POST in one go.
     */
    public CouponData issueRandomQualifiedCoupon(double ourTotalAmount) {
        List<PartnerPromotion> eligiblePromos = getAvailablePromotions(ourTotalAmount);
        if (eligiblePromos.isEmpty()) return null;

        java.util.Collections.shuffle(eligiblePromos);
        PartnerPromotion selected = eligiblePromos.get(0);
        return issueCoupon(selected.getId());
    }

    /**
     * Issues a coupon from the partner.
     */
    public CouponData issueCoupon(Integer promotionId) {
        try {
            java.util.Map<String, Object> request = new java.util.HashMap<>();
            request.put("promotionId", promotionId);
            ResponseEntity<CouponIssueResponse> response = restTemplate.postForEntity(BASE_URL + "/coupons/issue", request, CouponIssueResponse.class);
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null && response.getBody().isSuccess()) {
                return response.getBody().getData();
            }
        } catch (Exception e) {
            System.err.println("Error issuing partner coupon: " + e.getMessage());
        }
        return null;
    }
}
