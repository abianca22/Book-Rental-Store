package com.example.books_rental.service;

import com.example.books_rental.exception.AccessDeniedException;
import com.example.books_rental.exception.ExistingDataException;
import com.example.books_rental.exception.NotFoundException;
import com.example.books_rental.model.entities.Role;
import com.example.books_rental.model.entities.User;
import com.example.books_rental.repository.RentalRepository;
import com.example.books_rental.repository.ReturnRepository;
import com.example.books_rental.repository.RoleRepository;
import com.example.books_rental.repository.UserRepository;
import com.example.books_rental.validator.Validator;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UsersManagementService {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final AuthenticationService authenticationService;
    private final RentalRepository rentalRepository;
    private final ReturnRepository returnRepository;

    public UsersManagementService(UserRepository userRepository, RoleRepository roleRepository, AuthenticationService authenticationService, RentalRepository rentalRepository, ReturnRepository returnRepository) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.authenticationService = authenticationService;
        this.rentalRepository = rentalRepository;
        this.returnRepository = returnRepository;
    }

    public User getUserById(int id) {
        Optional<User> user = userRepository.findById(id);
        if(user.isEmpty()) {
            throw new NotFoundException("User with id " + id + " does not exist!");
        }
        return user.get();
    }

    public User getUserByUsername(String username) {
        Optional<User> user = userRepository.findUserByUsername(username);
        if(user.isEmpty()) {
            throw new NotFoundException("User with username " + username + " does not exist!");
        }
        return user.get();
    }

    public User getUserByEmail(String email) {
        Optional<User> user = userRepository.findByEmail(email);
        if(user.isEmpty()) {
            throw new NotFoundException("User with email " + email + " does not exist!");
        }
        return user.get();
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public User createUser(User user) {
        checkUsername(user);
        checkEmail(user);
        checkPhone(user);
        user.setPassword(this.passwordEncoder(user.getPassword()));
        Optional<Role> role = roleRepository.findByName("client");
        if (role.isEmpty()) {
            throw new NotFoundException("Role with name 'client' does not exist!");
        }
        user.setRole(role.get());
        return userRepository.save(user);
    }

    public User updateUser(User user) {
        Optional<User> foundUser = userRepository.findById(user.getId());
        User requesterUser = authenticationService.getCurrentUser();
        if (requesterUser.getRole().getName().equals("client") && requesterUser.getId() != user.getId()) {
            throw new AccessDeniedException("Users can only be updated by staff members or the user!");
        }
        if (foundUser.isEmpty()) {
            throw new NotFoundException("User with id " + user.getId() + " does not exist!");
        }
        if (user.getUsername() != null && !user.getUsername().equals(foundUser.get().getUsername())) {
            this.checkUsername(user);
            foundUser.get().setUsername(user.getUsername());
        }
        if (user.getPassword() != null && !user.getPassword().isEmpty()) {
            foundUser.get().setPassword(this.passwordEncoder(user.getPassword()));
        }
        if (user.getRole() != null) {
            foundUser.get().setRole(user.getRole());
        }
        if (user.getEmail() != null) {
            this.checkEmail(user);
            foundUser.get().setEmail(user.getEmail());
        }
        if (user.getLastname() != null) {
            foundUser.get().setLastname(user.getLastname());
        }
        if (user.getFirstname() != null) {
            foundUser.get().setFirstname(user.getFirstname());
        }
        Validator.validateObject(foundUser.get());
        User savedUser = userRepository.save(foundUser.get());
        if (savedUser.getId() == requesterUser.getId()) {
            authenticationService.updateSecurityContext(savedUser);
        }
        return savedUser;
    }

    public void deleteUser(int id) {
        Optional<User> user = userRepository.findById(id);
        User requesterUser = authenticationService.getCurrentUser();
        if (requesterUser.getId() != id) {
            throw new AccessDeniedException("Users can delete their own account only!");
        }
        if(user.isEmpty()) {
            throw new NotFoundException("User with id " + id + " does not exist!");
        }
        if (user.get().getClientRentals() != null && !user.get().getClientRentals().isEmpty()) {
            for (var rental : user.get().getClientRentals()) {
                rental.setClient(null);
                rentalRepository.save(rental);
            }
        }
        if (user.get().getEmployeeRentals() != null && !user.get().getEmployeeRentals().isEmpty()) {
            for (var rental : user.get().getEmployeeRentals()) {
                rental.setEmployee(null);
                rentalRepository.save(rental);
            }
        }
        if (user.get().getReturns() != null && !user.get().getReturns().isEmpty()) {
            for (var ret: user.get().getReturns()) {
                ret.setEmployee(null);
                returnRepository.save(ret);
            }
        }
        userRepository.deleteById(id);
    }

    public String passwordEncoder(String password) {
        return new BCryptPasswordEncoder().encode(password);
    }

    public void checkUsername(User user) {
        Optional<User> alreadyExistingUser = userRepository.findUserByUsername(user.getUsername());
        if (alreadyExistingUser.isPresent() && alreadyExistingUser.get().getId() != user.getId()) {
            throw new ExistingDataException("User with username " + user.getUsername() + " already exists!");
        }
    }

    public void checkEmail(User user) {
        Optional<User> alreadyExistingUser = userRepository.findByEmail(user.getEmail());
        if (alreadyExistingUser.isPresent() && alreadyExistingUser.get().getId() != user.getId()) {
            throw new ExistingDataException("User with email " + user.getEmail() + " already exists!");
        }
    }

    public void checkPhone(User user) {
        Optional<User> alreadyExistingUser = userRepository.findByPhone(user.getPhone());
        if (alreadyExistingUser.isPresent() && alreadyExistingUser.get().getId() != user.getId()) {
            throw new ExistingDataException("User with phone " + user.getPhone() + " already exists!");
        }
    }

    public Page<User> getAllUsersByFilters(String keyword, Integer role, Pageable pageable) {
        return userRepository.findAllByFilters(keyword, role, pageable);
    }

    public void deleteAllUsers() {
        for (var user: userRepository.findAll()) {
            deleteUser(user.getId());
        }
    }
}
