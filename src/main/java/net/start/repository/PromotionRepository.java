package net.start.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import net.start.model.Promotion;

public interface PromotionRepository extends JpaRepository<Promotion, Integer> {

}
