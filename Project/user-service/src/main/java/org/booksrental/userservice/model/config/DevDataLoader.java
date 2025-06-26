package org.booksrental.userservice.model.config;
import jakarta.annotation.PostConstruct;
import org.booksrental.userservice.repository.UserRepository;
import org.booksrental.userservice.repository.RoleRepository;
import org.booksrental.userservice.model.entity.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@Profile("dev")
public class DevDataLoader implements CommandLineRunner {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    public DevDataLoader(UserRepository userRepository,
                         RoleRepository roleRepository,
                         PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) throws Exception {

        if (roleRepository.findByName("admin").isEmpty()) {
            Role role = Role.builder().name("admin").build();
            roleRepository.save(role);
        }
        if (roleRepository.findByName("employee").isEmpty()) {
            Role role = Role.builder().name("employee").build();
            roleRepository.save(role);
        }
        if (roleRepository.findByName("client").isEmpty()) {
            Role role = Role.builder().name("client").build();
            roleRepository.save(role);
        }

        Role adminRole = roleRepository.findByName("admin").get();
        Role employeeRole = roleRepository.findByName("employee").get();
        Role userRole = roleRepository.findByName("client").get();

        if (userRepository.findUserByUsername("admin").isEmpty()) {
            User admin = User.builder()
                    .username("admin")
                    .password(passwordEncoder.encode("Admin1@"))
                    .email("admin@email.com")
                    .phone("0700000000")
                    .role(adminRole)
                    .firstname("Administrator")
                    .lastname("BookStore")
                    .build();
            userRepository.save(admin);
        }
        if (userRepository.findUserByUsername("employee").isEmpty()) {
            User employee = User.builder()
                    .username("employee")
                    .password(passwordEncoder.encode("Employee1@"))
                    .email("employee@email.com")
                    .phone("0700000001")
                    .role(employeeRole)
                    .firstname("Employee")
                    .lastname("BookStore")
                    .build();
            userRepository.save(employee);
        }
        if (userRepository.findUserByUsername("client").isEmpty()) {
            User user = User.builder()
                    .username("client")
                    .password(passwordEncoder.encode("Client1@"))
                    .email("client@email.com")
                    .phone("0700000002")
                    .role(userRole)
                    .firstname("Client")
                    .lastname("BookStore")
                    .build();
            userRepository.save(user);
        }
    }
}
