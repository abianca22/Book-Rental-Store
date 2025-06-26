package org.booksrental.rentalservice.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.booksrental.rentalservice.config.AuthorityCheck;
import org.booksrental.rentalservice.exception.AccessDeniedException;
import org.booksrental.rentalservice.model.dto.*;
import org.booksrental.rentalservice.model.entity.Rental;
import org.booksrental.rentalservice.model.mapper.RentalMapper;
import org.booksrental.rentalservice.service.BookService;
import org.booksrental.rentalservice.service.RentalService;

import org.booksrental.rentalservice.service.UserService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@RestController
@RequestMapping("/rentals")
@Slf4j
public class RentalController {

    private final RentalService rentalService;
    private final RentalMapper rentalMapper;
    private final AuthorityCheck authorityCheck;
    private final BookService bookService;
    private final UserService userService;

    public RentalController(RentalService rentalService,
                            RentalMapper rentalMapper,
                            AuthorityCheck authorityCheck,
                            BookService bookService,
                            UserService userService) {
        this.rentalService = rentalService;
        this.rentalMapper = rentalMapper;
        this.authorityCheck = authorityCheck;
        this.bookService = bookService;
        this.userService = userService;
    }

    @RequestMapping("/book/{id}")
    public ResponseEntity<?> getBook(@PathVariable Integer id, HttpServletRequest request) {
        return ResponseEntity.ok(bookService.getBookById(id, getToken(request)));
    }

    @RequestMapping("/user/{id}")
    public ResponseEntity<?> getUser(@PathVariable Integer id, HttpServletRequest request) {
        return ResponseEntity.ok(userService.getUserById(id, getToken(request)));
    }


    @PostMapping
    public ResponseEntity<?> addRental(@Valid @RequestBody CreateRentalDTO rentalDto,
                                       HttpServletRequest request) {
        CurrentUserDTO currentUser = authorityCheck.getCurrentUser(getToken(request));
        log.info("Adding new rental: {}", rentalDto);
        try {
            Rental rental = rentalMapper.toRental(rentalDto);
            BookDTO book = bookService.getBookById(rental.getBookId(), getToken(request));
            if (book == null || !book.isStatus()) {
                throw new AccessDeniedException("The book is not available for rent");
            }
            UserDTO user = userService.getUserById(rentalDto.getClientId(), getToken(request));
            UserDTO employee = UserDTO.builder().id(currentUser.getId()).role(currentUser.getRole()).build();
            rental.setTotalPrice(book.getRentalPrice());
            Rental createdRental = rentalService.addRental(rental, book, employee, user, getToken(request));
            log.info("Rental added successfully with id: {}", createdRental.getId());
            return ResponseEntity.ok().body(rentalMapper.toUpdateRentalDTO(createdRental));
        } catch (Exception e) {
            log.error("Error while adding rental: {}", e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/all")
    public ResponseEntity<?> getRentals(@RequestParam(value = "keyword", required = false) Optional<String> keywordOpt,
                                     @RequestParam(value = "status", required = false) Optional<String> statusOpt,
                                     @RequestParam(value = "startDateFrom", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Optional<LocalDate> startDateFromOpt,
                                     @RequestParam(value = "startDateTo", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Optional<LocalDate> startDateToOpt,
                                     @RequestParam(value = "bookId", required = false) Optional<Integer> bookIdOpt,
                                     HttpServletRequest request) {

        CurrentUserDTO currentUser = authorityCheck.getCurrentUser(getToken(request));
        String keyword = keywordOpt.orElse("");
        String status = statusOpt.orElse(null);
        if (status != null && status.isEmpty())  {
            status = null;
        }
        LocalDate startDateFrom = startDateFromOpt.orElse(null);
        LocalDate startDateTo = startDateToOpt.orElse(null);
        Integer bookId = bookIdOpt.orElse(null);
        log.info("Keyword: {}", keyword);
        log.info("Status: {}", status);
        log.info("Start Date From: {}", startDateFrom);
        log.info("Start Date To: {}", startDateTo);

        List<Rental> rentalPage;

        rentalPage = rentalService.getAllRentals();
        if (bookId != null) {
            rentalPage = rentalPage.stream()
                    .filter(rental -> rental.getBookId() != null && rental.getBookId().equals(bookId))
                    .toList();
        }
        else if (currentUser.getRole().equals("client")) {
            rentalPage = rentalService.searchClientRentals(currentUser.getId(), keyword, status, startDateFrom, startDateTo, getToken(request));
            log.info("Fetching rentals for client");
        } else {
            rentalPage = rentalService.searchAllRentals(keyword, status, startDateFrom, startDateTo, getToken(request));
            log.info("Fetching all rentals");
        }
        return ResponseEntity.ok().body(rentalPage.stream().map(rental -> {
            UpdateRentalDTO rentalDto = rentalMapper.toUpdateRentalDTO(rental);
            rentalDto.setAssociatedReturnId(rental.getAssociatedReturn() != null ? rental.getAssociatedReturn().getId() : null);
            return rentalDto;
        }).toList());
    }


    @PostMapping("/edit/{rentalId}")
    public ResponseEntity<?> updateRental(@PathVariable Integer rentalId,
                               @Valid @RequestBody UpdateRentalDTO rentalDto,
                               HttpServletRequest request) {
        CurrentUserDTO currentUser = authorityCheck.getCurrentUser(getToken(request));
        Rental foundRental = rentalService.getRentalById(rentalId);
        BookDTO book = bookService.getBookById(foundRental.getBookId(), getToken(request));
        log.info("RentalDTO: {}", rentalDto);
        log.info("Updating rental with id: {}", rentalId);
        if (rentalDto.getEmployeeId() == null && (currentUser.getRole().equals("admin") || (currentUser.getRole().equals("employee") && foundRental.getEmployeeId() != null && Objects.equals(currentUser.getId(), foundRental.getEmployeeId())))) {
            rentalService.updatePendingRequest(rentalId, false, currentUser, book);
            log.info("Pending request for rental {} has been approved successfully", rentalId);
        }
        else {
            try {
                if (!currentUser.getRole().equals("admin")) {
                    throw new AccessDeniedException("Only admin can change the employee for a rental");
                }
                UserDTO user = userService.getUserById(rentalDto.getEmployeeId(), getToken(request));
                Rental rental = rentalMapper.toRental(rentalDto);
                rental.setId(rentalId);
                rentalService.updateRental(rental, user, currentUser, null);
                log.info("Rental {} updated successfully", rentalId);
            }
            catch (Exception e) {
                log.error("Error while updating rental {}: {}", rentalId, e.getMessage());
                return ResponseEntity.badRequest().body(e.getMessage());
            }
        }
        return ResponseEntity.ok().body("Rental updated successfully");
    }

    @PostMapping("/editClient/{id}")
    public ResponseEntity<?> updateClientRental(@PathVariable Integer id, HttpServletRequest request) {
        CurrentUserDTO currentUser = authorityCheck.getCurrentUser(getToken(request));
        Rental rental = rentalService.getRentalById(id);
        if (rental.getClientId() != null && rental.getClientId().equals(currentUser.getId())) {
            try {
                rentalService.updatePendingRequest(id, true, currentUser, bookService.getBookById(rental.getBookId(), getToken(request)));
                log.info("Pending request for rental {} has been sent successfully", id);
            } catch (Exception e) {
                log.error("Error while updating rental {}: {}", id, e.getMessage());
                return ResponseEntity.badRequest().body(e.getMessage());
            }
        }
        else {
            log.warn("User {} tried to update rental {} without permission", currentUser.getId(), id);
            return ResponseEntity.status(403).body("You do not have permission to update this rental");
        }
        log.info("Rental {} updated successfully", id);
        return ResponseEntity.ok().body(rentalMapper.toUpdateRentalDTO(rentalService.getRentalById(id)));
    }

    @PostMapping("/deleteBook/{id}")
    public ResponseEntity<?> updateBookRentals(@PathVariable Integer id, HttpServletRequest request) {
        CurrentUserDTO currentUser = authorityCheck.getCurrentUser(getToken(request));
        if (currentUser.getRole().equals("client")) {
            return ResponseEntity.status(403).body("Only staff members can remove a book from rentals");
        }
        List<Rental> rentals = rentalService.getRentalsByBookId(id);
        rentals.forEach(rental -> {
            rentalService.deleteBookId(rental.getId());
        });
        return ResponseEntity.ok().body("Book with id " + id + " was removed from all rentals successfully!");
    }

    @PostMapping("/deleteUser/{id}")
    public ResponseEntity<?> updateRentalsUsers(@PathVariable Integer id, HttpServletRequest request) {
        CurrentUserDTO currentUser = authorityCheck.getCurrentUser(getToken(request));
        try {
            List<Rental> employeeRentals = rentalService.getRentalsByEmployee(UserDTO.builder().id(id).build());
            employeeRentals.forEach(rental -> {
                rentalService.deleteEmployeeId(rental.getId());
            });
            List<Rental> clientRentals = rentalService.getRentalsByClient(UserDTO.builder().id(id).build(), currentUser);
            clientRentals.forEach(rental -> {
                rentalService.deleteClientId(rental.getId());
            });
        } catch (Exception e) {
            log.error("Error while removing user from rentals: {}", e.getMessage());
            return ResponseEntity.badRequest().body("Error while removing user from rentals: " + e.getMessage());
        }
        return ResponseEntity.ok().body("User with id " + id + " was removed from all rentals successfully!");
    }


    String getToken(HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            return token.substring(7);
        }
        return null;
    }
}

