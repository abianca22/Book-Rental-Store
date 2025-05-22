package com.example.books_rental.repository;

import com.example.books_rental.model.entities.Return;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReturnRepository extends JpaRepository<Return, Integer> {
}
