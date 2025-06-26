package com.example.books_rental.service;

import com.example.books_rental.exception.AccessDeniedException;
import com.example.books_rental.exception.InvalidDataException;
import com.example.books_rental.exception.NotFoundException;
import com.example.books_rental.model.entities.Book;
import com.example.books_rental.model.entities.Rental;
import com.example.books_rental.model.entities.User;
import com.example.books_rental.repository.BookRepository;
import com.example.books_rental.repository.RentalRepository;
import com.example.books_rental.validator.Validator;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;

@Service
public class RentalsManagementService {
    private final RentalRepository rentalRepository;
    private final BookRepository bookRepository;
    private final AuthenticationService authenticationService;

    public RentalsManagementService(RentalRepository rentalsRepository, BookRepository bookRepository, AuthenticationService authenticationService) {
        this.rentalRepository = rentalsRepository;
        this.bookRepository = bookRepository;
        this.authenticationService = authenticationService;
    }

    public Rental addRental(Rental rental, Book book, User employee, User client) {
        if (!book.isStatus()) {
            throw new AccessDeniedException("This book is not available for rental!");
        }
        book.setStatus(false);
        rental.setBook(book);
        checkEmployee(employee);
        rental.setEmployee(employee);
        rental.setClient(client);
        rental.setStartDate(LocalDate.now());
        rental.setDueDate(LocalDate.now().plusDays(14));
        rental.setTotalPrice(book.getRentalPrice());
        bookRepository.save(book);
        return rentalRepository.save(rental);
    }

    public Rental getRentalById(int id) {
        return rentalRepository.findById(id).orElseThrow(() -> new NotFoundException("Rental with id " + id + " does not exist!"));
    }

    public Rental updateRental(Rental rental, User employee) {
        User requester = authenticationService.getCurrentUser();
        Rental foundRental = rentalRepository.findById(rental.getId()).orElseThrow(() -> new NotFoundException("Rental with id " + rental.getId() + " does not exist!"));
        checkRequester(requester, foundRental.getEmployee());
        if (foundRental.getBook() != null) {
            checkDate(foundRental.getDueDate());
            foundRental.setDueDate(foundRental.getDueDate().plusDays(7));
            foundRental.setTotalPrice(foundRental.getTotalPrice() + foundRental.getBook().getExtensionPrice());
        }
        if (employee != null) {
            if (!requester.getRole().getName().equals("admin")) {
                throw new AccessDeniedException("Only admins can update the employee of a rental!");
            }
            checkEmployee(employee);
            foundRental.setEmployee(employee);
        }
        Validator.validateObject(foundRental);
        return rentalRepository.save(foundRental);
    }

    public List<Rental> getRentalsByClient(User client) {
        User requester = authenticationService.getCurrentUser();
        if (requester.getRole().getName().equals("client") && requester.getId() != client.getId()) {
            throw new AccessDeniedException("Only the staff or the client can view their rentals!");
        }
        return rentalRepository.findByClientId(client.getId());
    }

    public void checkRequester(User requester, User employee) {
        if (requester.getRole().getName().equals("client") || (requester.getRole().getName().equals("employee") && requester.getId() != employee.getId())) {
            throw new AccessDeniedException("Only admins or the employee responsible for the rental can update it!");
        }
    }


    public void checkEmployee(User employee) {
        if (employee.getRole().getName().equals("client")) {
            throw new AccessDeniedException("This user is not part of the staff!");
        }
    }

    public void checkDate(LocalDate dueDate) {
        if (LocalDate.now().isAfter(dueDate)) {
            throw new RuntimeException("The rental is overdue!");
        }
    }

    public List<Rental> getAllRentals() {
        return rentalRepository.findAll();
    }

    public List<Rental> getRentalsByEmployee(User employee) {
        return rentalRepository.findByEmployeeId(employee.getId());
    }

    public void updatePendingRequest(Integer id, boolean pendingRequest) {
        Rental foundRental = rentalRepository.findById(id).orElseThrow(() -> new NotFoundException("Rental with id " + id + " does not exist!"));
        User currentUser = authenticationService.getCurrentUser();
        if (!currentUser.getRole().getName().equals("admin") && (foundRental.getClient() == null || currentUser.getId() != foundRental.getClient().getId()) && (foundRental.getEmployee() == null || currentUser.getId() != foundRental.getEmployee().getId())) {
            throw new AccessDeniedException("Only the staff or the client can update the pending request!");
        }
        foundRental.setTotalPrice(foundRental.getTotalPrice() + (foundRental.isPendingRequest() ? foundRental.getBook().getExtensionPrice() : 0));        foundRental.setPendingRequest(pendingRequest);
        foundRental.setPendingRequest(pendingRequest);

        rentalRepository.save(foundRental);
    }

    public Page<Rental> searchAllRentals(String keyword, String status, LocalDate startDateFrom, LocalDate startDateTo, Pageable pageable) {
        return rentalRepository.searchAll(keyword, status, startDateFrom, startDateTo, pageable);
    }

    public Page<Rental> searchClientRentals(User client, String keyword, String status, LocalDate startDateFrom, LocalDate startDateTo, Pageable pageable) {
        return rentalRepository.searchByClient(client, keyword, status, startDateFrom, startDateTo, pageable);
    }

}
