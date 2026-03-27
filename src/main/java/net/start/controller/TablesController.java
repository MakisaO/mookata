package net.start.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import net.start.model.Tables;
import net.start.service.TablesService;

@Controller
@RequestMapping("/tables")
public class TablesController {

    @Autowired
    private TablesService tablesService;

    @GetMapping("")
    public String listTables(Model model) {
        model.addAttribute("tables", tablesService.findAll());
        return "app";
    }

    @GetMapping("/new")
    public String showCreateForm(Model model) {
        model.addAttribute("table", new Tables());
        return "app";
    }

    @PostMapping("/save")
    public String saveTable(@ModelAttribute("table") Tables tables) {
        tablesService.save(tables);
        return "redirect:/tables";
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable("id") int id, Model model) {
        model.addAttribute("table", tablesService.findById(id));
        return "app";
    }

    @PostMapping("/update/{id}")
    public String updateTable(@PathVariable("id") int id, @ModelAttribute("table") Tables tableDetails) {
        Tables existingTable = tablesService.findById(id);
        existingTable.setStatus(tableDetails.getStatus());
        tablesService.save(existingTable);
        return "redirect:/tables";
    }

    @GetMapping("/delete/{id}")
    public String deleteTable(@PathVariable("id") int id) {
        tablesService.deleteById(id);
        return "redirect:/tables";
    }

    @GetMapping("/{id}")
    public String showTableDetails(@PathVariable("id") int id, Model model) {
        model.addAttribute("table", tablesService.findById(id));
        return "app";
    }
}
