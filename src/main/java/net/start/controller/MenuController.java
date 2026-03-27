package net.start.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import net.start.model.Categories;
import net.start.model.Product;
import net.start.service.CategoriesService;
import net.start.service.ProductService;

@Controller
@RequestMapping("/menu")
public class MenuController {

    @Autowired
    private ProductService productService;

    @Autowired
    private CategoriesService categoriesService;

    @GetMapping("")
    public String listMenu(
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "sortField", defaultValue = "productId") String sortField,
            @RequestParam(value = "sortDir", defaultValue = "asc") String sortDir,
            Model model) {

        Page<Product> productPage = productService.findPaginated(keyword, page, 10, sortField, sortDir);
        model.addAttribute("products", productPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", productPage.getTotalPages());
        model.addAttribute("totalItems", productPage.getTotalElements());
        model.addAttribute("keyword", keyword);
        model.addAttribute("sortField", sortField);
        model.addAttribute("sortDir", sortDir);
        model.addAttribute("reverseSortDir", sortDir.equals("asc") ? "desc" : "asc");
        return "app";
    }

    @GetMapping("/new")
    public String showCreateForm(Model model) {
        Product product = new Product();
        product.setCategories(new Categories());
        model.addAttribute("product", product);
        model.addAttribute("categories", categoriesService.findAll());
        return "app";
    }

    @PostMapping("/save")
    public String saveMenu(@ModelAttribute("product") Product product) {
        productService.save(product);
        return "redirect:/menu";
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable("id") int id, Model model) {
        Product existingProduct = productService.findById(id);
        if (existingProduct.getCategories() == null) {
            existingProduct.setCategories(new Categories());
        }
        model.addAttribute("product", existingProduct);
        model.addAttribute("categories", categoriesService.findAll());
        return "app";
    }

    @PostMapping("/update/{id}")
    public String updateMenu(@PathVariable("id") int id, @ModelAttribute("product") Product productDetails) {
        Product existingProduct = productService.findById(id);
        existingProduct.setProductName(productDetails.getProductName());
        existingProduct.setProductPrice(productDetails.getProductPrice());
        existingProduct.setProductStatus(productDetails.getProductStatus());
        existingProduct.setProductDetail(productDetails.getProductDetail());
        existingProduct.setCategories(productDetails.getCategories());
        productService.save(existingProduct);
        return "redirect:/menu";
    }

    @GetMapping("/delete/{id}")
    public String deleteMenu(@PathVariable("id") int id) {
        productService.deleteById(id);
        return "redirect:/menu";
    }
}
