package org.booksrental.rentalservice.service;

import org.booksrental.rentalservice.config.AuthorityCheck;
import org.booksrental.rentalservice.exception.AccessDeniedException;
import org.booksrental.rentalservice.exception.NotFoundException;
import org.booksrental.rentalservice.model.dto.BookDTO;
import org.booksrental.rentalservice.model.dto.CurrentUserDTO;
import org.booksrental.rentalservice.model.dto.UserDTO;
import org.booksrental.rentalservice.model.entity.Rental;
import org.booksrental.rentalservice.model.entity.Return;
import org.booksrental.rentalservice.repository.RentalRepository;
import org.booksrental.rentalservice.repository.ReturnRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

@Service
public class ReturnService {
    private final ReturnRepository returnRepository;
    private final RentalRepository rentalRepository;
    private final BookService bookService;

    public ReturnService(ReturnRepository returnRepository, RentalRepository rentalRepository,
                         BookService bookService) {
        this.returnRepository = returnRepository;
        this.rentalRepository = rentalRepository;
        this.bookService = bookService;
    }

    public Return addReturn(Return returnRental, Rental rental, UserDTO employee, CurrentUserDTO requester, String token) {
        if (requester.getRole().equals("client") || (requester.getRole().equals("employee") && !Objects.equals(requester.getId(), employee.getId()))) {
            throw new AccessDeniedException("Only admins or the responsible employee can add the return!");
        }
        returnRental.setRental(rental);
        if (returnRental.getRental().getAssociatedReturn() != null) {
            throw new NotFoundException("The rented book has already been returned!");
        }
        if (returnRental.getRental().getBookId() != null) {
            BookDTO book = bookService.getBookById(returnRental.getRental().getBookId(), token);
            book.setStatus(true);
            bookService.updateBook(book, token);
        }
        returnRental.getRental().setPendingRequest(false);
        rentalRepository.save(returnRental.getRental());
        returnRental.setEmployeeId(employee.getId());
        returnRental.setReturnDate(LocalDate.now());
        return returnRepository.save(returnRental);
    }

    public Return deleteEmployeeId(Integer id) {
        Return rentalReturn = returnRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Rental with id " + id + " does not exist!"));
        rentalReturn.setEmployeeId(null);
        return returnRepository.save(rentalReturn);
    }

    public List<Return> getReturnsByEmployee(Integer employeeId) {
        return returnRepository.findByEmployeeId(employeeId);
    }

}

