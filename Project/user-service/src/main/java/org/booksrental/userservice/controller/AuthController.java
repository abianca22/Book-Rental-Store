package org.booksrental.userservice.controller;

import jakarta.validation.Valid;
import org.booksrental.userservice.model.config.AuthRequest;
import org.booksrental.userservice.model.dto.CreateUserDTO;
import org.booksrental.userservice.model.dto.UpdateUserDTO;
import org.booksrental.userservice.model.entity.User;
import org.booksrental.userservice.model.mapper.UserMapper;
import org.booksrental.userservice.security.JwtUtil;
import org.booksrental.userservice.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.Collections;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private AuthenticationManager authManager;
    @Autowired private JwtUtil jwtUtil;
    @Autowired private UserService userService;
    @Autowired private UserMapper userMapper;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody AuthRequest request) {
        try {
            authManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
            );

            User user = userService.getUserByUsername(request.getUsername());
            String token = jwtUtil.generateToken(user);

            return ResponseEntity.ok(Collections.singletonMap("token", token));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid credentials");
        }
    }

    @PostMapping("/register")
    public ResponseEntity<?> createUser(@Valid @RequestBody CreateUserDTO userDto) throws Exception {
        User user = userMapper.toUser(userDto);
        User createdUser = userService.createUser(user);
        UpdateUserDTO createdUserDto = userMapper.toUpdateUserDto(createdUser);
        createdUserDto.setRole(createdUser.getRole() != null ? createdUser.getRole().getName() : null);
        return ResponseEntity.created(URI.create("/auth/" + createdUserDto.getId())).body(createdUser);
    }
}
