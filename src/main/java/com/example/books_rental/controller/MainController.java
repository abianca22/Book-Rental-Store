package com.example.books_rental.controller;

import com.example.books_rental.dto.CreateUserDto;
import com.example.books_rental.mapper.UserMapper;
import com.example.books_rental.service.UsersManagementService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RequestMapping("/")
@Controller
@Slf4j
public class MainController {

    private final UsersManagementService userService;
    private final UserMapper userMapper;

    public MainController(UsersManagementService userService, UserMapper userMapper) {
        this.userService = userService;
        this.userMapper = userMapper;
    }

    @RequestMapping("")
    public String mainPage(Authentication authentication) {
        return "redirect:/books";
    }

    @RequestMapping("/home")
    public String home(Authentication authentication) {
        return "redirect:/books";
    }

    @GetMapping("/login")
    public String showLogInForm(Authentication authentication) {
        if (authentication != null && authentication.isAuthenticated()) {
            log.warn("User {} tried to access login page while logged in", authentication.getName());
            return "redirect:/books";
        }
        return "login";
    }

    @GetMapping("/accessDenied")
    public String accessDeniedPage(Authentication authentication) {
        log.warn("Access denied for user {}", authentication.getName());
        return "access_denied";
    }

    @GetMapping("/register")
    public String showRegisterForm(Model model, Authentication authentication) {
        if (authentication != null && authentication.isAuthenticated()) {
            log.warn("User {} tried to access registration page while logged in", authentication.getName());
            return "redirect:/books";
        }
        model.addAttribute("user", new CreateUserDto());
        return "register";
    }

    @PostMapping("/register")
    public String register(@ModelAttribute("user") @Valid CreateUserDto userDTO,
                           BindingResult bindingResult,
                           Model model,
                           @RequestParam("confirmPassword") String confirmPassword) {
        if (bindingResult.hasErrors()) {
            List<String> globalErrors = bindingResult.getGlobalErrors().stream().filter(error -> !(error instanceof FieldError))
                    .map(ObjectError::getDefaultMessage)
                    .collect(Collectors.toList());
            model.addAttribute("error", globalErrors.isEmpty() ? null : globalErrors);
            log.error("Validation errors during registration: {}", bindingResult.getAllErrors().stream().map(ObjectError::getDefaultMessage).collect(Collectors.toList()));
            return "register";
        }
        if (!userDTO.getPassword().equals(confirmPassword)) {
            model.addAttribute("error", "Passwords do not match");
            log.error("Passwords do not match");
            return "register";
        }
        try {
            userService.createUser(userMapper.toUser(userDTO));
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            log.error("Error during registration: {}", e.getMessage());
            return "register";
        }
        log.info("User registered successfully: {}", userDTO.getUsername());
        return "redirect:/login?registered";
    }
}
