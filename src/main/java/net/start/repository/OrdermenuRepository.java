package net.start.repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import net.start.model.Ordermenu;

public interface OrdermenuRepository extends JpaRepository<Ordermenu, Integer> {
	
	List<Ordermenu> findByTables_TableIdAndOrderStatusNot(Integer tableId, String status);

	List<Ordermenu> findByOrderStatusOrderByOrderDateDesc(String status);

	Page<Ordermenu> findByOrderStatusOrderByOrderDateDesc(String status, Pageable pageable);
	
	Optional <Ordermenu> findFirstByTables_TableIdAndOrderStatusNotOrderByOrderDateDesc(Integer tableId, String status);

    @Query("SELECT SUM(o.totalAmount) FROM Ordermenu o WHERE o.orderStatus = 'paid'")
    BigDecimal getTotalRevenue();
}
