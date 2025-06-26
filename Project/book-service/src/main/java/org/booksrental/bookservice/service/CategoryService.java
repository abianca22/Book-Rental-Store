package org.booksrental.bookservice.service;

import org.booksrental.bookservice.exception.ExistingDataException;
import org.booksrental.bookservice.exception.NotFoundException;
import org.booksrental.bookservice.model.entity.Category;
import org.booksrental.bookservice.repository.BookRepository;
import org.booksrental.bookservice.repository.CategoryRepository;
import org.booksrental.bookservice.validator.Validator;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CategoryService {

    private final CategoryRepository categoryRepository;

    private final BookRepository bookRepository;

    public CategoryService(CategoryRepository categoryRepository,
                           BookRepository bookRepository) {
        this.categoryRepository = categoryRepository;
        this.bookRepository = bookRepository;
    }

    public Category addCategory(Category category) {
        Optional<Category> alreadyExistingCategory = categoryRepository.findByName(category.getName());
//        if(user.getRole().getName().equals("client")) {
//            throw new AccessDeniedException("Categories can only be added by staff members!");
//        }
        if (alreadyExistingCategory.isPresent()) {
            throw new ExistingDataException("Category with name " + category.getName() + " already exists!");
        }
        return categoryRepository.save(category);
    }

    public Category updateCategory(Category category) {
        Optional<Category> foundCategoryById = categoryRepository.findById(category.getId());
        Optional<Category> foundCategoryByName = categoryRepository.findByName(category.getName());
//        User user = authenticationService.getCurrentUser();
//        if (user.getRole().getName().equals("client")) {
//            throw new AccessDeniedException("Categories can only be updated by staff members!");
//        }
        if (foundCategoryById.isEmpty()) {
            throw new NotFoundException("Category with id " + category.getId() + " does not exist!");
        }
        if (foundCategoryByName.isPresent() && foundCategoryByName.get().getId() != category.getId()) {
            throw new ExistingDataException("Category with name " + category.getName() + " already exists!");
        }
        if(!foundCategoryById.get().getName().equals(category.getName()) && category.getName() != null) {
            foundCategoryById.get().setName(category.getName());
        }
        if(category.getDescription() != null && !foundCategoryById.get().getDescription().equals(category.getDescription())) {
            foundCategoryById.get().setDescription(category.getDescription());
        }
        Validator.validateObject(foundCategoryById.get());
        return categoryRepository.save(foundCategoryById.get());
    }

    public void deleteCategory(int id) {
//        User user = authenticationService.getCurrentUser();
        Optional<Category> category = categoryRepository.findById(id);
//        if (user.getRole().getName().equals("client")) {
//            throw new AccessDeniedException("Categories can only be deleted by staff members!");
//        }
        if (category.isEmpty()) {
            throw new NotFoundException("Category with id " + id + " does not exist!");
        }
        if (category.get().getBooks() != null && !category.get().getBooks().isEmpty()) {
            for (var book : category.get().getBooks()) {
                book.getCategories().remove(category.get());
                book.setCategories(book.getCategories());
                bookRepository.save(book);
            }
        }
        categoryRepository.deleteById(id);
    }

    public Category getById(Integer id) {
        Optional<Category> category = categoryRepository.findById(id);
        if (category.isEmpty()) {
            throw new NotFoundException("Category with id " + id + " does not exist!");
        }
        return category.get();
    }

    public List<Category> getAllCategories() {
        return categoryRepository.findAll();
    }

    public void deleteAllCategories() {
        for (var cat: categoryRepository.findAll()) {
            deleteCategory(cat.getId());
        }
    }

    public List<Category> findAllById(List<Integer> ids) {
        return categoryRepository.findAllById(ids);
    }
}

