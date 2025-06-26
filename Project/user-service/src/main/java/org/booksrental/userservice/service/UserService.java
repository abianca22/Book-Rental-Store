package org.booksrental.userservice.service;

import org.booksrental.userservice.model.entity.Role;
import org.booksrental.userservice.model.entity.User;
import org.booksrental.userservice.repository.RoleRepository;
import org.booksrental.userservice.repository.UserRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    private UserRepository userRepository;
    private RoleRepository roleRepository;
    private AuthenticationService authenticationService;
    private RentalService rentalService;
    private ReturnService returnService;


    public UserService(UserRepository userRepository,
                       RoleRepository roleRepository,
                       AuthenticationService authenticationService,
                       RentalService rentalService,
                       ReturnService returnService) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.authenticationService = authenticationService;
        this.rentalService = rentalService;
        this.returnService = returnService;
    }

    public User getUserById(int id) throws Exception {
        Optional<User> user = userRepository.findById(id);
        if(user.isEmpty()) {
            throw new Exception("User with id " + id + " does not exist!");
        }
        return user.get();
    }

    public User getUserByUsername(String username) throws Exception {
        Optional<User> user = userRepository.findUserByUsername(username);
        if(user.isEmpty()) {
            throw new Exception("User with username " + username + " does not exist!");
        }
        return user.get();
    }

    public User getUserByEmail(String email) throws Exception {
        Optional<User> user = userRepository.findByEmail(email);
        if(user.isEmpty()) {
            throw new Exception("User with email " + email + " does not exist!");
        }
        return user.get();
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public User createUser(User user) throws Exception {
        checkUsername(user);
        checkEmail(user);
        checkPhone(user);
        user.setPassword(this.passwordEncoder(user.getPassword()));
        Optional<Role> role = roleRepository.findByName("client");
        if (role.isEmpty()) {
            throw new Exception("Role with name 'client' does not exist!");
        }
        user.setRole(role.get());
        return userRepository.save(user);
    }

    public User updateUser(User user) throws Exception {
        Optional<User> foundUser = userRepository.findById(user.getId());
        User requesterUser = authenticationService.getCurrentUser();
        if (requesterUser.getRole().getName().equals("client") && requesterUser.getId() != user.getId()) {
            throw new Exception("Users can only be updated by staff members or the user!");
        }
        if (foundUser.isEmpty()) {
            throw new Exception("User with id " + user.getId() + " does not exist!");
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
        User savedUser = userRepository.save(foundUser.get());
        if (savedUser.getId() == requesterUser.getId()) {
            authenticationService.updateSecurityContext(savedUser);
        }
        return savedUser;
    }

    public void deleteUser(int id, String token) throws Exception {
        Optional<User> user = userRepository.findById(id);
        User requesterUser = authenticationService.getCurrentUser();
        if (requesterUser.getId() != id) {
            throw new Exception("Users can delete their own account only!");
        }
        if(user.isEmpty()) {
            throw new Exception("User with id " + id + " does not exist!");
        }
        rentalService.updateRentals(id, token);
        returnService.updateReturns(id, token);
        userRepository.deleteById(id);
    }

    public String passwordEncoder(String password) {
        return new BCryptPasswordEncoder().encode(password);
    }

    public void checkUsername(User user) throws Exception {
        Optional<User> alreadyExistingUser = userRepository.findUserByUsername(user.getUsername());
        if (alreadyExistingUser.isPresent() && alreadyExistingUser.get().getId() != user.getId()) {
            throw new Exception("User with username " + user.getUsername() + " already exists!");
        }
    }

    public void checkEmail(User user) throws Exception {
        Optional<User> alreadyExistingUser = userRepository.findByEmail(user.getEmail());
        if (alreadyExistingUser.isPresent() && alreadyExistingUser.get().getId() != user.getId()) {
            throw new Exception("User with email " + user.getEmail() + " already exists!");
        }
    }

    public void checkPhone(User user) throws Exception {
        Optional<User> alreadyExistingUser = userRepository.findByPhone(user.getPhone());
        if (alreadyExistingUser.isPresent() && alreadyExistingUser.get().getId() != user.getId()) {
            throw new Exception("User with phone " + user.getPhone() + " already exists!");
        }
    }

    public List<User> getAllUsersByFilters(String keyword, Integer role) {
        return userRepository.findAllByFilters(keyword, role);
    }

    public void deleteAllUsers(String token) throws Exception {
        for (var user: userRepository.findAll()) {
            deleteUser(user.getId(), token);
        }
    }

}
