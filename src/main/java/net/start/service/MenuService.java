package net.start.service;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import net.start.model.Product; // กูใช้ Model Product เป็นข้อมูลเมนู
import net.start.repository.MenuRepository; // เดี๋ยวเราจะสร้าง Repository ตัวนี้กัน

@Service
public class MenuService {

    @Autowired
    private MenuRepository menuRepository;

    // 1. ดึงรายการเมนูอาหารทั้งหมดในร้าน
    public List<Product> findAll() {
        return menuRepository.findAll();
    }

    // 2. ค้นหาเมนูอาหารตาม ID (ใช้ตอนกด 'แก้ไข' ในหน้าเว็บ)
    public Product findById(int id) {
        // ถ้าหาไม่เจอให้คืนค่า null
        return menuRepository.findById(id).orElse(null);
    }

    // 3. บันทึกข้อมูลเมนูใหม่ หรืออัปเดตข้อมูลเมนูเดิม
    public Product save(Product product) {
        return menuRepository.save(product);
    }

    // 4. ลบเมนูอาหารออกจากระบบ (เผื่อกูทำปุ่มลบในอนาคต)
    public void deleteById(int id) {
        menuRepository.deleteById(id);
    }
}