package com.example.books_rental.repository;

import com.example.books_rental.model.entities.Category;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CategoryRepository extends JpaRepository<Category, Integer> {
    Optional<Category> findById(Integer id);

    Optional<Category> findByName(String name);
}
