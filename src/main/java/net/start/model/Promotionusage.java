package net.start.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "promotionusage")
@IdClass(PromotionusageId.class)
public class Promotionusage implements java.io.Serializable {

    @Id
    @Column(name = "promo_id")
    private Integer promoId;

    @Id
    @Column(name = "od_id")
    private Integer odId;

    @ManyToOne
    @JoinColumn(name = "promo_id", insertable = false, updatable = false)
    private Promotion promotion;

    @ManyToOne
    @JoinColumn(name = "od_id", insertable = false, updatable = false)
    private OrderDetail orderDetail;

    public Promotionusage() {
    }

    public Integer getPromoId() {
        return this.promoId;
    }

    public void setPromoId(Integer promoId) {
        this.promoId = promoId;
    }

    public Integer getOdId() {
        return this.odId;
    }

    public void setOdId(Integer odId) {
        this.odId = odId;
    }

    public Promotion getPromotion() {
        return this.promotion;
    }

    public void setPromotion(Promotion promotion) {
        this.promotion = promotion;
    }

    public OrderDetail getOrderDetail() {
        return this.orderDetail;
    }

    public void setOrderDetail(OrderDetail orderDetail) {
        this.orderDetail = orderDetail;
    }

}
