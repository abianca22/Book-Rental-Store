package org.booksrental.rentalservice.controller;


import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.booksrental.rentalservice.config.AuthorityCheck;
import org.booksrental.rentalservice.model.dto.CurrentUserDTO;
import org.booksrental.rentalservice.model.dto.ReturnDTO;
import org.booksrental.rentalservice.model.dto.UserDTO;
import org.booksrental.rentalservice.model.entity.Rental;
import org.booksrental.rentalservice.model.entity.Return;
import org.booksrental.rentalservice.model.mapper.ReturnMapper;
import org.booksrental.rentalservice.service.RentalService;
import org.booksrental.rentalservice.service.ReturnService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/returns")
@Slf4j
public class ReturnController {
    private final ReturnService returnService;
    private final RentalService rentalService;
    private final ReturnMapper returnMapper;
    private final AuthorityCheck authorityCheck;

    public ReturnController(ReturnService returnService, RentalService rentalService, ReturnMapper returnMapper, AuthorityCheck authorityCheck) {
        this.returnService = returnService;
        this.rentalService = rentalService;
        this.returnMapper = returnMapper;
        this.authorityCheck = authorityCheck;
    }

    @PostMapping("/{id}")
    public ResponseEntity<?> addReturn(@PathVariable Integer id, HttpServletRequest request) {

        Rental rental = rentalService.getRentalById(id);
        Return returnRental = new Return();
        try {
            UserDTO employee = UserDTO.builder().id(authorityCheck.getCurrentUser(getToken(request)).getId()).build();
            returnService.addReturn(returnRental, rental, employee, authorityCheck.getCurrentUser(getToken(request)), getToken(request));
            log.info("Return added successfully for rental ID: {}", id);
        }
        catch (Exception e) {
            log.error("Error while adding return: {}", e.getMessage());
            return ResponseEntity.badRequest().body("Error while adding return: " + e.getMessage());
        }
        ReturnDTO returnDTO = returnMapper.toReturnDTO(returnRental);
        returnDTO.setRentalId(returnRental.getRental().getId());
        return ResponseEntity.ok(returnDTO);
    }

    @PostMapping("/deleteUser/{id}")
    public ResponseEntity<?> updateReturnsUsers(@PathVariable Integer id, HttpServletRequest request) {
        CurrentUserDTO currentUser = authorityCheck.getCurrentUser(getToken(request));
        try {
            List<Return> employeeReturns = returnService.getReturnsByEmployee(id);
            employeeReturns.forEach(rentalReturn -> {
                returnService.deleteEmployeeId(rentalReturn.getId());
            });
        } catch (Exception e) {
            log.error("Error while removing user from rentals: {}", e.getMessage());
            return ResponseEntity.badRequest().body("Error while removing user from returns: " + e.getMessage());
        }
        return ResponseEntity.ok().body("User with id " + id + " was removed from all returns successfully!");
    }

    String getToken(HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            return token.substring(7);
        }
        return null;
    }

}
