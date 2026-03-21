package net.start.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import net.start.model.OrderDetail;

public interface OrderDetailRepository extends JpaRepository<OrderDetail, Integer> {
}
