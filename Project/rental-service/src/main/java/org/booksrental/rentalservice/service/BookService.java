package org.booksrental.rentalservice.service;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.booksrental.rentalservice.exception.NotFoundException;
import org.booksrental.rentalservice.model.dto.BookDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;

@Service
public class BookService {
    @Autowired
    private RestTemplate restTemplate;

    @Value("${gateway.url}")
    private String gatewayUrl;

    @CircuitBreaker(name = "bookService", fallbackMethod = "getBookByIdFallback")
    public BookDTO getBookById(Integer bookId, String token) {
        String url = gatewayUrl + "/books/" + bookId;
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + token);

        HttpEntity<Void> entity = new HttpEntity<>(headers);
        ResponseEntity<BookDTO> response = restTemplate.exchange(url, HttpMethod.GET, entity, BookDTO.class);

        return response.getBody();
    }

    public BookDTO getBookByIdFallback(Integer bookId, String token, Throwable throwable) {
        return BookDTO.builder().status(false).build();
    }

    @CircuitBreaker(name = "bookService", fallbackMethod = "getAllBooksFallback")
    public List<BookDTO> getByKeyword(String keyword, String token) {
        String url = gatewayUrl + "/books?search=" + keyword;
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + token);

        HttpEntity<Void> entity = new HttpEntity<>(headers);
        ResponseEntity<List<BookDTO>> response = restTemplate.exchange(url, HttpMethod.GET, entity,  new ParameterizedTypeReference<List<BookDTO>>() {});

        return response.getBody();
    }

    public List<BookDTO> getAllBooksFallback(String keyword, String token, Throwable throwable) {
        return new ArrayList<>();
    }

    @CircuitBreaker(name = "bookService", fallbackMethod = "updateBookFallback")
    public BookDTO updateBook(BookDTO bookDTO, String token) {
        String url = gatewayUrl + "/books/form?bookId=" + bookDTO.getId();
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + token);  // dacă ai token
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<BookDTO> entity = new HttpEntity<>(bookDTO, headers);

        ResponseEntity<BookDTO> response = restTemplate.postForEntity(
                url,
                entity,
                BookDTO.class
        );

        return response.getBody();
    }

    public BookDTO updateBookFallback(BookDTO bookDTO, String token, Throwable throwable) {
        throw new NotFoundException("Book service not available. Please try again later.");
    }

}
