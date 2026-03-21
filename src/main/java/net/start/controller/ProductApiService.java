package net.start.api;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import net.start.model.Product;
import net.start.service.ProductService;

@RestController
@RequestMapping("/api")
public class ProductApiService {

    @Autowired
    ProductService productService;

    @GetMapping(value = "/products", produces = "application/json")
    public ResponseEntity<List<Product>> findAll() {
        return new ResponseEntity<List<Product>>(productService.findAll(), HttpStatus.OK);
    }

    @GetMapping(value = "/products/{id}", produces = "application/json")
    public ResponseEntity<Product> findById(@PathVariable("id") int id) {
        return new ResponseEntity<Product>(productService.findById(id), HttpStatus.OK);
    }

    @GetMapping(value = "/products/available", produces = "application/json")
    public ResponseEntity<List<Product>> findAvailable() {
        return new ResponseEntity<List<Product>>(productService.findAvailable(), HttpStatus.OK);
    }

}
