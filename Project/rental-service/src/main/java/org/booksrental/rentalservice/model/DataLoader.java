package org.booksrental.rentalservice.model;

import org.booksrental.rentalservice.model.entity.Rental;
import org.booksrental.rentalservice.model.entity.Return;
import org.booksrental.rentalservice.repository.RentalRepository;
import org.booksrental.rentalservice.repository.ReturnRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.List;


@Component
@Profile("dev")
public class DataLoader implements CommandLineRunner {

    private final RentalRepository rentalRepository;
    private final ReturnRepository returnRepository;

    public DataLoader(RentalRepository rentalRepository,
                      ReturnRepository returnRepository) {
        this.rentalRepository = rentalRepository;
        this.returnRepository = returnRepository;
    }

    @Override
    public void run(String... args) {

        if (rentalRepository.findAll().isEmpty()) {
            Rental rental = Rental.builder()
                    .clientId(3)
                    .bookId(1)
                    .employeeId(2)
                    .totalPrice(12.5f)
                    .startDate(java.time.LocalDate.now())
                    .dueDate(java.time.LocalDate.now().plusDays(14))
                    .pendingRequest(false)
                    .build();
            rentalRepository.save(rental);

            Rental rental2 = Rental.builder()
                    .clientId(3)
                    .bookId(3)
                    .employeeId(1)
                    .totalPrice(20.f)
                    .startDate(java.time.LocalDate.now().minusDays(11))
                    .dueDate(java.time.LocalDate.now().plusDays(3))
                    .pendingRequest(false)
                    .build();
            rentalRepository.save(rental2);

            Rental rental3 = Rental.builder()
                    .clientId(1)
                    .bookId(3)
                    .employeeId(1)
                    .totalPrice(25.f)
                    .startDate(java.time.LocalDate.now().minusDays(20))
                    .dueDate(java.time.LocalDate.now().minusDays(6))
                    .pendingRequest(false)
                    .build();
            rentalRepository.save(rental3);

            Return return1 = Return.builder()
                    .rental(rental3)
                    .employeeId(1)
                    .returnDate(java.time.LocalDate.now().minusDays(7))
                    .build();
            returnRepository.save(return1);
        }

    }
}

