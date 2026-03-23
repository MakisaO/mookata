package net.start.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.bind.WebDataBinder;
import java.beans.PropertyEditorSupport;
import java.sql.Timestamp;

import net.start.model.Promotion;
import net.start.model.Product;
import net.start.service.PromotionService;
import net.start.service.ProductService;

@Controller
@RequestMapping("/promotion")
public class PromotionController {

    @InitBinder
    protected void initBinder(WebDataBinder binder) {
        // Date Editor
        binder.registerCustomEditor(Timestamp.class, new PropertyEditorSupport() {
            @Override
            public void setAsText(String text) {
                if (text == null || text.isBlank()) {
                    setValue(null);
                } else {
                    String sanitized = text.replace("T", " ");
                    if (sanitized.length() == 10) sanitized += " 00:00:00";
                    else if (sanitized.length() == 16) sanitized += ":00";
                    try {
                        setValue(Timestamp.valueOf(sanitized));
                    } catch (IllegalArgumentException e) {
                        setValue(null);
                    }
                }
            }

            @Override
            public String getAsText() {
                Object value = getValue();
                if (value instanceof Timestamp) {
                    return ((Timestamp) value).toString().replace(" ", "T").substring(0, 16);
                }
                return "";
            }
        });

        // Product Editor for binding relationships from IDs
        binder.registerCustomEditor(Product.class, new PropertyEditorSupport() {
            @Override
            public void setAsText(String text) {
                if (text == null || text.isBlank()) {
                    setValue(null);
                } else {
                    try {
                        setValue(productService.findById(Integer.parseInt(text)));
                    } catch (Exception e) {
                        setValue(null);
                    }
                }
            }
        });
    }

    @Autowired
    private PromotionService promotionService;

    @Autowired
    private ProductService productService;

    @GetMapping("")
    public String listPromotions(Model model) {
        model.addAttribute("promotions", promotionService.findAll());
        return "promotions/list";
    }

    @GetMapping("/new")
    public String showCreateForm(Model model) {
        if (!model.containsAttribute("promotion")) {
            model.addAttribute("promotion", new Promotion());
        }
        model.addAttribute("products", productService.findAll());
        return "promotions/form";
    }

    @PostMapping("/save")
    public String savePromotion(@ModelAttribute("promotion") Promotion promotion, RedirectAttributes redirectAttributes, Model model) {
        try {
            promotionService.save(promotion);
            redirectAttributes.addFlashAttribute("successMessage", "บันทึกโปรโมชั่นเรียบร้อยแล้ว");
            return "redirect:/promotion";
        } catch (Exception e) {
            model.addAttribute("errorMessage", "เกิดข้อผิดพลาด: " + e.getMessage());
            model.addAttribute("products", productService.findAll());
            return "promotions/form";
        }
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable("id") int id, Model model) {
        if (!model.containsAttribute("promotion")) {
            Promotion promotion = promotionService.findById(id);
            model.addAttribute("promotion", promotion);
        }
        model.addAttribute("products", productService.findAll());
        return "promotions/form";
    }

    @PostMapping("/update/{id}")
    public String updatePromotion(@PathVariable("id") int id, @ModelAttribute("promotion") Promotion promotion, RedirectAttributes redirectAttributes, Model model) {
        try {
            promotion.setPromotionId(id);
            promotionService.save(promotion);
            redirectAttributes.addFlashAttribute("successMessage", "อัปเดตโปรโมชั่นเรียบร้อยแล้ว");
            return "redirect:/promotion";
        } catch (Exception e) {
            model.addAttribute("errorMessage", "เกิดข้อผิดพลาด: " + e.getMessage());
            model.addAttribute("products", productService.findAll());
            return "promotions/form";
        }
    }

    @GetMapping("/delete/{id}")
    public String deletePromotion(@PathVariable("id") int id, RedirectAttributes redirectAttributes) {
        try {
            promotionService.deleteById(id);
            redirectAttributes.addFlashAttribute("successMessage", "ลบโปรโมชั่นเรียบร้อยแล้ว");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "ไม่สามารถลบได้: " + e.getMessage());
        }
        return "redirect:/promotion";
    }
}
