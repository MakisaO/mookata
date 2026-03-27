package net.start.controller;

import java.beans.PropertyEditorSupport;
import java.sql.Timestamp;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import net.start.model.Product;
import net.start.model.Promotion;
import net.start.service.ProductService;
import net.start.service.PromotionService;

@Controller
@RequestMapping("/promotion")
public class PromotionController {

    @Autowired
    private PromotionService promotionService;

    @Autowired
    private ProductService productService;

    @InitBinder
    protected void initBinder(WebDataBinder binder) {
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
                if (value instanceof Timestamp timestamp) {
                    return timestamp.toString().replace(" ", "T").substring(0, 16);
                }
                return "";
            }
        });

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

    @GetMapping("")
    public String listPromotions(Model model) {
        model.addAttribute("promotions", promotionService.findAll());
        return "app";
    }

    @GetMapping("/new")
    public String showCreateForm(Model model) {
        if (!model.containsAttribute("promotion")) {
            model.addAttribute("promotion", new Promotion());
        }
        model.addAttribute("products", productService.findAll());
        return "app";
    }

    @PostMapping("/save")
    public String savePromotion(@ModelAttribute("promotion") Promotion promotion, RedirectAttributes redirectAttributes, Model model) {
        try {
            promotionService.save(promotion);
            redirectAttributes.addFlashAttribute("successMessage", "Promotion saved");
            return "redirect:/promotion";
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Failed to save promotion: " + e.getMessage());
            model.addAttribute("products", productService.findAll());
            return "app";
        }
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable("id") int id, Model model) {
        if (!model.containsAttribute("promotion")) {
            model.addAttribute("promotion", promotionService.findById(id));
        }
        model.addAttribute("products", productService.findAll());
        return "app";
    }

    @PostMapping("/update/{id}")
    public String updatePromotion(@PathVariable("id") int id, @ModelAttribute("promotion") Promotion promotion, RedirectAttributes redirectAttributes, Model model) {
        try {
            promotion.setPromotionId(id);
            promotionService.save(promotion);
            redirectAttributes.addFlashAttribute("successMessage", "Promotion updated");
            return "redirect:/promotion";
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Failed to update promotion: " + e.getMessage());
            model.addAttribute("products", productService.findAll());
            return "app";
        }
    }

    @GetMapping("/delete/{id}")
    public String deletePromotion(@PathVariable("id") int id, RedirectAttributes redirectAttributes) {
        try {
            promotionService.deleteById(id);
            redirectAttributes.addFlashAttribute("successMessage", "Promotion deleted");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Unable to delete promotion: " + e.getMessage());
        }
        return "redirect:/promotion";
    }
}
