package net.start.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import net.start.model.Coupon;
import net.start.service.CouponService;

@RestController
@RequestMapping("/api/coupons")
public class CouponController {

    @Autowired
    private CouponService couponService;

    // 1. ดึงคูปองทั้งหมด
    @GetMapping
    public List<Coupon> getAllCoupons() {
        return couponService.getAllCoupons();
    }

    // 2. ดึงคูปองทีละใบดัวย ID
    @GetMapping("/{id}")
    public ResponseEntity<Coupon> getCouponById(@PathVariable Long id) {
        return couponService.getCouponById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // 3. สร้างคูปองใหม่ และส่งรหัสที่สุ่มได้ (หรือที่ระบุมา) กลับไปดัวย
    @PostMapping
    public ResponseEntity<Coupon> createCoupon(@RequestBody Coupon coupon) {
        try {
            // ระบบจะทำการสุ่มโค้ด TSM-XXXXXX ให้ที่นี่ และบันทึก
            Coupon savedCoupon = couponService.createCoupon(coupon);
            return ResponseEntity.ok(savedCoupon); // คืนค่าข้อมูลคูปองทัั้งใบ (มีโค้ดอยู่ข้างใน)
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // 4. ตรวจสอบสถานะคูปอง (ใช้สำหรับหน้าจอ Check Status)
    @GetMapping("/validate/{code}")
    public ResponseEntity<Coupon> validateCoupon(@PathVariable String code) {
        // ดึงคูปองมาดูทุกสถานะ (แม้จะเป็น USED หรือ CANCELED)
        return couponService.getCouponByCode(code)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // 5. ใช้งานคูปอง (เปลี่ยนสถานะเป็นอะไรก็ได้ตามสั่ง)
    @PostMapping("/use/{code}")
    public ResponseEntity<String> useCoupon(@PathVariable String code) {
        boolean success = couponService.updateCouponStatus(code, "USED");
        if (success) return ResponseEntity.ok("Coupon used successfully");
        return ResponseEntity.badRequest().body("Invalid coupon code");
    }

    // 5.1 อัปเดตสถานะตรงๆ (PATCH /api/coupons/status/TSM-123/USED)
    @PostMapping("/status/{code}/{status}")
    public ResponseEntity<String> updateStatus(@PathVariable String code, @PathVariable String status) {
        boolean success = couponService.updateCouponStatus(code, status);
        if (success) return ResponseEntity.ok("Status updated: " + status);
        return ResponseEntity.badRequest().body("Failed to update status");
    }

    // 6. ยกเลิกคูปอง (DELETE /api/coupons/12)
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCoupon(@PathVariable Long id) {
        couponService.deleteCoupon(id);
        return ResponseEntity.ok().build();
    }
}
