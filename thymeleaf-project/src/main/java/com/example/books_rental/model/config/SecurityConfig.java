package com.example.books_rental.model.config;

import com.example.books_rental.service.AuthenticationService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
//@Profile("mysql")
public class SecurityConfig {

    private final AuthenticationService userDetailsService;

    public SecurityConfig(AuthenticationService userDetailsService) {
        this.userDetailsService = userDetailsService;
    }

    @Bean
    SecurityFilterChain securityFilterChain(final HttpSecurity httpSecurity) throws Exception {
        return httpSecurity.authorizeHttpRequests(auth -> auth
                .requestMatchers("/webjars/**", "/login", "/resources/**", "/register", "/books", "/categories").permitAll()
                        .requestMatchers(HttpMethod.GET, "/books/getimage/*").permitAll()
                        .requestMatchers("/books/form", "/books/edit/**", "/books/delete/**").hasAnyRole("admin", "employee")
                        .requestMatchers(HttpMethod.GET, "/books/*").permitAll()
                        .requestMatchers("/books/rent/**").hasAnyRole("employee", "admin")
                        .requestMatchers("/rentals/edit/*").hasAnyRole("admin", "employee")
                        .requestMatchers("/users").hasAnyRole("admin", "employee")
                        .requestMatchers("/users/edit/*").hasAnyRole("client", "admin")
                        .requestMatchers("/users/profile", "/users/delete").hasAnyRole("client", "admin", "employee")
                        .requestMatchers("/categories/delete/**", "/categories/edit/**", "/categories/add").hasRole("admin")
                        .requestMatchers("/categories/*").permitAll()
                .requestMatchers("/books/**", "/rentals", "/rentals/editClient/*").hasAnyRole("admin", "employee", "client")
                .anyRequest()
                .authenticated()
        )
                .userDetailsService(userDetailsService)
                .formLogin(formLogin -> formLogin
                        .loginPage("/login")
                        .permitAll()
                        .loginProcessingUrl("/perform_login")
                        .defaultSuccessUrl("/books", true)
                )
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/books")
                        .invalidateHttpSession(true)
                        .clearAuthentication(true)
                        .deleteCookies("JSESSIONID")
                )
                .exceptionHandling(ex -> ex.accessDeniedPage("/accessDenied"))
                .httpBasic(AbstractHttpConfigurer::disable
                )
                .build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

}

