package org.booksrental.bookservice.service;

import org.booksrental.bookservice.config.AuthorityCheck;
import org.booksrental.bookservice.exception.AccessDeniedException;
import org.booksrental.bookservice.exception.NotFoundException;
import org.booksrental.bookservice.model.dto.CurrentUserDTO;
import org.booksrental.bookservice.model.entity.Book;
import org.booksrental.bookservice.model.entity.Category;
import org.booksrental.bookservice.repository.BookRepository;
import org.booksrental.bookservice.repository.CategoryRepository;
import org.booksrental.bookservice.validator.Validator;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class BookService {
    private final BookRepository bookRepository;
    private final CategoryRepository categoryRepository;
    private final AuthorityCheck authorityCheck;
    private final RentalService rentalService;


    public BookService(BookRepository bookRepository, CategoryRepository categoryRepository, AuthorityCheck authorityCheck, RentalService rentalService) {
        this.bookRepository = bookRepository;
        this.categoryRepository = categoryRepository;
        this.authorityCheck = authorityCheck;
        this.rentalService = rentalService;
    }

    public Book addBook(Book book, List<Category> categories, String token) {
        CurrentUserDTO requester = authorityCheck.getCurrentUser(token);
        book.setCategories(categories);
        book.setStatus(true);
        if (requester.getRole().equals("client")) {
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

    public Book updateBook(Book book, List<Category> categories, String token) {
        CurrentUserDTO requester = authorityCheck.getCurrentUser(token);
        if (requester.getRole().equals("client")) {
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
        if (categories != null) {
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
        Validator.validateObject(foundBook);
        return bookRepository.save(foundBook);
    }

    public void deleteBook(int id, String token) {
        CurrentUserDTO requester = authorityCheck.getCurrentUser(token);
        if (requester.getRole().equals("client")) {
            throw new AccessDeniedException("Books can only be deleted by staff members!");
        }
        Book foundBook = bookRepository.findById(id).orElseThrow(() -> new NotFoundException("Book with id " + id + " does not exist!"));
        rentalService.updateRentals(foundBook.getId(), token);
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

    public List<Book> findAllBooks() {
        return bookRepository.findAll();
    }

    public List<Book> getAllDistinctBooks() {
        return bookRepository.findAllDistinct();
    }


    public List<Book> getBooksByCategoryId(Integer categoryId) {
        return bookRepository.findAllByCategoryId(categoryId);
    }

    public List<Book> getAllBooksByFilters(Optional<String> keyword, Optional<Integer> categoryId, Optional<Boolean> status) {
        if (keyword.isPresent() && categoryId.isPresent() && status.isPresent() && !keyword.get().isEmpty() && categoryId.get() != 0) {
            return bookRepository.findAllByKeywordAndStatusAndCategoryId(keyword.get(), status.get(), categoryId.get());
        } else if (keyword.isPresent() && categoryId.isPresent() && !keyword.get().isEmpty() && categoryId.get() != 0) {
            return bookRepository.findAllByKeywordAndCategoryId(keyword.get(), categoryId.get());
        } else if (keyword.isPresent() && status.isPresent() && !keyword.get().isEmpty()) {
            return bookRepository.findAllByKeywordAndStatus(keyword.get(), status.get());
        } else if (categoryId.isPresent() && status.isPresent() && categoryId.get() != 0) {
            return bookRepository.findAllByStatusAndCategoryId(status.get(), categoryId.get());
        } else if (keyword.isPresent() && !keyword.get().isEmpty()) {
            return bookRepository.findAllByKeyword(keyword.get());
        } else if (categoryId.isPresent() && categoryId.get() != 0) {
            return bookRepository.findAllByCategoryId(categoryId.get());
        } else if (status.isPresent()) {
            return bookRepository.findAllByStatus(status.get());
        } else {
            return bookRepository.findAll();
        }
    }

    public List<Book> getAllDistinctBooksByFilters(Optional<String> keyword, Optional<Integer> categoryId) {
        if (keyword.isPresent() && categoryId.isPresent() && !keyword.get().isEmpty() && categoryId.get() != 0) {
            return bookRepository.findAllDistinctByCategoryIdAndKeyword(categoryId.get(), keyword.get());
        } else if (keyword.isPresent() && !keyword.get().isEmpty()) {
            return bookRepository.findAllDistinctByKeyword(keyword.get());
        } else if (categoryId.isPresent() && categoryId.get() != 0) {
            return bookRepository.findAllDistinctByCategoryId(categoryId.get());
        } else {
            return bookRepository.findAllDistinct();
        }
    }

    public void deleteAllBooks(String token) {
        for (var book: bookRepository.findAll()) {
            deleteBook(book.getId(), token);
        }
    }
}
