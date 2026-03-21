package net.start.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import net.start.model.Ordermenu;

public interface OrdermenuRepository extends JpaRepository<Ordermenu, Integer> {
}
