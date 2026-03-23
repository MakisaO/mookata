package net.start.model;

import java.math.BigDecimal;
import java.sql.Timestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "promotion")
public class Promotion implements java.io.Serializable {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "promotion_id")
	private Integer promotionId;

	@Column(name = "name")
	private String name;

	@Column(name = "detail")
	private String detail;

	@Column(name = "type")
	private String type;

	@Column(name = "value")
	private BigDecimal value;

	@Column(name = "percent")
	private BigDecimal percent;

	@Column(name = "quantity")
	private Integer quantity;

	@ManyToOne
	@JoinColumn(name = "free_product_id")
	private Product freeProduct;

	@Column(name = "minspend")
	private BigDecimal minspend;

	@ManyToOne
	@JoinColumn(name = "req_product_id")
	private Product reqProduct;

	@Column(name = "req_quantity")
	private Integer reqQuantity;

	@Column(name = "start_date")
	private Timestamp startDate;

	@Column(name = "end_date")
	private Timestamp endDate;

	public Promotion() {
	}

	public Integer getPromotionId() {
		return this.promotionId;
	}

	public void setPromotionId(Integer promotionId) {
		this.promotionId = promotionId;
	}

	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDetail() {
		return this.detail;
	}

	public void setDetail(String detail) {
		this.detail = detail;
	}

	public String getType() {
		return this.type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public BigDecimal getValue() {
		return this.value;
	}

	public void setValue(BigDecimal value) {
		this.value = value;
	}

	public BigDecimal getPercent() {
		return this.percent;
	}

	public void setPercent(BigDecimal percent) {
		this.percent = percent;
	}

	public Integer getQuantity() {
		return this.quantity;
	}

	public void setQuantity(Integer quantity) {
		this.quantity = quantity;
	}

	public Product getFreeProduct() {
		return this.freeProduct;
	}

	public void setFreeProduct(Product freeProduct) {
		this.freeProduct = freeProduct;
	}

	public BigDecimal getMinspend() {
		return this.minspend;
	}

	public void setMinspend(BigDecimal minspend) {
		this.minspend = minspend;
	}

	public Product getReqProduct() {
		return this.reqProduct;
	}

	public void setReqProduct(Product reqProduct) {
		this.reqProduct = reqProduct;
	}

	public Integer getReqQuantity() {
		return this.reqQuantity;
	}

	public void setReqQuantity(Integer reqQuantity) {
		this.reqQuantity = reqQuantity;
	}

	public Timestamp getStartDate() {
		return this.startDate;
	}

	public void setStartDate(Timestamp startDate) {
		this.startDate = startDate;
	}

	public Timestamp getEndDate() {
		return this.endDate;
	}

	public void setEndDate(Timestamp endDate) {
		this.endDate = endDate;
	}

}
