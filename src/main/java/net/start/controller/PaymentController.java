package net.start.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import net.start.dto.CheckoutPageData;
import net.start.service.OrdermenuService;
import net.start.service.PaymentService;

@Controller
@RequestMapping("/payments")
public class PaymentController {

    @Autowired
    private OrdermenuService ordermenuService;

    @Autowired
    private PaymentService paymentService;

    @GetMapping("/checkout/table/{tableId}")
    public String showTableCheckout(@PathVariable("tableId") Integer tableId, Model model, RedirectAttributes redirectAttributes) {
        if (ordermenuService.getActiveOrdersByTable(tableId).isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "No active orders for this table");
            return "redirect:/tables";
        }

        CheckoutPageData checkoutPageData = paymentService.getCheckoutPageData(tableId);
        model.addAttribute("tableId", tableId);
        model.addAttribute("aggregatedOrders", checkoutPageData.aggregatedOrders());
        model.addAttribute("originalTotal", checkoutPageData.originalTotal());
        model.addAttribute("discount", checkoutPageData.discount());
        model.addAttribute("finalTotal", checkoutPageData.finalTotal());
        model.addAttribute("promoMessages", checkoutPageData.promoMessages());
        return "app";
    }

    @PostMapping("/save/table/{tableId}")
    public String processTablePayment(@PathVariable("tableId") Integer tableId, RedirectAttributes redirectAttributes) {
        try {
            paymentService.processTablePayment(tableId);
            redirectAttributes.addFlashAttribute("successMessage", "Payment completed for table " + tableId);
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Payment failed: " + e.getMessage());
            return "redirect:/payments/checkout/table/" + tableId;
        }

        return "redirect:/tables";
    }

    @PostMapping("/reset/{tableId}")
    public String resetTable(@PathVariable("tableId") Integer tableId, RedirectAttributes redirectAttributes) {
        try {
            paymentService.resetTableStatus(tableId);
            redirectAttributes.addFlashAttribute("successMessage", "Table " + tableId + " reset");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Reset failed: " + e.getMessage());
        }
        return "redirect:/tables";
    }
}
