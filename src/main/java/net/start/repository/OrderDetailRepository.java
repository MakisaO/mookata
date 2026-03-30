package net.start.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import net.start.model.OrderDetail;

public interface OrderDetailRepository extends JpaRepository<OrderDetail, Integer> {
    List<OrderDetail> findByItemStatusInOrderByDetailIdAsc(List<String> statuses);

    @Query("SELECT od.product, SUM(od.quantity) as total FROM OrderDetail od " +
           "WHERE od.ordermenu.orderStatus = 'paid' " +
           "GROUP BY od.product ORDER BY total DESC")
    List<Object[]> findTopSellingProducts(Pageable pageable);

    @Query("SELECT p, COALESCE(SUM(od.quantity), 0) as total FROM Product p " +
           "LEFT JOIN OrderDetail od ON p = od.product AND od.ordermenu.orderStatus = 'paid' " +
           "GROUP BY p ORDER BY total ASC")
    List<Object[]> findLeastSellingProducts(Pageable pageable);

    @Query("SELECT od FROM OrderDetail od JOIN FETCH od.ordermenu o LEFT JOIN FETCH o.tables " +
           "WHERE od.product.productId = :productId AND o.orderStatus IN ('paid', 'completed') " +
           "ORDER BY o.orderDate DESC")
    List<OrderDetail> findSalesHistoryByProduct(@Param("productId") Integer productId);
}
