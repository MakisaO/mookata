package net.start.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import net.start.model.Tables;
import net.start.repository.TablesRepository;

@Service
public class TablesService {

	@Autowired
	TablesRepository tablesRepository;

	public List<Tables> findAll() {
		return tablesRepository.findAll();
	}

	public List<Tables> findByStatus(String status) {
		return tablesRepository.findByStatus(status);
	}

	public Tables findById(int id) {
		return tablesRepository.findById(id)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "ไม่พบโต๊ะที่ต้องการ"));
	}

	public Tables save(Tables tables) {
		return tablesRepository.save(tables);
	}

	public void deleteById(int id) {
		tablesRepository.deleteById(id);
	}
}
