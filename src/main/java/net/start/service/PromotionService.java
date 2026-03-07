package net.start.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import net.start.model.Promotion;
import net.start.repository.PromotionRepository;

@Service
public class PromotionService {

    @Autowired
    PromotionRepository promotionRepository;

    public List<Promotion> findAll() {
        return promotionRepository.findAll();
    }

    public Promotion findById(int id) {
        return promotionRepository.findById(id).orElseThrow();
    }

    public void save(Promotion promotion) {
        promotionRepository.save(promotion);
    }

    public void deleteById(int id) {
        promotionRepository.deleteById(id);
    }

}
