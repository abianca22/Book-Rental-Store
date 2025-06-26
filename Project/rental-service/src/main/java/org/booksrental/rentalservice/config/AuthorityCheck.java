package org.booksrental.rentalservice.config;

import org.booksrental.rentalservice.model.dto.CurrentUserDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class AuthorityCheck {

    @Autowired
    private JwtUtil jwtUtil;

    public CurrentUserDTO getCurrentUser(String token) {
        if (!jwtUtil.validateToken(token)) {
            throw new RuntimeException("Invalid JWT token");
        }

        String username = jwtUtil.extractUsername(token);
        String role = jwtUtil.extractClaim(token, claims -> claims.get("role", String.class));
        Integer id = jwtUtil.extractClaim(token, claims -> claims.get("id", Integer.class));

        return CurrentUserDTO.builder().id(id)
                .username(username)
                .role(role)
                .build();
    }
}
