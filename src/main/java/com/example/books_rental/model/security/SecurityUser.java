//package com.example.books_rental.model.security;
//
//import com.example.books_rental.model.entities.User;
//import org.springframework.security.core.GrantedAuthority;
//import org.springframework.security.core.userdetails.UserDetails;
//
//import java.util.Collection;
//import java.util.stream.Stream;
//
//public class SecurityUser implements UserDetails {
//
//    private final User user;
//
//    public SecurityUser(User user) {
//        this.user = user;
//    }
//
//
//    @Override
//    public Collection<? extends GrantedAuthority> getAuthorities() {
//        return Stream.of(user.getRole()).map(SecurityRole::new).toList();
//    }
//
//    @Override
//    public String getPassword() {
//        return user.getPassword();
//    }
//
//    @Override
//    public String getUsername() {
//        return user.getUsername();
//    }
//}
