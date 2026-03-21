package net.start.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import net.start.model.Product; // บอก Jpa ว่าเราจะจัดการข้อมูลของ Model Product

@Repository
public interface MenuRepository extends JpaRepository<Product, Integer> {
    // JpaRepository จะเตรียมคำสั่ง findAll, findById, save, delete มาให้เราใช้ฟรีๆ ครับ
}