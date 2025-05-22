package com.example.books_rental.service;

import com.example.books_rental.exception.AccessDeniedException;
import com.example.books_rental.exception.ExistingDataException;
import com.example.books_rental.exception.NotFoundException;
import com.example.books_rental.model.entities.Category;
import com.example.books_rental.model.entities.User;
import com.example.books_rental.repository.BookRepository;
import com.example.books_rental.repository.CategoryRepository;
import com.example.books_rental.repository.UserRepository;
import com.example.books_rental.validator.Validator;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CategoriesManagementService {
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;
    private final AuthenticationService authenticationService;
    private final BookRepository bookRepository;

    public CategoriesManagementService(CategoryRepository categoryRepository,
                                       UserRepository userRepository,
                                       AuthenticationService authenticationService, BookRepository bookRepository) {
        this.categoryRepository = categoryRepository;
        this.userRepository = userRepository;
        this.authenticationService = authenticationService;
        this.bookRepository = bookRepository;
    }

    public Category addCategory(Category category) {
        Optional<Category> alreadyExistingCategory = categoryRepository.findByName(category.getName());
        User user = authenticationService.getCurrentUser();
        if(user.getRole().getName().equals("client")) {
            throw new AccessDeniedException("Categories can only be added by staff members!");
        }
        if (alreadyExistingCategory.isPresent()) {
            throw new ExistingDataException("Category with name " + category.getName() + " already exists!");
        }
        return categoryRepository.save(category);
    }

    public Category updateCategory(Category category) {
        Optional<Category> foundCategoryById = categoryRepository.findById(category.getId());
        Optional<Category> foundCategoryByName = categoryRepository.findByName(category.getName());
        User user = authenticationService.getCurrentUser();
        if (user.getRole().getName().equals("client")) {
            throw new AccessDeniedException("Categories can only be updated by staff members!");
        }
        if (foundCategoryById.isEmpty()) {
            throw new NotFoundException("Category with id " + category.getId() + " does not exist!");
        }
        if (foundCategoryByName.isPresent() && foundCategoryByName.get().getId() != category.getId()) {
            throw new ExistingDataException("Category with name " + category.getName() + " already exists!");
        }
        if(!foundCategoryById.get().getName().equals(category.getName()) && category.getName() != null) {
            foundCategoryById.get().setName(category.getName());
        }
        if(!foundCategoryById.get().getDescription().equals(category.getDescription()) && category.getDescription() != null) {
            foundCategoryById.get().setDescription(category.getDescription());
        }
        Validator.validateObject(foundCategoryById.get());
        return categoryRepository.save(foundCategoryById.get());
    }

    public void deleteCategory(int id) {
        User user = authenticationService.getCurrentUser();
        Optional<Category> category = categoryRepository.findById(id);
        if (user.getRole().getName().equals("client")) {
            throw new AccessDeniedException("Categories can only be deleted by staff members!");
        }
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
