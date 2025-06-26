package org.booksrental.bookservice.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.booksrental.bookservice.config.AuthorityCheck;
import org.booksrental.bookservice.model.dto.*;
import org.booksrental.bookservice.model.entity.Category;
import org.booksrental.bookservice.model.entity.Book;
import org.booksrental.bookservice.model.mapper.BookMapper;
import org.booksrental.bookservice.service.BookService;
import org.booksrental.bookservice.service.CategoryService;
import org.booksrental.bookservice.service.RentalService;
import org.booksrental.bookservice.validator.Validator;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/books")
@Slf4j
public class BookController {
    private final BookService bookService;
    private final CategoryService categoryService;
    private final BookMapper bookMapper;
    private final AuthorityCheck authorityCheck;
    private final RentalService rentalService;

    public BookController(BookService bookService, CategoryService categoryService, BookMapper bookMapper, AuthorityCheck authorityCheck, RentalService rentalService) {
        this.bookService = bookService;
        this.categoryService = categoryService;
        this.bookMapper = bookMapper;
        this.authorityCheck = authorityCheck;
        this.rentalService = rentalService;
    }


    // ADAUGARE
    @PostMapping("/form")
    public ResponseEntity<?> addBook(@RequestBody CreateBookDTO bookDTO, @RequestParam(required = false) Integer bookId, HttpServletRequest request) {
        List<Category> categories = bookDTO.getCategoryIds() != null ? categoryService.findAllById(bookDTO.getCategoryIds()) : new ArrayList<>();
        Book book = bookMapper.toBook(bookDTO);
        Book newBook = null;
        if (bookId != null) {
            book.setId(bookId);
            newBook = bookService.updateBook(book, categories, getToken(request));
        }
        else {
            Validator.validateObject(bookDTO);
            newBook = bookService.addBook(book, categories, getToken(request));
        }
        UpdateBookDTO updateBookDTO = bookMapper.toUpdateBookDto(newBook);
        updateBookDTO.setCategoryIds(newBook.getCategories().stream().map(Category::getId).toList());
        return ResponseEntity.ok(updateBookDTO);
    }

    // ȘTERGERE
    @RequestMapping("/delete/{id}")
    public ResponseEntity<String> deleteBook(@PathVariable int id, HttpServletRequest request) {
        bookService.deleteBook(id, getToken(request));
        return ResponseEntity.ok("Book with id " + id + " was deleted successfully!");
    }

//    @RequestMapping("/rent/{id}")
//    public String rentBook(@PathVariable int id, Model model) {
//        Book book = bookService.getBookById(id);
//        model.addAttribute("book", book);
//        CreateRentalDto rental = CreateRentalDto.builder().bookId(id).build();
//        model.addAttribute("rental", rental);
//        model.addAttribute("currentUserRole", authenticationService.getCurrentUser() != null ? authenticationService.getCurrentUser().getRole().getName() : null);
//        model.addAttribute("authenticated", authenticationService.checkIfAuthenticated());
//        return "rentals/rentalForm"; // templates/books/rentBook.html
//    }

    @PostMapping("/rent/{id}")
    public ResponseEntity<?> rentBook(@PathVariable Integer id,
                                  @RequestBody @Valid RentalDTO rentalDto,
                                  HttpServletRequest request) {
        rentalDto.setBookId(id);
        CurrentUserDTO currentUser = authorityCheck.getCurrentUser(getToken(request));
        if (currentUser != null) {
            try {
                return ResponseEntity.ok().body(rentalService.addRental(rentalDto, getToken(request)));
            } catch (Exception e) {
                log.error("Error while renting book: {}", e.getMessage());
                return ResponseEntity.badRequest().body("Error while renting book: " + e.getMessage());
            }
        } else {
            log.warn("User is not authenticated");
            return ResponseEntity.status(401).body("User is not authenticated");
        }
    }


    @RequestMapping("")
    public ResponseEntity<List<UpdateBookDTO>> getBookPage(
                              @RequestParam(value = "categoryId", required = false) Optional<Integer> categoryId,
                              @RequestParam(value = "search", required = false) Optional<String> search,
                              @RequestParam(value = "available", required = false) Optional<Boolean> available,
                              HttpServletRequest request) {
       List<Book> books = null;
        String header = request.getHeader("Authorization");
        if (header == null) {
            books = bookService.getAllDistinctBooksByFilters(search, categoryId);
        }
        else {
            String token = header.substring(7);
            CurrentUserDTO currentUser = authorityCheck.getCurrentUser(token);
            if (currentUser == null || currentUser.getRole().equals("client")) {
                books = bookService.getAllDistinctBooksByFilters(search, categoryId);
            }
            else {
                books = bookService.getAllBooksByFilters(search, categoryId, available);
            }
        }
        List<UpdateBookDTO> bookDTOs = books.stream()
                .map(bookMapper::toUpdateBookDto)
                .toList();
        bookDTOs.forEach(dto -> {
            List<Integer> dtoCategories = bookService.getBookById(dto.getId()).getCategories().stream()
                    .map(Category::getId)
                    .toList();
            dto.setCategoryIds(dtoCategories);
        });
        return ResponseEntity.ok(bookDTOs);
    }


    @RequestMapping("/{id}")
    public ResponseEntity<UpdateBookDTO> getBook(@PathVariable int id) {
        Book book = bookService.getBookById(id);
        UpdateBookDTO bookDTO = bookMapper.toUpdateBookDto(book);
        bookDTO.setCategoryIds(book.getCategories().stream()
                .map(Category::getId)
                .toList());
        return ResponseEntity.ok(bookDTO);
    }

    String getToken(HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            return token.substring(7);
        }
        return null;
    }
}

