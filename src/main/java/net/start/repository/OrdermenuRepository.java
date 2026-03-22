package net.start.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import net.start.model.Ordermenu;

public interface OrdermenuRepository extends JpaRepository<Ordermenu, Integer> {
	List<Ordermenu> findByTables_TableIdAndOrderStatusNot(Integer tableId, String status);
}
