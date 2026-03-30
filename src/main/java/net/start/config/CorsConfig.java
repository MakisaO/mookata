package net.start.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**") // อนุญาตให้รับ Request ได้ในทุก Endpoint
                .allowedOrigins("*") // 💡 คุณสามารถเปลี่ยน "*" เป็นโดเมนที่ต้องการอนุญาตโดยเฉพาะได้ เช่น
                                     // "http://localhost:3000"
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH") // Method ที่อนุญาต
                .allowedHeaders("*"); // Header ที่อนุญาต
    }
}
