package org.booksrental.userservice.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.booksrental.userservice.model.dto.UpdateUserDTO;
import org.booksrental.userservice.model.dto.ViewUserDTO;
import org.booksrental.userservice.model.entity.User;
import org.booksrental.userservice.model.mapper.UserMapper;
import org.booksrental.userservice.service.AuthenticationService;
import org.booksrental.userservice.service.RoleService;
import org.booksrental.userservice.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/users")
public class UserController {

    @Autowired
    private UserService userService;
    @Autowired
    private AuthenticationService authenticationService;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private RoleService roleService;

    @GetMapping("/all")
    public ResponseEntity<List<ViewUserDTO>> getAllUsers(@RequestParam(required = false) String keyword, @RequestParam(required = false) Integer role) {
        List<User> users = null;
        if (keyword != null) {
            users = userService.getAllUsersByFilters(keyword, role);
        } else {
            users = userService.getAllUsers();
        }
        if (role != null) {
            users = users.stream()
                    .filter(user -> user.getRole() != null && user.getRole().getId() == role)
                    .toList();
        }
        List<ViewUserDTO> viewUsers = users.stream()
                .map(user -> {
                    ViewUserDTO viewUserDto = userMapper.toViewUserDto(user);
                    viewUserDto.setRole(user.getRole() != null ? user.getRole().getName() : null);
                    return viewUserDto;
                })
                .toList();
        return ResponseEntity.ok(viewUsers);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateUser(@PathVariable Integer id, @Valid @RequestBody UpdateUserDTO userDto) throws Exception {
        User user = userMapper.toUser(userDto);
        user.setRole(userDto.getRole() != null ? roleService.getRoleByName(userDto.getRole()) : null);
        if (!id.equals(userDto.getId())) {
            throw new RuntimeException("Id from path does not match with id from request");
        }
        User updatedUser = userService.updateUser(user);
        UpdateUserDTO mappedUser = userMapper.toUpdateUserDto(updatedUser);
        mappedUser.setRole(updatedUser.getRole() != null ? updatedUser.getRole().getName() : null);
        return ResponseEntity.ok().body(mappedUser);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ViewUserDTO> getUser(@PathVariable Integer id) throws Exception {
        User user = userService.getUserById(id);
        ViewUserDTO userDto = userMapper.toViewUserDto(user);
        userDto.setRole(user.getRole() != null ? user.getRole().getName() : null);
        return ResponseEntity.ok().body(userDto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteUser(@PathVariable Integer id, HttpServletRequest request) throws Exception {
        userService.deleteUser(id, getToken(request));
        return ResponseEntity.ok().body("User with id " + id + " was deleted successfully!");
    }

    String getToken(HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            return token.substring(7);
        }
        return null;
    }
}
