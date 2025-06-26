package org.booksrental.rentalservice.service;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.booksrental.rentalservice.model.dto.UserDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;

@Service
public class UserService {
    @Autowired
    private RestTemplate restTemplate;

    @Value("${gateway.url}")
    private String gatewayUrl;

    @CircuitBreaker(name = "userService", fallbackMethod = "getUserByIdFallback")
    public UserDTO getUserById(Integer userId, String token) {
        String url = gatewayUrl + "/users/" + userId;
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + token);

        HttpEntity<Void> entity = new HttpEntity<>(headers);
        ResponseEntity<UserDTO> response = restTemplate.exchange(url, HttpMethod.GET, entity, UserDTO.class);

        return response.getBody();
    }

    public UserDTO getUserByIdFallback(Integer userId, String token, Throwable throwable) {
        throw new RuntimeException("User service unavailable. Please try again later");
    }

    @CircuitBreaker(name = "userService", fallbackMethod = "getUsersByKeywordFallback")
    public List<UserDTO> getUsersByKeyword(String username, String token) {
        String url = gatewayUrl + "/users/all?keyword=" + username;
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + token);

        HttpEntity<Void> entity = new HttpEntity<>(headers);
        ResponseEntity<List<UserDTO>> response = restTemplate.exchange(url, HttpMethod.GET, entity, new ParameterizedTypeReference<List<UserDTO>>() {}
        );

        return response.getBody();
    }

    public List<UserDTO> getUsersByKeywordFallback(String username, String token, Throwable throwable) {
        return new ArrayList<>();
    }

}
