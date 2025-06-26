package org.booksrental.userservice.service;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.booksrental.userservice.exception.NoDataFound;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;


@Service
public class RentalService {

    @Autowired
    private RestTemplate restTemplate;

    @Value("${gateway.url}")
    private String gatewayUrl;

    @CircuitBreaker(name = "rentalService", fallbackMethod = "updateRentalsFallback")
    public String updateRentals(Integer userId, String token) {
        String url = gatewayUrl + "/rentals/deleteUser/" + userId;
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + token);  // dacă ai token
        headers.setContentType(MediaType.APPLICATION_JSON);

        ResponseEntity<String> response = restTemplate.postForEntity(
                url,
                new HttpEntity<>(headers),
                String.class
        );

        return response.getBody();
    }

    public String updateRentalsFallback(Integer userId, String token, Throwable throwable) {
        throw new NoDataFound("Rental service unavailable, please try again later.");
    }
}
