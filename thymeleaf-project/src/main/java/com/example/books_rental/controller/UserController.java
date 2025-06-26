package com.example.books_rental.controller;

import com.example.books_rental.dto.UpdateUserDto;
import com.example.books_rental.mapper.UserMapper;
import com.example.books_rental.model.entities.User;
import com.example.books_rental.service.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/users")
@Slf4j
public class UserController {
    private final UsersManagementService userService;
    private final UserMapper userMapper;
    private final RentalsManagementService rentalService;
    private final BooksManagementService bookService;
    private final AuthenticationService authenticationService;
    private final RolesManagementService roleService;

    public UserController (UsersManagementService userService, RentalsManagementService rentalService, BooksManagementService bookService, UserMapper userMapper, AuthenticationService authenticationService, RolesManagementService roleService) {
        this.userService = userService;
        this.rentalService = rentalService;
        this.bookService = bookService;
        this.userMapper = userMapper;
        this.authenticationService = authenticationService;
        this.roleService = roleService;
    }


    @GetMapping("")
    public String getUsers(@RequestParam("page") Optional<Integer> page,
                             @RequestParam("size") Optional<Integer> size,
                             @RequestParam(value = "keyword", required = false) Optional<String> keywordOpt,
                             @RequestParam(value = "role", required = false) Optional<Integer> roleOpt,
                             Model model) {

        User currentUser = authenticationService.getCurrentUser();
        int currentPage = page.orElse(1);
        int pageSize = size.orElse(10);
        String keyword = keywordOpt.orElse("");
        Integer role = roleOpt.orElse(null);

        Page<User> userPage;
            Pageable pageRequest = PageRequest.of(currentPage-1, pageSize, Sort.by("username").ascending());

        if (currentUser.getRole().getName().equals("client")) {
            log.warn("User {} tried to access user list while logged in as client", currentUser.getUsername());
            return "access_denied";
        } else {
            userPage = userService.getAllUsersByFilters(keyword, role, pageRequest);
            log.info("Fetching all users with filters: keyword={}, role={}", keyword, role);
        }

        model.addAttribute("userPage", userPage);
        model.addAttribute("currentPage", currentPage);
        model.addAttribute("pageSize", pageSize);
        model.addAttribute("keyword", keyword);
        model.addAttribute("role", role);
        model.addAttribute("currentUserRole", currentUser.getRole().getName());
        model.addAttribute("currentUserId", currentUser.getId());
        model.addAttribute("authenticated", authenticationService.checkIfAuthenticated());
        model.addAttribute("roles", roleService.getAllRoles());

        return "users/usersList";
    }

    @GetMapping("/edit/{id}")
    public String editUser(@PathVariable Integer id, Model model) {
        User user = userService.getUserById(id);
        User currentUser = authenticationService.getCurrentUser();
        model.addAttribute("user", userMapper.toUpdateUserDto(user));
        model.addAttribute("currentUser", currentUser);
        model.addAttribute("currentUseRole", currentUser.getRole().getName());
        model.addAttribute("authenticated", authenticationService.checkIfAuthenticated());
        if (currentUser.getRole().getName().equals("admin") && currentUser.getId() != id) {
            model.addAttribute("roles", roleService.getAllRoles());
            log.info("Admin {} is editing user {}", currentUser.getUsername(), user.getUsername());
            return "users/changeRoleForm";
        }
        else if (currentUser.getId() == id) {
            log.info("User {} is editing their own data", currentUser.getUsername());
            return "users/editDataForm";
        }
        return "access_denied";
    }

    @PostMapping("/edit/{id}")
    public String editUser(@ModelAttribute("user") @Valid UpdateUserDto userDTO,
                           BindingResult bindingResult,
                           Model model,
                           @RequestParam(value = "confirmPassword", required = false) String confirmPassword,
                           @PathVariable Integer id) {
        model.addAttribute("user", userDTO);
        model.addAttribute("roles", roleService.getAllRoles());
        model.addAttribute("currentUserRole", authenticationService.getCurrentUser().getRole().getName());
        model.addAttribute("authenticated", authenticationService.checkIfAuthenticated());
        User currentUser = authenticationService.getCurrentUser();
        if (bindingResult.hasErrors()) {
            List<String> globalErrors = bindingResult.getAllErrors().stream()
                    .filter(error -> !(error instanceof FieldError))
                    .map(ObjectError::getDefaultMessage)
                    .toList();
            List<String> fieldErrors = bindingResult.getFieldErrors().stream()
                    .map(FieldError::getDefaultMessage)
                    .toList();
            logErrors(globalErrors, fieldErrors);
            model.addAttribute("error", globalErrors.isEmpty() ? null : globalErrors);
            if (currentUser.getRole().getName().equals("admin") && currentUser.getId() != id) {
                log.warn("The role of the user {} could not be changed", userDTO.getUsername());
                return "users/changeRoleForm";
            }
            else {
                log.warn("User {} could not be updated", userDTO.getUsername());
                return "users/editDataForm";
            }
        }
        if (currentUser.getRole().getName().equals("client") && !userDTO.getPassword().equals(confirmPassword)) {
            model.addAttribute("error", "Passwords do not match");
            log.error("Passwords do not match");
            return "users/editDataForm";
        }
        try {
            User user = userMapper.toUser(userDTO);
            if (userDTO.getRoleId() != null) {
                user.setRole(roleService.getRoleById(userDTO.getRoleId()));
            }
            userService.updateUser(user);
            log.info("User {} updated successfully", userDTO.getUsername());
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            log.error("Error during edit action: {}", e.getMessage());
            if (currentUser.getRole().getName().equals("admin") && currentUser.getId() != id) {
                return "users/changeRoleForm";
            }
            else {
                return "users/editDataForm";
            }
        }
        if (confirmPassword != null && !confirmPassword.isEmpty()) {
            model.addAttribute("message", "Password updated successfully!");
            log.info("User {} changed their password successfully", userDTO.getUsername());
        }
        else {
            model.addAttribute("message", "Data updated successfully!");
            log.info("User {} updated their data successfully", userDTO.getUsername());
        }
        if (currentUser.getId() == id) {
            return "redirect:/users/profile?source=" + (confirmPassword != null && !confirmPassword.isEmpty() ? "passwordChanged" : "dataUpdated");
        }
        else {
            return "redirect:/users?source=userUpdated";
        }
    }

    @RequestMapping("/profile")
    public String getProfile(Model model, @RequestParam(value="source", required = false) String source) {
        User currentUser = authenticationService.getCurrentUser();
        model.addAttribute("user", currentUser);
        model.addAttribute("currentUserRole", authenticationService.getCurrentUser().getRole().getName());
        model.addAttribute("authenticated", authenticationService.checkIfAuthenticated());
        if (source != null && source.equals("passwordChanged")) {
            model.addAttribute("message", "Password changed successfully!");
        }
        else if (source != null) {
            model.addAttribute("message", "Data updated successfully!");
        }
        return "users/profile";
    }

    @RequestMapping("/delete")
    public String deleteConfirm(Model model) {
        return "users/deleteConfirm";
    }

    @PostMapping("/delete")
    public String deleteAccount(HttpServletRequest request,
                                HttpServletResponse response,
                                Principal principal) {
        try {
            userService.deleteUser(userService.getUserByUsername(principal.getName()).getId());
        }
        catch (Exception e) {
            log.error("Error during account deletion: {}", e.getMessage());
            return "redirect:/users/profile?error=accountDeletionFailed";
        }
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null) {
            log.warn("User deleted their account");
            new SecurityContextLogoutHandler().logout(request, response, auth);
        }
        return "redirect:/login?accountDeleted";
    }

    public void logErrors(List<String> objectErrors, List<String> fieldErrors) {
        if (!objectErrors.isEmpty()) {
            log.error("Object errors: {}", objectErrors);
        }
        if (!fieldErrors.isEmpty()) {
            log.error("Field errors: {}", fieldErrors);
        }
    }
}
