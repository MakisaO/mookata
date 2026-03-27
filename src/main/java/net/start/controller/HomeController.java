package net.start.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller // สำคัญ: ต้องใช้ @Controller เพื่อคืนค่าเป็นหน้า View (HTML)
public class HomeController {

    @GetMapping("/") // เข้าหน้าแรกด้วย URL: http://localhost:8080/
    public String index() {
        return "app";
    }
}
