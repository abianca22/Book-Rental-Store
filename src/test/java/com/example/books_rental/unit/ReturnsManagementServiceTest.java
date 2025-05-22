package com.example.books_rental.unit;

import com.example.books_rental.exception.AccessDeniedException;
import com.example.books_rental.exception.NotFoundException;
import com.example.books_rental.model.entities.*;
import com.example.books_rental.repository.BookRepository;
import com.example.books_rental.repository.RentalRepository;
import com.example.books_rental.repository.ReturnRepository;
import com.example.books_rental.service.AuthenticationService;
import com.example.books_rental.service.ReturnsManagementService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ReturnsManagementServiceTest {

    @Mock private ReturnRepository returnRepository;
    @Mock private BookRepository bookRepository;
    @Mock private RentalRepository rentalRepository;
    @Mock private AuthenticationService authenticationService;

    @InjectMocks private ReturnsManagementService returnsService;

    private Rental rental;
    private Return returnEntity;
    private Book book;
    private User admin;
    private User employee;
    private User client;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        book = Book.builder()
                .id(1)
                .title("Test Book")
                .status(false)
                .rentalPrice(10f)
                .extensionPrice(5f)
                .build();

        admin = User.builder()
                .id(1)
                .username("admin")
                .role(Role.builder().name("admin").build())
                .build();

        employee = User.builder()
                .id(2)
                .username("employee")
                .role(Role.builder().name("employee").build())
                .build();

        client = User.builder()
                .id(3)
                .username("client")
                .role(Role.builder().name("client").build())
                .build();

        rental = Rental.builder()
                .id(1)
                .book(book)
                .client(client)
                .employee(employee)
                .pendingRequest(true)
                .build();

        returnEntity = Return.builder()
                .rental(rental)
                .employee(employee)
                .build();
    }

    @Test
    void addReturn_AsAdmin_Success() {
        when(authenticationService.getCurrentUser()).thenReturn(admin);
        when(returnRepository.save(any(Return.class))).thenAnswer(i -> i.getArgument(0));
        when(bookRepository.save(any(Book.class))).thenReturn(book);
        when(rentalRepository.save(any(Rental.class))).thenReturn(rental);

        Return result = returnsService.addReturn(new Return(), rental, employee);

        assertNotNull(result);
        assertEquals(rental, result.getRental());
        assertEquals(employee, result.getEmployee());
        assertEquals(LocalDate.now(), result.getReturnDate());
        assertTrue(result.getRental().getBook().isStatus());
        assertFalse(result.getRental().isPendingRequest());

        verify(bookRepository).save(book);
        verify(rentalRepository).save(rental);
        verify(returnRepository).save(any(Return.class));
    }

    @Test
    void addReturn_AlreadyReturned_ThrowsNotFound() {
        rental.setAssociatedReturn(Return.builder().id(99).build());

        when(authenticationService.getCurrentUser()).thenReturn(admin);

        Return newReturn = new Return();
        assertThrows(NotFoundException.class, () -> returnsService.addReturn(newReturn, rental, employee));

        verify(returnRepository, never()).save(any());
    }

    @Test
    void addReturn_AsClient_ThrowsAccessDenied() {
        when(authenticationService.getCurrentUser()).thenReturn(client);

        Return newReturn = new Return();
        assertThrows(AccessDeniedException.class, () -> returnsService.addReturn(newReturn, rental, employee));

        verify(returnRepository, never()).save(any());
    }

    @Test
    void addReturn_AsOtherEmployee_ThrowsAccessDenied() {
        User otherEmployee = User.builder()
                .id(5)
                .role(Role.builder().name("employee").build())
                .build();

        when(authenticationService.getCurrentUser()).thenReturn(otherEmployee);

        Return newReturn = new Return();
        assertThrows(AccessDeniedException.class, () -> returnsService.addReturn(newReturn, rental, employee));

        verify(returnRepository, never()).save(any());
    }

    @Test
    void addReturn_SetsBookAvailableAndClearsPendingRequest() {
        when(authenticationService.getCurrentUser()).thenReturn(admin);
        when(returnRepository.save(any(Return.class))).thenAnswer(i -> i.getArgument(0));
        when(bookRepository.save(any(Book.class))).thenReturn(book);
        when(rentalRepository.save(any(Rental.class))).thenReturn(rental);

        Return result = returnsService.addReturn(new Return(), rental, employee);

        assertTrue(result.getRental().getBook().isStatus());
        assertFalse(result.getRental().isPendingRequest());
    }
}
