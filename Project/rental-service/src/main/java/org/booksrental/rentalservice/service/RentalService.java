package org.booksrental.rentalservice.service;

import org.booksrental.rentalservice.config.AuthorityCheck;
import org.booksrental.rentalservice.exception.AccessDeniedException;
import org.booksrental.rentalservice.exception.NotFoundException;
import org.booksrental.rentalservice.exception.OverdueException;
import org.booksrental.rentalservice.model.dto.BookDTO;
import org.booksrental.rentalservice.model.dto.CurrentUserDTO;
import org.booksrental.rentalservice.model.dto.UpdateRentalDTO;
import org.booksrental.rentalservice.model.dto.UserDTO;
import org.booksrental.rentalservice.model.entity.Rental;
import org.booksrental.rentalservice.repository.RentalRepository;
import org.booksrental.rentalservice.validator.Validator;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

@Service
public class RentalService {
    private final RentalRepository rentalRepository;
    private final AuthorityCheck authorityCheck;
    private final BookService bookService;
    private final UserService userService;

    public RentalService(RentalRepository rentalsRepository,
                         AuthorityCheck authorityCheck,
                         BookService bookService,
                         UserService userService) {
        this.rentalRepository = rentalsRepository;
        this.authorityCheck = authorityCheck;
        this.bookService = bookService;
        this.userService = userService;
    }

    public Rental addRental(Rental rental, BookDTO book, UserDTO employee, UserDTO client, String token) {
        if (!book.isStatus()) {
            throw new AccessDeniedException("This book is not available for rental!");
        }
        book.setStatus(false);
        rental.setBookId(book.getId());
        checkEmployee(employee);
        rental.setEmployeeId(employee.getId());
        rental.setClientId(client.getId());
        rental.setStartDate(LocalDate.now());
        rental.setDueDate(LocalDate.now().plusDays(14));
        rental.setTotalPrice(book.getRentalPrice());
        bookService.updateBook(book, token);
        return rentalRepository.save(rental);
    }

    public Rental getRentalById(int id) {
        return rentalRepository.findById(id).orElseThrow(() -> new NotFoundException("Rental with id " + id + " does not exist!"));
    }

    public Rental updateRental(Rental rental, UserDTO employee, CurrentUserDTO requester, BookDTO book) {
        Rental foundRental = rentalRepository.findById(rental.getId()).orElseThrow(() -> new NotFoundException("Rental with id " + rental.getId() + " does not exist!"));
        checkRequester(requester, employee);
        if (book != null) {
            checkDate(foundRental.getDueDate());
            foundRental.setDueDate(foundRental.getDueDate().plusDays(7));
            foundRental.setTotalPrice(foundRental.getTotalPrice() + book.getExtensionPrice());
        }
        if (employee != null) {
            if (!requester.getRole().equals("admin")) {
                throw new AccessDeniedException("Only admins can update the employee of a rental!");
            }
            checkEmployee(employee);
            foundRental.setEmployeeId(employee.getId());
        }
        Validator.validateObject(foundRental);
        return rentalRepository.save(foundRental);
    }

    public List<Rental> getRentalsByClient(UserDTO client, CurrentUserDTO requester) {
        if (requester.getRole().equals("client") && !Objects.equals(requester.getId(), client.getId())) {
            throw new AccessDeniedException("Only the staff or the client can view their rentals!");
        }
        return rentalRepository.findByClientId(client.getId());
    }

    public void checkRequester(CurrentUserDTO requester, UserDTO employee) {
        if (requester.getRole().equals("client") || (requester.getRole().equals("employee") && !Objects.equals(requester.getId(), employee.getId()))) {
            throw new AccessDeniedException("Only admins or the employee responsible for the rental can update it!");
        }
    }


    public void checkEmployee(UserDTO employee) {
        if (employee.getRole().equals("client")) {
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

    public List<Rental> getRentalsByEmployee(UserDTO employee) {
        return rentalRepository.findByEmployeeId(employee.getId());
    }


    public void updatePendingRequest(Integer id, boolean pendingRequest, CurrentUserDTO currentUser, BookDTO book) {
        Rental foundRental = rentalRepository.findById(id).orElseThrow(() -> new NotFoundException("Rental with id " + id + " does not exist!"));
        if (!currentUser.getRole().equals("admin") && (foundRental.getClientId() == null || !Objects.equals(currentUser.getId(), foundRental.getClientId())) && (foundRental.getEmployeeId() == null || !Objects.equals(currentUser.getId(), foundRental.getEmployeeId()))) {
            throw new AccessDeniedException("Only the staff or the client can update the pending request!");
        }
        if (pendingRequest && foundRental.getDueDate().isBefore(LocalDate.now())) {
            throw new OverdueException("The rental is overdue, so it cannot be extended!");
        }
        if ((pendingRequest && foundRental.isPendingRequest()) || (!pendingRequest && !foundRental.isPendingRequest())) {
            throw new AccessDeniedException("The rental was already updated!");
        }
        foundRental.setTotalPrice(foundRental.getTotalPrice() + (foundRental.isPendingRequest() ? book.getExtensionPrice() : 0));
        foundRental.setPendingRequest(pendingRequest);
        if (!pendingRequest) {
            foundRental.setDueDate(foundRental.getDueDate().plusDays(7));
        }
        rentalRepository.save(foundRental);
    }

    public List<Rental> searchAllRentals(String keyword, String status, LocalDate startDateFrom, LocalDate startDateTo, String token) {
        List<Rental> rentals = rentalRepository.searchAll(status, startDateFrom, startDateTo);

        if (keyword != null && !keyword.isEmpty()) {
            List<BookDTO> books = bookService.getByKeyword(keyword, token);
            List<Integer> bookIds = books.stream()
                    .map(BookDTO::getId)
                    .toList();
            List<UserDTO> users = userService.getUsersByKeyword(keyword, token);
            List<Integer> userIds = users.stream()
                    .map(UserDTO::getId)
                    .toList();
            return rentals.stream().filter(rental ->
                bookIds.contains(rental.getBookId()) ||
                userIds.contains(rental.getClientId()) ||
                userIds.contains(rental.getEmployeeId()))
                .toList();
        }
        return rentals;
    }

    public List<Rental> searchClientRentals(Integer client, String keyword, String status, LocalDate startDateFrom, LocalDate startDateTo, String token) {
        List<Rental> rentals = rentalRepository.searchByClient(client, status, startDateFrom, startDateTo);
        if (keyword != null && !keyword.isEmpty()) {
            List<BookDTO> books = bookService.getByKeyword(keyword, token);
            List<Integer> bookIds = books.stream()
                    .map(BookDTO::getId)
                    .toList();
            List<UserDTO> users = userService.getUsersByKeyword(keyword, token);
            List<Integer> userIds = users.stream()
                    .map(UserDTO::getId)
                    .toList();
            return rentals.stream().filter(rental ->
                bookIds.contains(rental.getBookId()) ||
                userIds.contains(rental.getEmployeeId()))
                .toList();
        }
        return rentalRepository.searchByClient(client, status, startDateFrom, startDateTo);
    }

    public List<Rental> getRentalsByBookId(Integer bookId) {
        return rentalRepository.findByBookId(bookId);
    }


    public Rental deleteBookId(Integer id) {
        Rental rental = rentalRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Rental with id " + id + " does not exist!"));
        rental.setBookId(null);
        rental.setPendingRequest(false);
        return rentalRepository.save(rental);
    }

    public Rental deleteEmployeeId(Integer id) {
        Rental rental = rentalRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Rental with id " + id + " does not exist!"));
        rental.setEmployeeId(null);
        return rentalRepository.save(rental);
    }

    public Rental deleteClientId(Integer id) {
        Rental rental = rentalRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Rental with id " + id + " does not exist!"));
        rental.setClientId(null);
        return rentalRepository.save(rental);
    }
}

