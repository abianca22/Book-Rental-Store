package org.booksrental.bookservice.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.booksrental.bookservice.model.dto.CreateCategoryDTO;
import org.booksrental.bookservice.model.dto.UpdateBookDTO;
import org.booksrental.bookservice.model.dto.UpdateCategoryDTO;
import org.booksrental.bookservice.model.entity.Category;
import org.booksrental.bookservice.model.mapper.CategoryMapper;
import org.booksrental.bookservice.service.CategoryService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/categories")
public class CategoryController {
    private final CategoryService categoryService;
    private final CategoryMapper categoryMapper;

    public CategoryController(CategoryService categoryService, CategoryMapper categoryMapper) {
        this.categoryService = categoryService;
        this.categoryMapper = categoryMapper;
    }

    @PostMapping
    public ResponseEntity<Category> create(@RequestBody @Valid CreateCategoryDTO createCategoryDto, HttpServletRequest request) {
        Category category = categoryMapper.toCategory(createCategoryDto);
        Category createdCategory = categoryService.addCategory(category, getToken(request));
        return ResponseEntity.created(URI.create("/categories/" + createdCategory.getId()))
                .body(category);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Category> update(@PathVariable Integer id, @RequestBody @Valid UpdateCategoryDTO updateCategoryDto, HttpServletRequest request) {
        if (!id.equals(updateCategoryDto.getId())) {
            throw new IllegalArgumentException("Category ID in the path does not match the ID in the request body.");
        }
        Category category = categoryMapper.toCategory(updateCategoryDto);
        return ResponseEntity.ok().body(categoryService.updateCategory(category, getToken(request)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> delete(@PathVariable Integer id, HttpServletRequest request) {
        categoryService.deleteCategory(id, getToken(request));
        return ResponseEntity.ok().body("Category with id " + id + " was deleted successfully!");
    }

    @GetMapping("/{id}")
    public ResponseEntity<Category> get(@PathVariable Integer id) {
        return ResponseEntity.ok().body(categoryService.getById(id));
    }

    @GetMapping("/all")
    public ResponseEntity<List<Category>> getAll() {
        return ResponseEntity.ok().body(categoryService.getAllCategories());
    }

    String getToken(HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            return token.substring(7);
        }
        return null;
    }
}

