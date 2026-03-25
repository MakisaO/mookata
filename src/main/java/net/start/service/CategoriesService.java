package net.start.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import net.start.model.Categories;
import net.start.repository.CategoriesRepository;

@Service
public class CategoriesService {

    @Autowired
    private CategoriesRepository categoriesRepository;

    public List<Categories> findAll() {
        return categoriesRepository.findAll();
    }

    public Categories findById(int id) {
        return categoriesRepository.findById(id).orElse(null);
    }
}
