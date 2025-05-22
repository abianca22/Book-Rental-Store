package com.example.books_rental.repository;

import com.example.books_rental.model.entities.Rental;
import com.example.books_rental.model.entities.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface RentalRepository extends JpaRepository<Rental, Integer> {
    @Query("""
    SELECT r FROM Rental r WHERE r.client.id = :clientId
    """)
    List<Rental> findByClientId(Integer clientId);
    @Query("""
    SELECT r FROM Rental r WHERE r.employee.id = :employeeId
    """)
    List<Rental> findByEmployeeId(Integer employeeId);
    @Query("""
    SELECT r FROM Rental r
            LEFT JOIN r.book b
            LEFT JOIN r.client c
            LEFT JOIN r.employee e
            WHERE (:keyword = '' OR
            LOWER(b.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR
            LOWER(c.username) LIKE LOWER(CONCAT('%', :keyword, '%')) OR
            LOWER(e.username) LIKE LOWER(CONCAT('%', :keyword, '%'))) AND
            (:status IS NULL OR (:status = 'returned' AND r.associatedReturn IS NOT NULL) OR (:status = 'not_returned' AND r.associatedReturn IS NULL)) AND
            (CAST(:startDateFrom AS DATE) IS NULL OR r.startDate >= :startDateFrom) AND
            (CAST(:startDateTo AS DATE) IS NULL OR r.startDate <= :startDateTo)
    """)
    Page<Rental> searchAll(@Param("keyword") String keyword,
                           @Param("status") String status,
                           @Param("startDateFrom") LocalDate startDateFrom,
                           @Param("startDateTo") LocalDate startDateTo,
                           Pageable pageable);

    @Query("""
    SELECT r FROM Rental r
            LEFT JOIN r.book b
            LEFT JOIN r.employee e
            WHERE r.client = :client AND
            (:keyword = '' OR LOWER(b.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR
            LOWER(e.username) LIKE LOWER(CONCAT('%', :keyword, '%'))) AND
            (:status IS NULL OR (:status = 'returned' AND r.associatedReturn IS NOT NULL) OR (:status = 'not_returned' AND r.associatedReturn IS NULL)) AND
            (CAST(:startDateFrom AS DATE) IS NULL OR r.startDate >= :startDateFrom) AND
            (CAST(:startDateTo AS DATE) IS NULL OR r.startDate <= :startDateTo)
    """)
    Page<Rental> searchByClient(@Param("client") User client,
                                @Param("keyword") String keyword,
                                @Param("status") String status,
                                @Param("startDateFrom") LocalDate startDateFrom,
                                @Param("startDateTo") LocalDate startDateTo,
                                Pageable pageable);


}
