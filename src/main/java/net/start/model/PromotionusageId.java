package net.start.model;

import java.io.Serializable;
import java.util.Objects;

public class PromotionusageId implements Serializable {

    private Integer promoId;
    private Integer odId;

    public PromotionusageId() {
    }

    public PromotionusageId(Integer promoId, Integer odId) {
        this.promoId = promoId;
        this.odId = odId;
    }

    public Integer getPromoId() {
        return promoId;
    }

    public void setPromoId(Integer promoId) {
        this.promoId = promoId;
    }

    public Integer getOdId() {
        return odId;
    }

    public void setOdId(Integer odId) {
        this.odId = odId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        PromotionusageId that = (PromotionusageId) o;
        return Objects.equals(promoId, that.promoId) && Objects.equals(odId, that.odId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(promoId, odId);
    }

}
