package net.start.controller;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import net.start.model.Tables;
import net.start.service.TablesService;

@RestController
@RequestMapping("/api/tables")
public class TablesController {

	@Autowired
	private TablesService tablesService;

	@GetMapping("")
	public ResponseEntity<List<Tables>> listTables() {
		return ResponseEntity.ok(tablesService.findAll());
	}

	@GetMapping("/{id}")
	public ResponseEntity<Tables> getTable(@PathVariable("id") int id) {
		return ResponseEntity.ok(tablesService.findById(id));
	}

	@PostMapping("")
	public ResponseEntity<Tables> createTable(@RequestBody Tables tableRequest) {
		Tables table = new Tables();
		table.setStatus(tableRequest.getStatus());
		return ResponseEntity.status(HttpStatus.CREATED).body(tablesService.save(table));
	}

	@PutMapping("/{id}")
	public ResponseEntity<Tables> updateTable(@PathVariable("id") int id, @RequestBody Tables tableDetails) {
		Tables existingTable = tablesService.findById(id);
		existingTable.setStatus(tableDetails.getStatus());
		return ResponseEntity.ok(tablesService.save(existingTable));
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<Map<String, Object>> deleteTable(@PathVariable("id") int id) {
		Tables existingTable = tablesService.findById(id);
		if ("unavailable".equalsIgnoreCase(existingTable.getStatus())) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "ไม่สามารถลบโต๊ะที่กำลังใช้งานอยู่");
		}

		tablesService.deleteById(id);
		return ResponseEntity.ok(Map.of(
				"success", true,
				"deletedId", id));
	}
}
