package net.start.model;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

@Entity
@Table(name = "tables")
public class Tables implements java.io.Serializable {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "tableID")
	private Integer tableId;

	@Column(name = "status")
	private String status;

	@JsonIgnore
	@OneToMany(mappedBy = "tables", cascade = CascadeType.ALL)
	private List<Ordermenu> ordermenus = new ArrayList<>();

	public Tables() {
	}

	public Integer getTableId() {
		return this.tableId;
	}

	public void setTableId(Integer tableId) {
		this.tableId = tableId;
	}

	public String getStatus() {
		return this.status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public List<Ordermenu> getOrdermenus() {
		return this.ordermenus;
	}

	public void setOrdermenus(List<Ordermenu> ordermenus) {
		this.ordermenus = ordermenus;
	}

}
