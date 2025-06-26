package org.booksrental.userservice.service;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.booksrental.userservice.model.entity.User;
import org.booksrental.userservice.model.entity.Role;
import org.booksrental.userservice.repository.UserRepository;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
//@Profile("mysql")
public class AuthenticationService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public User loadUserByUsername(String username) throws UsernameNotFoundException {
        final var userFromDb = userRepository.findUserByUsername(username);
        User user = userFromDb
                .orElseThrow(() -> new UsernameNotFoundException("Username " + username + " not found!"));
        return User.builder().username(user.getUsername())
                .password(user.getPassword())
                .enabled(user.getEnabled())
                .accountNonExpired(user.getAccountNonExpired())
                .credentialsNonExpired(user.getCredentialsNonExpired())
                .accountNonLocked(user.getAccountNonLocked())
                .role(user.getRole())
                .build();
    }

    private Collection<? extends GrantedAuthority> getAuthorities(Set<Role> roles) {
        if (roles == null || roles.isEmpty()) {
            return new HashSet<>();
        }
        return roles.stream()
                .map(Role::getName)
                .map(roleName -> new SimpleGrantedAuthority("ROLE_" + roleName))
                .collect(Collectors.toSet());
    }

    public User getCurrentUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        log.info("Current user: {}", username);
        return userRepository.findUserByUsername(username)
                .orElse(null);
    }

    public void updateSecurityContext(User user) {
        UserDetails updatedUserDetails = this.loadUserByUsername(user.getUsername());
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                updatedUserDetails,
                updatedUserDetails.getPassword(),
                updatedUserDetails.getAuthorities()
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    public boolean checkIfAuthenticated() {
        return getCurrentUser() != null;
    }
}
