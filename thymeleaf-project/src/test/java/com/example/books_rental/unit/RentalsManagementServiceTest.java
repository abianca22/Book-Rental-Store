package com.example.books_rental.unit;

import com.example.books_rental.exception.AccessDeniedException;
import com.example.books_rental.exception.NotFoundException;
import com.example.books_rental.model.entities.Book;
import com.example.books_rental.model.entities.Rental;
import com.example.books_rental.model.entities.Role;
import com.example.books_rental.model.entities.User;
import com.example.books_rental.repository.BookRepository;
import com.example.books_rental.repository.RentalRepository;
import com.example.books_rental.service.AuthenticationService;
import com.example.books_rental.service.RentalsManagementService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class RentalsManagementServiceTest {

    @Mock private RentalRepository rentalRepository;
    @Mock private BookRepository bookRepository;
    @Mock private AuthenticationService authenticationService;

    @InjectMocks private RentalsManagementService rentalsService;

    private Book book;
    private User admin;
    private User employee;
    private User client;
    private Rental rental;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        book = Book.builder()
                .id(1)
                .title("Some Book")
                .rentalPrice(10f)
                .extensionPrice(5f)
                .status(true)
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
                .startDate(LocalDate.now())
                .dueDate(LocalDate.now().plusDays(14))
                .totalPrice(book.getRentalPrice())
                .pendingRequest(false)
                .build();
    }

    @Test
    void addRental_BookAvailable_Success() {
        when(bookRepository.save(any(Book.class))).thenReturn(book);
        when(rentalRepository.save(any(Rental.class))).thenAnswer(i -> i.getArgument(0));

        Rental result = rentalsService.addRental(new Rental(), book, employee, client);

        assertNotNull(result);
        assertEquals(book, result.getBook());
        assertEquals(employee, result.getEmployee());
        assertEquals(client, result.getClient());
        assertEquals(book.getRentalPrice(), result.getTotalPrice());
        assertFalse(book.isStatus());  // status set to false after rental

        verify(bookRepository).save(book);
        verify(rentalRepository).save(any(Rental.class));
    }

    @Test
    void addRental_BookNotAvailable_ThrowsAccessDenied() {
        book.setStatus(false);
        assertThrows(AccessDeniedException.class,
                () -> rentalsService.addRental(new Rental(), book, employee, client));
        verify(bookRepository, never()).save(any());
        verify(rentalRepository, never()).save(any());
    }

    @Test
    void getRentalById_Exists_ReturnsRental() {
        when(rentalRepository.findById(1)).thenReturn(Optional.of(rental));

        Rental result = rentalsService.getRentalById(1);

        assertEquals(rental, result);
    }

    @Test
    void getRentalById_NotFound_ThrowsNotFound() {
        when(rentalRepository.findById(1)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> rentalsService.getRentalById(1));
    }

    @Test
    void updateRental_AsEmployee_ExtendsDueDateAndPrice() {
        Rental foundRental = Rental.builder()
                .id(1)
                .book(book)
                .client(client)
                .employee(employee)
                .startDate(LocalDate.now())
                .dueDate(LocalDate.now().plusDays(14))
                .totalPrice(book.getRentalPrice())
                .pendingRequest(false)
                .build();
        when(authenticationService.getCurrentUser()).thenReturn(employee);
        when(rentalRepository.findById(rental.getId())).thenReturn(Optional.of(foundRental));
        when(rentalRepository.save(any(Rental.class))).thenAnswer(i -> i.getArgument(0));
        Rental updated = rentalsService.updateRental(foundRental, null);
        assertEquals(rental.getDueDate().plusDays(7), updated.getDueDate());
        assertEquals(rental.getTotalPrice() + book.getExtensionPrice(), updated.getTotalPrice());
        verify(rentalRepository).save(any(Rental.class));
    }

    @Test
    void updateRental_AsClient_ThrowsAccessDenied() {
        when(authenticationService.getCurrentUser()).thenReturn(client);
        when(rentalRepository.findById(rental.getId())).thenReturn(Optional.of(rental));

        assertThrows(AccessDeniedException.class, () -> rentalsService.updateRental(rental, null));
    }

    @Test
    void updateRental_ChangeEmployee_AsAdmin_Success() {
        User newEmployee = User.builder()
                .id(4)
                .role(Role.builder().name("employee").build())
                .build();

        when(authenticationService.getCurrentUser()).thenReturn(admin);
        when(rentalRepository.findById(rental.getId())).thenReturn(Optional.of(rental));
        when(rentalRepository.save(any(Rental.class))).thenAnswer(i -> i.getArgument(0));

        Rental updated = rentalsService.updateRental(rental, newEmployee);

        assertEquals(newEmployee, updated.getEmployee());
    }

    @Test
    void updateRental_ChangeEmployee_AsNonAdmin_ThrowsAccessDenied() {
        when(authenticationService.getCurrentUser()).thenReturn(employee);
        when(rentalRepository.findById(rental.getId())).thenReturn(Optional.of(rental));

        assertThrows(AccessDeniedException.class, () -> rentalsService.updateRental(rental, employee));
    }

    @Test
    void getRentalsByClient_AsClientOwn_ReturnsRentals() {
        when(authenticationService.getCurrentUser()).thenReturn(client);
        when(rentalRepository.findByClientId(client.getId())).thenReturn(List.of(rental));

        List<Rental> rentals = rentalsService.getRentalsByClient(client);

        assertFalse(rentals.isEmpty());
        verify(rentalRepository).findByClientId(client.getId());
    }

    @Test
    void getRentalsByClient_AsClientOther_ThrowsAccessDenied() {
        User otherClient = User.builder().id(99).role(Role.builder().name("client").build()).build();
        when(authenticationService.getCurrentUser()).thenReturn(client);

        assertThrows(AccessDeniedException.class, () -> rentalsService.getRentalsByClient(otherClient));
    }

    @Test
    void updatePendingRequest_AsClientOrEmployee_Success() {
        rental.setPendingRequest(false);
        when(rentalRepository.findById(1)).thenReturn(Optional.of(rental));
        when(authenticationService.getCurrentUser()).thenReturn(client);
        when(rentalRepository.save(any(Rental.class))).thenAnswer(i -> i.getArgument(0));

        rentalsService.updatePendingRequest(1, true);
        assertTrue(rental.isPendingRequest());
        verify(rentalRepository).save(rental);
    }

    @Test
    void updatePendingRequest_AsUnauthorizedUser_ThrowsAccessDenied() {
        User unauthorizedUser = User.builder().id(99).role(Role.builder().name("client").build()).build();
        when(rentalRepository.findById(1)).thenReturn(Optional.of(rental));
        when(authenticationService.getCurrentUser()).thenReturn(unauthorizedUser);

        assertThrows(AccessDeniedException.class, () -> rentalsService.updatePendingRequest(1, true));
    }
}
