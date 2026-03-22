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

	// Read: แสดงรายการโต๊ะทั้งหมด
	@GetMapping("")
	public String listTables(Model model) {
		model.addAttribute("tables", tablesService.findAll());
		return "tables/list"; // แนะนำไปที่ไฟล์ src/main/resources/templates/tables/list.html
	}

	// Create: แสดงหน้าฟอร์มเพิ่มโต๊ะ
	@GetMapping("/new")
	public String showCreateForm(Model model) {
		model.addAttribute("table", new Tables());
		return "tables/form"; // ใช้ไฟล์ form.html เดียวกันทั้งเพิ่มและแก้
	}

	// Create: รับข้อมูลจากฟอร์มและบันทึกลง DB
	@PostMapping("/save")
	public String saveTable(@ModelAttribute("table") Tables tables) {
		tablesService.save(tables);
		return "redirect:/tables"; // บันทึกเสร็จ กลับไปหน้า list
	}

	// Update: แสดงฟอร์มแก้ไขสถานะโต๊ะ
	@GetMapping("/edit/{id}")
	public String showEditForm(@PathVariable("id") int id, Model model) {
		Tables existingTable = tablesService.findById(id);
		model.addAttribute("table", existingTable);
		return "tables/form";
	}

	// Update: อัปเดตข้อมูล (ทำงานผ่าน URL /save เหมือน Create ได้เลย หรือแยกก็ได้)
	@PostMapping("/update/{id}")
	public String updateTable(@PathVariable("id") int id, @ModelAttribute("table") Tables tableDetails) {
		Tables existingTable = tablesService.findById(id);
		existingTable.setStatus(tableDetails.getStatus());
		tablesService.save(existingTable);
		return "redirect:/tables";
	}



	// Read: แสดงรายละเอียดโต๊ะ (ดูรายการอาหารที่สั่ง)
	@GetMapping("/{id}")
	public String showTableDetails(@PathVariable("id") int id, Model model) {
		Tables existingTable = tablesService.findById(id);
		model.addAttribute("table", existingTable);
		return "tables/detail";
	}

}
