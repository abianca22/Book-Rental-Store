package org.booksrental.rentalservice.repository;

import org.booksrental.rentalservice.model.entity.Return;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReturnRepository extends JpaRepository<Return, Integer> {
    List<Return> findByEmployeeId(Integer employeeId);
}
