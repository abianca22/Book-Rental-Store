package com.example.books_rental.service;

import com.example.books_rental.exception.AccessDeniedException;
import com.example.books_rental.exception.NotFoundException;
import com.example.books_rental.model.entities.Book;
import com.example.books_rental.model.entities.Category;
import com.example.books_rental.model.entities.Rental;
import com.example.books_rental.model.entities.User;
import com.example.books_rental.repository.BookRepository;
import com.example.books_rental.repository.CategoryRepository;
import com.example.books_rental.repository.RentalRepository;
import com.example.books_rental.repository.UserRepository;
import com.example.books_rental.validator.Validator;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@Service
public class BooksManagementService {
    private final BookRepository bookRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final AuthenticationService authenticationService;
    private final RentalRepository rentalRepository;

    public BooksManagementService(BookRepository bookRepository, UserRepository userRepository, CategoryRepository categoryRepository, AuthenticationService authenticationService, RentalRepository rentalRepository) {
        this.bookRepository = bookRepository;
        this.userRepository = userRepository;
        this.categoryRepository = categoryRepository;
        this.authenticationService = authenticationService;
        this.rentalRepository = rentalRepository;
    }

    public Book addBook(Book book, List<Category> categories, MultipartFile file) {
        User requester = authenticationService.getCurrentUser();
        book.setCategories(categories);
        book.setStatus(true);
        if (!file.isEmpty()) {
            try {
                book.setImage(file.getBytes());
            } catch (Exception e) {
                throw new RuntimeException("Error while uploading image: " + e.getMessage());
            }
        }
        if (requester.getRole().getName().equals("client")) {
            throw new RuntimeException("Books can only be added by staff members!");
        }
        return bookRepository.save(book);
    }

    public Book getBookById(int id) {
        return bookRepository.findById(id).orElseThrow(() -> new NotFoundException("Book with id " + id + " does not exist!"));
    }

    public List<Book> getAllBooks() {
        return bookRepository.findAll();
    }

    public Book updateBook(Book book, List<Category> categories, MultipartFile file) {
        User requester = authenticationService.getCurrentUser();
        if (requester.getRole().getName().equals("client")) {
            throw new AccessDeniedException("Books can only be updated by staff members!");
        }
        Book foundBook = bookRepository.findById(book.getId()).orElseThrow(() -> new NotFoundException("Book with id " + book.getId() + " does not exist!"));
        if (book.getTitle() != null) {
            foundBook.setTitle(book.getTitle());
        }
        if (book.getAuthor() != null) {
            foundBook.setAuthor(book.getAuthor());
        }
        if (book.getCategories() != null && !book.getCategories().isEmpty()) {
            foundBook.setCategories(book.getCategories());
        }
        if (book.getDescription() != null) {
            foundBook.setDescription(book.getDescription());
        }
        if (categories != null && !categories.isEmpty()) {
            foundBook.setCategories(categories);
        }
        if (book.isStatus() != foundBook.isStatus()) {
            foundBook.setStatus(book.isStatus());
        }
        if(book.getPublishingHouse() != null && !book.getPublishingHouse().isEmpty()) {
            foundBook.setPublishingHouse(book.getPublishingHouse());
        }
        if (book.getExtensionPrice() >= 0) {
            foundBook.setExtensionPrice(book.getExtensionPrice());
        }
        if (book.getRentalPrice() > 0) {
            foundBook.setRentalPrice(book.getRentalPrice());
        }
        if (!file.isEmpty()) {
            try {
                foundBook.setImage(file.getBytes());
            } catch (Exception e) {
                throw new RuntimeException("Error while uploading image: " + e.getMessage());
            }
        }
        Validator.validateObject(foundBook);
        return bookRepository.save(foundBook);
    }

    public void deleteBook(int id) {
        User requester = authenticationService.getCurrentUser();
        if (requester.getRole().getName().equals("client")) {
            throw new AccessDeniedException("Books can only be deleted by staff members!");
        }
        Book foundBook = bookRepository.findById(id).orElseThrow(() -> new NotFoundException("Book with id " + id + " does not exist!"));
        if (foundBook.getRentals() != null && !foundBook.getRentals().isEmpty()) {
                for (Rental rental : foundBook.getRentals()) {
                    rental.setBook(null);
                    rental.setPendingRequest(false);
                    rentalRepository.save(rental);
                }
        }
        bookRepository.delete(foundBook);
    }

    public List<Book> getBooksByCategory(int categoryId) {
        return bookRepository.findAllByCategoryId(categoryId);
    }

    public List<Book> filterByTitle(String title) {
        return bookRepository.filterByTitle(title);
    }

    public List<Book> filterByAuthor(String author) {
        return bookRepository.filterByAuthor(author);
    }

    public Page<Book> findPaginated(Pageable pageable) {
        return bookRepository.findAll(pageable);
    }

    public Page<Book> getAllDistinctBooks(Pageable pageable) {
        return bookRepository.findAllByTitleAndAuthorAndPublishingHouse(pageable);
    }


    public Page<Book> getBooksByCategoryId(Integer categoryId, Pageable pageable) {
        return bookRepository.findAllByCategoryId(categoryId, pageable);
    }

    public Page<Book> getAllBooksByFilters(Optional<String> keyword, Optional<Integer> categoryId, Optional<Boolean> status, Pageable pageable) {
        if (keyword.isPresent() && categoryId.isPresent() && status.isPresent() && !keyword.get().isEmpty() && categoryId.get() != 0) {
            return bookRepository.findAllByKeywordAndStatusAndCategoryId(keyword.get(), status.get(), categoryId.get(), pageable);
        } else if (keyword.isPresent() && categoryId.isPresent() && !keyword.get().isEmpty() && categoryId.get() != 0) {
            return bookRepository.findAllByKeywordAndCategoryId(keyword.get(), categoryId.get(), pageable);
        } else if (keyword.isPresent() && status.isPresent() && !keyword.get().isEmpty()) {
            return bookRepository.findAllByKeywordAndStatus(keyword.get(), status.get(), pageable);
        } else if (categoryId.isPresent() && status.isPresent() && categoryId.get() != 0) {
            return bookRepository.findAllByStatusAndCategoryId(status.get(), categoryId.get(), pageable);
        } else if (keyword.isPresent() && !keyword.get().isEmpty()) {
            return bookRepository.findAllByKeyword(keyword.get(), pageable);
        } else if (categoryId.isPresent() && categoryId.get() != 0) {
            return bookRepository.findAllByCategoryId(categoryId.get(), pageable);
        } else if (status.isPresent()) {
            return bookRepository.findAllByStatus(status.get(), pageable);
        } else {
            return bookRepository.findAll(pageable);
        }
    }

    public Page<Book> getAllDistinctBooksByFilters(Optional<String> keyword, Optional<Integer> categoryId, Pageable pageable) {
        if (keyword.isPresent() && categoryId.isPresent() && !keyword.get().isEmpty() && categoryId.get() != 0) {
            return bookRepository.findAllDistinctByCategoryIdAndKeyword(categoryId.get(), keyword.get(), pageable);
        } else if (keyword.isPresent() && !keyword.get().isEmpty()) {
            return bookRepository.findAllDistinctByKeyword(keyword.get(), pageable);
        } else if (categoryId.isPresent() && categoryId.get() != 0) {
            return bookRepository.findAllDistinctByCategoryId(categoryId.get(), pageable);
        } else {
            return bookRepository.findAllByTitleAndAuthorAndPublishingHouse(pageable);
        }
    }

    public void deleteAllBooks() {
        for (var book: bookRepository.findAll()) {
            deleteBook(book.getId());
        }
    }
}
