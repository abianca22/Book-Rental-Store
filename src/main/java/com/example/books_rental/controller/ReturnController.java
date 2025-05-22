package com.example.books_rental.controller;

import com.example.books_rental.mapper.ReturnMapper;
import com.example.books_rental.model.entities.Rental;
import com.example.books_rental.model.entities.Return;
import com.example.books_rental.service.AuthenticationService;
import com.example.books_rental.service.RentalsManagementService;
import com.example.books_rental.service.ReturnsManagementService;
import com.example.books_rental.service.UsersManagementService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/returns")
@Slf4j
public class ReturnController {
    private final ReturnsManagementService returnService;
    private final RentalsManagementService rentalService;
    private final UsersManagementService userService;
    private final ReturnMapper returnMapper;
    private final AuthenticationService authenticationService;

    public ReturnController(ReturnsManagementService returnsManagementService, RentalsManagementService rentalsManagementService, UsersManagementService usersManagementService, ReturnMapper returnMapper, AuthenticationService authenticationService) {
        this.returnService = returnsManagementService;
        this.rentalService = rentalsManagementService;
        this.userService = usersManagementService;
        this.returnMapper = returnMapper;
        this.authenticationService = authenticationService;
    }

    @PostMapping("/{id}")
    public String addReturn(@PathVariable Integer id, Model model) {

        Rental rental = rentalService.getRentalById(id);
        Return returnRental = new Return();
        try {
            returnService.addReturn(returnRental, rental, authenticationService.getCurrentUser());
            log.info("Return added successfully for rental ID: {}", id);
        }
        catch (Exception e) {
            log.error("Error while adding return: {}", e.getMessage());
            model.addAttribute("error", "An error occurred while processing the return.");
            return "error";
        }
        return "redirect:/rentals";
    }
}
