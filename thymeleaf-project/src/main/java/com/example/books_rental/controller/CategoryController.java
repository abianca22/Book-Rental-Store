package com.example.books_rental.controller;

import com.example.books_rental.model.entities.Category;
import com.example.books_rental.service.AuthenticationService;
import com.example.books_rental.service.CategoriesManagementService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/categories")
@Slf4j
public class CategoryController {

    private final CategoriesManagementService categoryService;
    private final AuthenticationService authenticationService;

    public CategoryController(CategoriesManagementService categoryService,
                              AuthenticationService authenticationService) {
        this.categoryService = categoryService;
        this.authenticationService = authenticationService;
    }

    @GetMapping
    public String listCategories(Model model, @RequestParam(value = "error", required = false) String error) {
        model.addAttribute("categories", categoryService.getAllCategories());
        model.addAttribute("currentUserRole", authenticationService.getCurrentUser() != null ? authenticationService.getCurrentUser().getRole().getName() : null);
        model.addAttribute("authenticated", authenticationService.checkIfAuthenticated());
        if(error != null) {
            model.addAttribute("error", error);
        }
        return "categories/list";
    }

    @GetMapping("/add")
    public String showAddForm(Model model) {
        model.addAttribute("category", new Category());
        model.addAttribute("currentUserRole", authenticationService.getCurrentUser() != null ? authenticationService.getCurrentUser().getRole().getName() : null);
        model.addAttribute("authenticated", authenticationService.checkIfAuthenticated());
        return "categories/form";
    }

    @PostMapping("/add")
    public String addCategory(@ModelAttribute @Valid Category category, BindingResult result, Model model) {
        if (result.hasErrors()) {
            model.addAttribute("category", category);
            model.addAttribute("currentUserRole", authenticationService.getCurrentUser() != null ? authenticationService.getCurrentUser().getRole().getName() : null);
            model.addAttribute("authenticated", authenticationService.checkIfAuthenticated());
            List<String> errors = result.getAllErrors().stream().filter(error -> !(error instanceof FieldError))
                    .map(DefaultMessageSourceResolvable::getDefaultMessage)
                    .toList();
            model.addAttribute("error", errors.isEmpty() ? null : errors);
            List<String> fieldErrors = result.getFieldErrors().stream()
                    .map(FieldError::getDefaultMessage)
                    .toList();
            this.logErrors(errors, fieldErrors);
            log.warn("Category could not be created");
            return "categories/form";
        }
        try {
            categoryService.addCategory(category);
        } catch (Exception e) {
            model.addAttribute("category", category);
            model.addAttribute("currentUserRole", authenticationService.getCurrentUser() != null ? authenticationService.getCurrentUser().getRole().getName() : null);
            model.addAttribute("authenticated", authenticationService.checkIfAuthenticated());
            model.addAttribute("error", e.getMessage());
            log.error("Error while adding category: {}", e.getMessage());
            return "categories/form";
        }
        log.info("Category {} created successfully", category.getName());
        return "redirect:/categories";
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable int id, Model model) {
        Category category = categoryService.getById(id);
        model.addAttribute("currentUserRole", authenticationService.getCurrentUser() != null ? authenticationService.getCurrentUser().getRole().getName() : null);
        model.addAttribute("authenticated", authenticationService.checkIfAuthenticated());
        model.addAttribute("category", category);
        return "categories/form";
    }

    @PostMapping("/edit/{id}")
    public String updateCategory(@PathVariable int id, @ModelAttribute @Valid Category category, BindingResult result, Model model) {
        if (result.hasErrors()) {
            model.addAttribute("currentUserRole", authenticationService.getCurrentUser() != null ? authenticationService.getCurrentUser().getRole().getName() : null);
            model.addAttribute("authenticated", authenticationService.checkIfAuthenticated());
            model.addAttribute("category", category);
            List<String> errors = result.getAllErrors().stream().filter(error -> !(error instanceof FieldError))
                    .map(DefaultMessageSourceResolvable::getDefaultMessage)
                    .toList();
            List<String> fieldErrors = result.getFieldErrors().stream()
                    .map(FieldError::getDefaultMessage)
                    .toList();
            model.addAttribute("error", errors.isEmpty() ? null : errors);
            this.logErrors(errors, fieldErrors);
            log.warn("Category could not be updated");
            return "categories/form";
        }
        category.setId(id);
        try {
            categoryService.updateCategory(category);
        } catch (Exception e) {
            model.addAttribute("currentUserRole", authenticationService.getCurrentUser() != null ? authenticationService.getCurrentUser().getRole().getName() : null);
            model.addAttribute("authenticated", authenticationService.checkIfAuthenticated());
            model.addAttribute("category", category);
            model.addAttribute("error", e.getMessage());
            log.error("Error while updating category: {}", e.getMessage());
            return "categories/form";
        }
        log.info("Category {} updated successfully", category.getName());
        return "redirect:/categories";
    }

    @PostMapping("/delete/{id}")
    public String deleteCategory(@PathVariable int id) {
        try {
            categoryService.deleteCategory(id);
        }
        catch (Exception e) {
            log.error("Error while deleting category with id {}: {}", id, e.getMessage());
            return "redirect:/categories?error=" + e.getMessage();
        }
        log.info("Category with id {} deleted successfully", id);
        return "redirect:/categories";
    }

    @GetMapping("/{id}")
    public String viewBooksByCategory(@PathVariable int id) {
        return "redirect:/books?categoryId=" + id;
    }

    public void logErrors(List<String> objectErrors, List<String> fieldErrors) {
        if (!objectErrors.isEmpty()) {
            log.error("Errors not related to fields: {}", objectErrors);
        }
        if (!fieldErrors.isEmpty()) {
            log.error("Field errors: {}", fieldErrors);
        }
    }
}
