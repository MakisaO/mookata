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
@Table(name = "categories")
public class Categories implements java.io.Serializable {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "categoriesID")
	private Integer categoriesId;

	@Column(name = "categoriesName")
	private String categoriesName;

	@JsonIgnore
	@OneToMany(mappedBy = "categories", cascade = CascadeType.ALL)
	private List<Product> products = new ArrayList<>();

	public Categories() {
	}

	public Categories(String categoriesName) {
		this.categoriesName = categoriesName;
	}

	public Integer getCategoriesId() {
		return this.categoriesId;
	}

	public void setCategoriesId(Integer categoriesId) {
		this.categoriesId = categoriesId;
	}

	public String getCategoriesName() {
		return this.categoriesName;
	}

	public void setCategoriesName(String categoriesName) {
		this.categoriesName = categoriesName;
	}

	public List<Product> getProducts() {
		return this.products;
	}

	public void setProducts(List<Product> products) {
		this.products = products;
	}

}
