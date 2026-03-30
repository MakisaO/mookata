package net.start.model;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

@Entity
@Table(name = "ordermenu")
public class Ordermenu implements java.io.Serializable {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "orderID")
	private Integer orderId;

	@ManyToOne
	@JoinColumn(name = "tableID")
	private Tables tables;

	@Column(name = "orderDate")
	private Timestamp orderDate;

	@Column(name = "orderStatus")
	private String orderStatus;

	@Column(name = "totalAmount")
	private BigDecimal totalAmount;

	@JsonManagedReference(value="order-details")
	@OneToMany(mappedBy = "ordermenu", cascade = CascadeType.ALL)
	private List<OrderDetail> orderDetails = new ArrayList<>();

	@JsonIgnore
	@OneToMany(mappedBy = "ordermenu", cascade = CascadeType.ALL)
	private List<Payment> payments = new ArrayList<>();

	public Ordermenu() {
	}

	public Integer getOrderId() {
		return this.orderId;
	}

	public void setOrderId(Integer orderId) {
		this.orderId = orderId;
	}

	public Tables getTables() {
		return this.tables;
	}

	public void setTables(Tables tables) {
		this.tables = tables;
	}

	public Timestamp getOrderDate() {
		return this.orderDate;
	}

	public void setOrderDate(Timestamp orderDate) {
		this.orderDate = orderDate;
	}

	public String getOrderStatus() {
		return this.orderStatus;
	}

	public void setOrderStatus(String orderStatus) {
		this.orderStatus = orderStatus;
	}

	public BigDecimal getTotalAmount() {
		return this.totalAmount;
	}

	public void setTotalAmount(BigDecimal totalAmount) {
		this.totalAmount = totalAmount;
	}

	public List<OrderDetail> getOrderDetails() {
		return this.orderDetails;
	}

	public void setOrderDetails(List<OrderDetail> orderDetails) {
		this.orderDetails = orderDetails;
	}

	public List<Payment> getPayments() {
		return this.payments;
	}

	public void setPayments(List<Payment> payments) {
		this.payments = payments;
	}

}
