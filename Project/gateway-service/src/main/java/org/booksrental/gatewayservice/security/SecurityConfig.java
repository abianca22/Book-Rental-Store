package org.booksrental.gatewayservice.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;


@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

    @Autowired
    private JwtFilter jwtFilter;

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) throws Exception {
        return http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .authorizeExchange(exchanges -> exchanges
                        .pathMatchers("/auth/**", "/books", "/categories/all", "/usersTest/**", "/booksTest/**", "/rentalsTest/**", "/actuator/**").permitAll()
                        .pathMatchers(HttpMethod.GET, "/categories/*").permitAll()
                        .pathMatchers("/users/all").hasAnyRole("admin", "employee")
                        .pathMatchers("/users/**").hasAnyRole("client", "admin", "employee")
                        .pathMatchers("/books/form", "/books/edit/**", "/books/delete/**").hasAnyRole("admin", "employee")
                        .pathMatchers(HttpMethod.GET, "/books/*").permitAll()
                        .pathMatchers("/books/rent/**").hasAnyRole("employee", "admin")
                        .pathMatchers("/categories/**").hasRole("admin")
                        .pathMatchers("/rentals").hasAnyRole("client", "employee", "admin")
                        .pathMatchers("/rentals/deleteUser/**").hasAnyRole("employee", "admin", "client")
                        .pathMatchers("/rentals/**").hasAnyRole("employee", "admin")
                        .pathMatchers("/returns/deleteUser/**").hasAnyRole("employee", "admin", "client")
                        .anyExchange().authenticated()
                )
                .addFilterAt(jwtFilter, SecurityWebFiltersOrder.AUTHENTICATION)
                .build();
    }
}
