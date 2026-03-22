package net.start.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import net.start.model.OrderDetail;

public interface OrderDetailRepository extends JpaRepository<OrderDetail, Integer> {
    List<OrderDetail> findByItemStatusInOrderByDetailIdAsc(List<String> statuses);
}
