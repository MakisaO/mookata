package net.start.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import net.start.model.Tables;
import net.start.repository.TablesRepository;

@Service
public class TablesService {

	@Autowired
	TablesRepository tablesRepository;

	// Read: ดึงข้อมูลโต๊ะทั้งหมด
	public List<Tables> findAll() {
		return tablesRepository.findAll();
	}

	public List<Tables> findByStatus(String status) {
		return tablesRepository.findByStatus(status);
	}

	// Read: ดึงข้อมูลโต๊ะตาม ID
	public Tables findById(int id) {
		return tablesRepository.findById(id).orElseThrow();
	}

	// Create / Update: บันทึกข้อมูลโต๊ะ (ไม่มีฟังก์ชัน Delete ตามรีเควส)
	public Tables save(Tables tables) {
		return tablesRepository.save(tables);
	}

}
