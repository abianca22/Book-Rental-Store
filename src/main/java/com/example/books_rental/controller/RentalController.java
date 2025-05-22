package com.example.books_rental.controller;

import com.example.books_rental.dto.UpdateRentalDto;
import com.example.books_rental.mapper.RentalMapper;
import com.example.books_rental.model.entities.Rental;
import com.example.books_rental.model.entities.User;
import com.example.books_rental.service.AuthenticationService;
import com.example.books_rental.service.BooksManagementService;
import com.example.books_rental.service.RentalsManagementService;
import com.example.books_rental.service.UsersManagementService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/rentals")
@Slf4j
public class RentalController {

    private final RentalsManagementService rentalService;
    private final BooksManagementService bookService;
    private final UsersManagementService userService;
    private final AuthenticationService authenticationService;
    private final RentalMapper rentalMapper;

    public RentalController(RentalsManagementService rentalService,
                            BooksManagementService bookService,
                            UsersManagementService userService,
                            AuthenticationService authenticationService,
                            RentalMapper rentalMapper) {
        this.rentalService = rentalService;
        this.bookService = bookService;
        this.userService = userService;
        this.authenticationService = authenticationService;
        this.rentalMapper = rentalMapper;
    }

//    @RequestMapping("")
//    public String getRentals(Model model) {
//        User currentUser = authenticationService.getCurrentUser();
//        List<Rental> rentals;
//        if (currentUser.getRole().getName().equals("client")) {
//           rentals = rentalService.getRentalsByClient(currentUser);
//        }
//        else {
//            rentals = rentalService.getAllRentals();
//        }
//        model.addAttribute("currentUserRole", currentUser.getRole().getName());
//        model.addAttribute("currentUserId", currentUser.getId());
//        model.addAttribute("rentals", rentals);
//        model.addAttribute("today", LocalDate.now());
//        return "rentals/rentalsList";
//    }

    @GetMapping("")
    public String getRentals(@RequestParam("page") Optional<Integer> page,
                             @RequestParam("size") Optional<Integer> size,
                             @RequestParam(value = "keyword", required = false) Optional<String> keywordOpt,
                             @RequestParam(value = "status", required = false) Optional<String> statusOpt,
                             @RequestParam(value = "startDateFrom", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Optional<LocalDate> startDateFromOpt,
                             @RequestParam(value = "startDateTo", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Optional<LocalDate> startDateToOpt,
                             Model model) {

        User currentUser = authenticationService.getCurrentUser();
        int currentPage = page.orElse(1);
        int pageSize = size.orElse(10);
        String keyword = keywordOpt.orElse("");
        String status = statusOpt.orElse(null);
        if (status != null && status.isEmpty())  {
            status = null;
        }
        LocalDate startDateFrom = startDateFromOpt.orElse(null);
        LocalDate startDateTo = startDateToOpt.orElse(null);
        log.info("Keyword: {}", keyword);
        log.info("Status: {}", status);
        log.info("Start Date From: {}", startDateFrom);
        log.info("Start Date To: {}", startDateTo);

        Page<Rental> rentalPage;
        Pageable pageRequest = PageRequest.of(currentPage-1, pageSize, Sort.by("startDate").descending());

        if (currentUser.getRole().getName().equals("client")) {
            rentalPage = rentalService.searchClientRentals(currentUser, keyword, status, startDateFrom, startDateTo, pageRequest);
            log.info("Fetching rentals for client");
        } else {
            rentalPage = rentalService.searchAllRentals(keyword, status, startDateFrom, startDateTo, pageRequest);
            log.info("Fetching all rentals");
        }

        model.addAttribute("rentals", rentalPage);
        model.addAttribute("currentPage", currentPage);
        model.addAttribute("pageSize", pageSize);
        model.addAttribute("keyword", keyword);
        model.addAttribute("status", status);
        model.addAttribute("startDateFrom", startDateFrom);
        model.addAttribute("startDateTo", startDateTo);
        model.addAttribute("currentUserRole", currentUser.getRole().getName());
        model.addAttribute("currentUserId", currentUser.getId());
        model.addAttribute("authenticated", authenticationService.checkIfAuthenticated());
        model.addAttribute("today", LocalDate.now());

        return "rentals/rentalsList";
    }



    @GetMapping("/edit/{id}")
    public String getRental(@PathVariable Integer id, Model model) {
        Rental rental = rentalService.getRentalById(id);
        model.addAttribute("rental", rental);
        model.addAttribute("currentUserRole", authenticationService.getCurrentUser().getRole().getName());
        model.addAttribute("authenticated", authenticationService.checkIfAuthenticated());
        if (rental.getAssociatedReturn() != null) {
            log.warn("Rental {} has an associated return", id);
            return "access_denied";
        }
        return "rentals/rentalEditForm";
    }

    @PostMapping("/edit/{id}")
    public String updateRental(@PathVariable Integer id,
                               @ModelAttribute UpdateRentalDto rentalDto,
                               @RequestParam(value = "email", required = false) String email,
                               Model model,
                               BindingResult result) {
        User currentUser = authenticationService.getCurrentUser();
        Rental foundRental = rentalService.getRentalById(id);
        if (result.hasErrors()) {
            List<String> globalErrors = result.getAllErrors().stream()
                    .filter(error -> !(error instanceof FieldError))
                    .map(ObjectError::getDefaultMessage)
                    .toList();
            List<String> fieldErrors = result.getFieldErrors().stream()
                    .map(FieldError::getDefaultMessage)
                    .toList();
            this.logErrors(globalErrors, fieldErrors);
            model.addAttribute("error", globalErrors.isEmpty() ? null : globalErrors);
            model.addAttribute("rental", rentalService.getRentalById(id));
            model.addAttribute("currentUserRole", currentUser.getRole().getName());
            model.addAttribute("authenticated", authenticationService.checkIfAuthenticated());
            if (email != null) {
                log.warn("Error occured while updating rental {}", id);
                return "rentals/rentalEditForm";
            }
            else {
                log.warn("Error occcured while on rentals list");
                return "redirect:/rentals";
            }
        }
        else if ((email == null || email.isEmpty()) && (currentUser.getRole().getName().equals("admin") || (currentUser.getRole().getName().equals("employee") && foundRental.getEmployee() != null && currentUser.getId() == foundRental.getEmployee().getId()))) {
            rentalService.updatePendingRequest(id, false);
            log.info("Pending request for rental {} has been approved successfully", id);
        }
        else {
            try {
                User user = userService.getUserByEmail(email);
                Rental rental = rentalMapper.toRental(rentalDto);
                rentalService.updateRental(rental, user);
                log.info("Rental {} updated successfully", id);
            }
            catch (Exception e) {
                model.addAttribute("error", "User with this email does not exist!");
                model.addAttribute("currentUserRole", currentUser.getRole().getName());
                model.addAttribute("rental", rentalService.getRentalById(id));
                model.addAttribute("authenticated", authenticationService.checkIfAuthenticated());
                log.error("Error while updating rental {}: {}", id, e.getMessage());
                return "rentals/rentalEditForm";
            }
        }
        return "redirect:/rentals";
    }

    @PostMapping("/editClient/{id}")
    public String updateClientRental(@PathVariable Integer id) {
        User currentUser = authenticationService.getCurrentUser();
        Rental rental = rentalService.getRentalById(id);
        if (currentUser.getRole().getName().equals("client") || (rental.getClient() != null && rental.getClient().getId() == currentUser.getId())) {
            rentalService.updatePendingRequest(id, true);
            log.info("Pending request for rental {} has been sent successfully", id);
        }
        else {
            log.warn("User {} tried to update rental {} without permission", currentUser.getId(), id);
            return "access_denied";
        }
        log.info("Rental {} updated successfully", id);
        return "redirect:/rentals";
    }

    void logErrors(List<String> objectErrors, List<String> fieldErrors) {
        if (!objectErrors.isEmpty()) {
            log.error("Object errors: {}", objectErrors);
        }
        if (!fieldErrors.isEmpty()) {
            log.error("Field errors: {}", fieldErrors);
        }
    }
}
