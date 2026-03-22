package net.start.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller // สำคัญ: ต้องใช้ @Controller เพื่อคืนค่าเป็นหน้า View (HTML)
public class HomeController {

    @GetMapping("/") // เข้าหน้าแรกด้วย URL: http://localhost:8080/
    public String index(Model model) {
        
        // ส่งข้อมูลจาก Java ไปยัง Thymeleaf (${userName})
        model.addAttribute("userName", "TheStar"); 
        
        // สมมติส่งสถานะระบบ (ในงาน SOA อาจจะเช็คว่า API เพื่อนพร้อมไหม)
        model.addAttribute("systemStatus", "Online & Ready");

        return "index"; // คืนชื่อไฟล์ HTML (Spring จะไปหาที่ templates/index.html เอง)
    }
}