package com.example.books_rental.unit;

import com.example.books_rental.exception.AccessDeniedException;
import com.example.books_rental.exception.NotFoundException;
import com.example.books_rental.model.entities.*;
import com.example.books_rental.repository.BookRepository;
import com.example.books_rental.service.AuthenticationService;
import com.example.books_rental.service.BooksManagementService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.mock.web.MockMultipartFile;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class BooksManagementServiceTest {

    @Mock private BookRepository bookRepository;
    @Mock private AuthenticationService authenticationService;

    @InjectMocks private BooksManagementService booksService;

    private Book book;
    private Category category;
    private User admin;
    private User client;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        category = Category.builder().id(1).name("Fantasy").build();

        book = Book.builder()
                .id(1)
                .title("Test Book")
                .author("Author")
                .publishingHouse("Publisher")
                .rentalPrice(10)
                .extensionPrice(5)
                .status(true)
                .categories(List.of(category))
                .build();

        admin = User.builder()
                .id(1)
                .username("admin")
                .role(Role.builder().name("admin").build())
                .build();

        client = User.builder()
                .id(2)
                .username("client")
                .role(Role.builder().name("client").build())
                .build();
    }

    @Test
    void addBook_AsAdmin_Success() {
        when(authenticationService.getCurrentUser()).thenReturn(admin);
        when(bookRepository.save(any(Book.class))).thenReturn(book);

        MockMultipartFile image = new MockMultipartFile("image", "book.jpg", "image/jpeg", new byte[10]);

        Book result = booksService.addBook(book, List.of(category), image);

        assertNotNull(result);
        assertEquals("Test Book", result.getTitle());
        verify(bookRepository).save(any(Book.class));
    }

    @Test
    void addBook_AsClient_ThrowsAccessDenied() {
        when(authenticationService.getCurrentUser()).thenReturn(client);

        assertThrows(RuntimeException.class, () -> booksService.addBook(book, List.of(category), new MockMultipartFile("file", new byte[0])));
        verify(bookRepository, never()).save(any());
    }

    @Test
    void getBookById_BookExists_ReturnsBook() {
        when(bookRepository.findById(1)).thenReturn(Optional.of(book));

        Book result = booksService.getBookById(1);
        assertEquals("Test Book", result.getTitle());
    }

    @Test
    void getBookById_NotFound_ThrowsException() {
        when(bookRepository.findById(1)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> booksService.getBookById(1));
    }

    @Test
    void updateBook_AsAdmin_Success() {
        Book updated = Book.builder()
                .id(1)
                .title("Updated Title")
                .author("Updated Author")
                .rentalPrice(12)
                .extensionPrice(6)
                .status(false)
                .build();

        when(bookRepository.findById(1)).thenReturn(Optional.of(book));
        when(authenticationService.getCurrentUser()).thenReturn(admin);
        when(bookRepository.save(any(Book.class))).thenReturn(updated);

        Book result = booksService.updateBook(updated, List.of(category), new MockMultipartFile("file", new byte[0]));

        assertEquals("Updated Title", result.getTitle());
        assertFalse(result.isStatus());
    }

    @Test
    void updateBook_AsClient_ThrowsAccessDenied() {
        when(authenticationService.getCurrentUser()).thenReturn(client);

        assertThrows(AccessDeniedException.class, () -> booksService.updateBook(book, List.of(category), new MockMultipartFile("file", new byte[0])));
    }

    @Test
    void deleteBook_AsAdmin_Success() {
        book.setRentals(List.of()); // empty rental list
        when(bookRepository.findById(1)).thenReturn(Optional.of(book));
        when(authenticationService.getCurrentUser()).thenReturn(admin);

        booksService.deleteBook(1);
        verify(bookRepository).delete(book);
    }

    @Test
    void deleteBook_NotFound_ThrowsException() {
        when(bookRepository.findById(1)).thenReturn(Optional.empty());
        when(authenticationService.getCurrentUser()).thenReturn(admin);

        assertThrows(NotFoundException.class, () -> booksService.deleteBook(1));
    }

    @Test
    void deleteBook_AsClient_ThrowsAccessDenied() {
        when(bookRepository.findById(1)).thenReturn(Optional.of(book));
        when(authenticationService.getCurrentUser()).thenReturn(client);

        assertThrows(AccessDeniedException.class, () -> booksService.deleteBook(1));
        verify(bookRepository, never()).delete(any());
    }
}
