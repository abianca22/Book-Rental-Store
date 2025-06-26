package org.booksrental.rentalservice.repository;

import org.booksrental.rentalservice.model.entity.Rental;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface RentalRepository extends JpaRepository<Rental, Integer> {
    @Query("""
    SELECT r FROM Rental r WHERE r.clientId = :clientId
    """)
    List<Rental> findByClientId(Integer clientId);
    @Query("""
    SELECT r FROM Rental r WHERE r.employeeId = :employeeId
    """)
    List<Rental> findByEmployeeId(Integer employeeId);
    @Query("""
    SELECT r FROM Rental r
            WHERE (:status IS NULL OR (:status = 'returned' AND r.associatedReturn IS NOT NULL) OR (:status = 'not_returned' AND r.associatedReturn IS NULL)) AND
            (CAST(:startDateFrom AS DATE) IS NULL OR r.startDate >= :startDateFrom) AND
            (CAST(:startDateTo AS DATE) IS NULL OR r.startDate <= :startDateTo)
    """)
    List<Rental> searchAll(@Param("status") String status,
                           @Param("startDateFrom") LocalDate startDateFrom,
                           @Param("startDateTo") LocalDate startDateTo);

    @Query("""
    SELECT r FROM Rental r
            WHERE r.clientId = :client AND
            (:status IS NULL OR (:status = 'returned' AND r.associatedReturn IS NOT NULL) OR (:status = 'not_returned' AND r.associatedReturn IS NULL)) AND
            (CAST(:startDateFrom AS DATE) IS NULL OR r.startDate >= :startDateFrom) AND
            (CAST(:startDateTo AS DATE) IS NULL OR r.startDate <= :startDateTo)
    """)
    List<Rental> searchByClient(@Param("client") Integer client,
                                @Param("status") String status,
                                @Param("startDateFrom") LocalDate startDateFrom,
                                @Param("startDateTo") LocalDate startDateTo);


    List<Rental> findByBookId(Integer bookId);
}
