package com.example.books_rental.repository;

import com.example.books_rental.model.entities.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Integer> {
    @Query("""
            SELECT u FROM User u where u.username = :username
""")
    Optional<User> findUserByUsername(String username);

    Optional<User> findByEmail(String email);

    Optional<User> findByPhone(String phone);

    @Query("""
    SELECT u FROM User u
    WHERE (:keyword = '' OR
         LOWER(u.username) LIKE LOWER(CONCAT('%', :keyword, '%')) OR
         LOWER(u.firstname) LIKE LOWER(CONCAT('%', :keyword, '%')) OR
         LOWER(u.lastname) LIKE LOWER(CONCAT('%', :keyword, '%')) OR
         LOWER(u.email) LIKE LOWER(CONCAT('%', :keyword, '%')) OR
         LOWER(u.phone) LIKE LOWER(CONCAT('%', :keyword, '%'))
        )
    AND (:role IS NULL OR u.role.id = :role)
""")
    Page<User> findAllByFilters(@Param("keyword") String keyword, @Param("role") Integer role, Pageable pageable);
}
