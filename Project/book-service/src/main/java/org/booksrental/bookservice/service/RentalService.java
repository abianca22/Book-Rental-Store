package org.booksrental.bookservice.service;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import jakarta.persistence.Entity;
import org.booksrental.bookservice.exception.NotFoundException;
import org.booksrental.bookservice.model.dto.FullRentalDTO;
import org.booksrental.bookservice.model.dto.RentalDTO;
import org.booksrental.bookservice.model.dto.UpdateBookDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Service
public class RentalService {
    @Autowired
    private RestTemplate restTemplate;

    @Value("${gateway.url}")
    private String gatewayUrl;


    @CircuitBreaker(name = "rentalService", fallbackMethod = "addRentalFallback")
    public FullRentalDTO addRental(RentalDTO rentalDTO, String token) {
        String url = gatewayUrl + "/rentals";
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + token);
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<RentalDTO> entity = new HttpEntity<>(rentalDTO, headers);

        ResponseEntity<FullRentalDTO> response = restTemplate.postForEntity(
                url,
                entity,
                FullRentalDTO.class
        );

        return response.getBody();
    }

    public FullRentalDTO addRentalFallback(RentalDTO rentalDTO, String token, Throwable throwable) {
        throw new NotFoundException("Rental service unavailable. Please try again later.");
    }

    @CircuitBreaker(name = "rentalService", fallbackMethod = "getRentalsFallback")
    public List<FullRentalDTO> bookRentals(Integer bookId, String token) {
        String url = gatewayUrl + "/rentals/all?bookId=" + bookId;
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + token);

        HttpEntity<Void> entity = new HttpEntity<>(headers);
        ResponseEntity<List<FullRentalDTO>> response = restTemplate.exchange(url, HttpMethod.GET, entity,  new ParameterizedTypeReference<List<FullRentalDTO>>() {});

        return response.getBody();
    }

    public List<FullRentalDTO> getRentalsFallback(Integer bookId, String token, Throwable throwable) {
        return List.of();
    }

    @CircuitBreaker(name = "rentalService", fallbackMethod = "updateRentalsFallback")
    public String updateRentals(Integer bookId, String token) {
        String url = gatewayUrl + "/rentals/deleteBook/" + bookId;
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

    public String updateRentalsFallback(Integer bookId, String token, Throwable throwable) {
        throw new NotFoundException("Rental service unavailable. Please try again later.");
    }
}
