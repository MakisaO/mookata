package net.start.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import net.start.model.Categories;

@Repository
public interface CategoriesRepository extends JpaRepository<Categories, Integer> {
}
