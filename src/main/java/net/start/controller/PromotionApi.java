package net.start.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import net.start.model.Promotion;
import net.start.service.PromotionService;

@RestController
@RequestMapping("/api")
public class PromotionApi {

    @Autowired
    PromotionService promotionService;

    @GetMapping(value = "/promotions", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<Promotion>> findAll() {
        return new ResponseEntity<List<Promotion>>(promotionService.findAll(), HttpStatus.OK);
    }

    @GetMapping(value = "/promotions/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Promotion> findById(@PathVariable("id") int id) {
        return new ResponseEntity<Promotion>(promotionService.findById(id), HttpStatus.OK);
    }

}
