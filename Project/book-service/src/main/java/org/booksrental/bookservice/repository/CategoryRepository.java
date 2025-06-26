package org.booksrental.bookservice.repository;


import org.booksrental.bookservice.model.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CategoryRepository extends JpaRepository<Category, Integer> {
    Optional<Category> findById(Integer id);

    Optional<Category> findByName(String name);
}
