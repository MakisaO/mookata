package net.start.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import net.start.model.Tables;

public interface TablesRepository extends JpaRepository<Tables, Integer> {

	List<Tables> findByStatus(String status);

}
