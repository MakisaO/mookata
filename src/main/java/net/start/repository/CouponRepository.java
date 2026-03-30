package net.start.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import net.start.model.Coupon;

@Repository
public interface CouponRepository extends JpaRepository<Coupon, Long> {
    
    // เพิ่ม Method สำหรับหา Coupon จาก Code
    Optional<Coupon> findByCode(String code);
    
    // เพิ่ม Method สำหรับเช็คว่ามี Code นี้อยู่หรือยัง
    boolean existsByCode(String code);
}
