package net.start.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import net.start.model.Coupon;
import net.start.repository.CouponRepository;

@Service
public class CouponService {

    @Autowired
    private CouponRepository couponRepository;

    // 1. ดึงคูปองทั้งหมด
    public List<Coupon> getAllCoupons() {
        return couponRepository.findAll();
    }

    // 2. ดึงคูปองด้วย ID
    public Optional<Coupon> getCouponById(Long id) {
        return couponRepository.findById(id);
    }

    // 2.2 ดึงคูปองดัวย Code (ใช้ตรวจสอบสถานะ)
    public Optional<Coupon> getCouponByCode(String code) {
        return couponRepository.findByCode(code);
    }

    // 3. สร้างคูปองใหม่ (สุ่มรหัสให้เองอัตโนมัติ)
    public Coupon createCoupon(Coupon coupon) {
        // หากไม่มีการระบุ Code มาให้ ระบบจะสุ่มให้ตาม format TSM-XXXXXX
        if (coupon.getCode() == null || coupon.getCode().trim().isEmpty()) {
            coupon.setCode(generateCouponCode());
        } else {
            // หากระบุมาเอง ก็ต้องเช็คว่าซ้ำไหม
            if (couponRepository.existsByCode(coupon.getCode())) {
                throw new RuntimeException("Coupon code already exists: " + coupon.getCode());
            }
        }
        return couponRepository.save(coupon);
    }

    // ฟังก์ชันสุ่มรหัส TSM-XXXXXX (และเช็คซ้ำใน DB)
    private String generateCouponCode() {
        java.util.Random random = new java.util.Random();
        String code;
        do {
            int randomNumber = 100000 + random.nextInt(900000); // สุ่มเลข 100,000 - 999,999 (6 หลัก)
            code = "TSM-" + randomNumber;
        } while (couponRepository.existsByCode(code)); // ถ้าสุ่มได้เลขที่เคยมีใน DB แล้วให้สุ่มใหม่
        return code;
    }

    // 4. ตรวจสอบคูปองดัวย Code (ตรวจสอบว่าใช้ได้หรือไม่)
    public Optional<Coupon> validateCoupon(String code) {
        Optional<Coupon> couponOpt = couponRepository.findByCode(code);
        
        if (couponOpt.isPresent()) {
            Coupon coupon = couponOpt.get();
            // เช็คสถานะว่าต้องเป็น ACTIVE เท่านั้นถึงจะใช้ได้
            if ("ACTIVE".equalsIgnoreCase(coupon.getStatus())) {
                return Optional.of(coupon);
            }
        }
        return Optional.empty();
    }

    // 5. เปลี่ยนสถานะคูปองเป็น USED (หลังจากใช้งานแล้ว)
    public boolean useCoupon(String code) {
        Optional<Coupon> couponOpt = couponRepository.findByCode(code);
        
        if (couponOpt.isPresent() && "ACTIVE".equalsIgnoreCase(couponOpt.get().getStatus())) {
            Coupon coupon = couponOpt.get();
            coupon.setStatus("USED");
            couponRepository.save(coupon);
            return true;
        }
        return false;
    }

    // 6. ยกเลิกคูปอง (Soft Delete)
    public void deleteCoupon(Long id) {
        Optional<Coupon> couponOpt = couponRepository.findById(id);
        if (couponOpt.isPresent()) {
            Coupon coupon = couponOpt.get();
            coupon.setStatus("CANCELED");
            couponRepository.save(coupon);
        }
    }
}
