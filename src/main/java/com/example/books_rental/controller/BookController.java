package com.example.books_rental.controller;

import com.example.books_rental.dto.CreateRentalDto;
import com.example.books_rental.dto.UpdateBookDto;
import com.example.books_rental.mapper.BookMapper;
import com.example.books_rental.mapper.RentalMapper;
import com.example.books_rental.model.entities.Book;
import com.example.books_rental.model.entities.Category;
import com.example.books_rental.model.entities.Rental;
import com.example.books_rental.model.entities.User;
import com.example.books_rental.service.*;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/books")
@Slf4j
public class BookController {

    private final BooksManagementService bookService;
    private final CategoriesManagementService categoryService;
    private final UsersManagementService userService;
    private final BookMapper bookMapper;
    private final AuthenticationService authenticationService;
    private final RentalsManagementService rentalService;
    private final RentalMapper rentalMapper;

    public BookController(BooksManagementService bookService, CategoriesManagementService categoryService, UsersManagementService userService, BookMapper bookMapper, AuthenticationService authenticationService, RentalsManagementService rentalService, RentalMapper rentalMapper) {
        this.bookService = bookService;
        this.categoryService = categoryService;
        this.userService = userService;
        this.bookMapper = bookMapper;
        this.authenticationService = authenticationService;
        this.rentalService = rentalService;
        this.rentalMapper = rentalMapper;
    }

    // LISTA CARTI
    @GetMapping("/all")
    public String listBooks(Model model) {
        model.addAttribute("books", bookService.getAllBooks());
        log.info("Books: {}", bookService.getAllBooks());
        return "books/booksList";
    }

    // FORMULAR ADAUGARE
    @GetMapping("/form")
    public String addForm(Model model) {
        model.addAttribute("book", new UpdateBookDto());
        model.addAttribute("categories", categoryService.getAllCategories());
        model.addAttribute("currentUserRole", authenticationService.getCurrentUser().getRole().getName());
        model.addAttribute("authenticated", authenticationService.checkIfAuthenticated());
        return "books/booksForm";
    }

    // ADAUGARE
    @PostMapping("/form")
    public String addBook(@ModelAttribute("book") @Valid UpdateBookDto bookDto,
                          BindingResult result,
                          Model model,
                          @RequestParam("imagefile") MultipartFile file) {
        if (result.hasErrors()) {
            List<String> globalErrors = result.getAllErrors().stream()
                    .filter(error -> !(error instanceof FieldError))
                    .map(ObjectError::getDefaultMessage)
                    .toList();
            List<String> fieldErrors = result.getFieldErrors().stream()
                    .map(FieldError::getDefaultMessage)
                    .toList();
            logErrors(globalErrors, fieldErrors);
            model.addAttribute("categories", categoryService.getAllCategories());
            model.addAttribute("book", bookDto);
            model.addAttribute("error", globalErrors.isEmpty() ? null : globalErrors);
            model.addAttribute("currentUserRole", authenticationService.getCurrentUser().getRole().getName());
            model.addAttribute("authenticated", authenticationService.checkIfAuthenticated());
            log.warn("Book could not be added");
            return "books/booksForm"; // templates/books/create.html
        }
        List<Category> categories = bookDto.getCategoryIds() != null ? categoryService.findAllById(bookDto.getCategoryIds()) : new ArrayList<>();
        Book book = bookMapper.toBook(bookDto);
        if (bookDto.getId() == null) {
            bookService.addBook(book, categories, file);
            log.info("Book added successfully: {}", book);
        }
        else {
            bookService.updateBook(book, categories, file);
            log.info("Book updated successfully: {}", book);
        }
        return "redirect:/books";
    }

    // EDITARE
    @GetMapping("/edit/{id}")
    public String editForm(@PathVariable int id, Model model) {
        Book book = bookService.getBookById(id);
        UpdateBookDto bookDto = bookMapper.toUpdateBookDto(book);
        bookDto.setCategoryIds(book.getCategories().stream().map(Category::getId).toList());
        model.addAttribute("book", bookDto);
        model.addAttribute("categories", categoryService.getAllCategories());
        model.addAttribute("currentUserRole", authenticationService.getCurrentUser().getRole().getName());
        model.addAttribute("authenticated", authenticationService.checkIfAuthenticated());
        return "books/booksForm";
    }

    @PostMapping("/edit/{id}")
    public String updateBook(@PathVariable Integer id,
                             @ModelAttribute("book") @Valid UpdateBookDto bookDto,
                             BindingResult result,
                             Model model,
                             @RequestParam("imagefile") MultipartFile file) {
        if (result.hasErrors()) {
            List<String> globalErrors = result.getAllErrors().stream()
                    .filter(error -> !(error instanceof FieldError))
                    .map(ObjectError::getDefaultMessage)
                    .toList();
            List<String> fieldErrors = result.getFieldErrors().stream()
                    .map(FieldError::getDefaultMessage)
                    .toList();
            logErrors(globalErrors, fieldErrors);
            model.addAttribute("categories", categoryService.getAllCategories());
            model.addAttribute("currentUserRole", authenticationService.getCurrentUser().getRole().getName());
            model.addAttribute("authenticated", authenticationService.checkIfAuthenticated());
            model.addAttribute("book", bookDto);
            model.addAttribute("error", globalErrors.isEmpty() ? null : globalErrors);
            log.warn("Book could not be updated");
            return "books/booksForm";
        }
        Book book = bookMapper.toBook(bookDto);
        book.setCategories(categoryService.findAllById(bookDto.getCategoryIds()));
        List<Category> categories = bookDto.getCategoryIds() != null && !bookDto.getCategoryIds().isEmpty() ? categoryService.findAllById(bookDto.getCategoryIds()) : new ArrayList<>();
        bookService.updateBook(book, categories, file);
        log.info("Book updated successfully: {}", book);
        return "redirect:/books";
    }

    // ȘTERGERE
    @RequestMapping("/delete/{id}")
    public String deleteBook(@PathVariable int id) {
        try {
            bookService.deleteBook(id);
        }
        catch (Exception e) {
            log.error("Error deleting book with id {}: {}", id, e.getMessage());
            return "redirect:/books?error=" + e.getMessage();
        }
        return "redirect:/books";
    }

    @RequestMapping("/rent/{id}")
    public String rentBook(@PathVariable int id, Model model) {
        Book book = bookService.getBookById(id);
        model.addAttribute("book", book);
        CreateRentalDto rental = CreateRentalDto.builder().bookId(id).build();
        model.addAttribute("rental", rental);
        model.addAttribute("currentUserRole", authenticationService.getCurrentUser() != null ? authenticationService.getCurrentUser().getRole().getName() : null);
        model.addAttribute("authenticated", authenticationService.checkIfAuthenticated());
        return "rentals/rentalForm"; // templates/books/rentBook.html
    }

    @PostMapping("/rent/{id}")
    public String rentBook(@PathVariable Integer id,
                           @ModelAttribute("rental") @Valid CreateRentalDto rentalDto,
                           BindingResult result,
                           Model model,
                           @RequestParam("email") String email) {
        Book book = bookService.getBookById(id);
        if (result.hasErrors()) {
            model.addAttribute("book", book);
            List<String> errors = result.getAllErrors().stream()
                    .filter(error -> !(error instanceof FieldError))
                    .map(ObjectError::getDefaultMessage)
                    .toList();
            List<String> fieldErrors = result.getFieldErrors().stream()
                    .map(FieldError::getDefaultMessage)
                    .toList();
            logErrors(errors, fieldErrors);
            model.addAttribute("error", result.getAllErrors());
            model.addAttribute("currentUserRole", authenticationService.getCurrentUser() != null ? authenticationService.getCurrentUser().getRole().getName() : null);
            model.addAttribute("authenticated", authenticationService.checkIfAuthenticated());
            log.warn("Book could not be rented");
            return "rentals/rentalForm"; // templates/books/rentBook.html
        }
        if (authenticationService.checkIfAuthenticated()) {
            User employee = authenticationService.getCurrentUser();
            Rental rental = rentalMapper.toRental(rentalDto);
            try {
                rentalService.addRental(rental, book, employee, userService.getUserByEmail(email));
            }
            catch (Exception e) {
                model.addAttribute("error", e.getMessage());
                model.addAttribute("book", book);
                model.addAttribute("currentUserRole", authenticationService.getCurrentUser() != null ? authenticationService.getCurrentUser().getRole().getName() : null);
                model.addAttribute("authenticated", authenticationService.checkIfAuthenticated());
                log.error("Error renting book: {}", e.getMessage());
                return "rentals/rentalForm";
            }
        } else {
            model.addAttribute("error", "Trebuie sa fii logat pentru a inregistra un imprumut!");
            log.warn("User is not authenticated");
            return "redirect:/rent/" + id;
        }
        log.info("Book rented successfully: {}", book);
        return "redirect:/books";
    }

    @GetMapping("/getimage/{id}")
    public void downloadImage(@PathVariable String id, HttpServletResponse response) throws IOException {
        Book book = bookService.getBookById(Integer.parseInt(id));

            if (book.getImage() != null) {
                byte[] byteArray = new byte[book.getImage().length];
                int i = 0;
                for (Byte wrappedByte : book.getImage()) {
                    byteArray[i++] = wrappedByte;
                }
                response.setContentType("image/jpeg");
                InputStream is = new ByteArrayInputStream(byteArray);
                try {
                    IOUtils.copy(is, response.getOutputStream());
                } catch (IOException e) {
                    log.error("Error while writing image to response: {}", e.getMessage());
                }
            }
    }

    @RequestMapping("")
    public String getBookPage(Model model,
                              @RequestParam("page") Optional<Integer> page,
                              @RequestParam("size") Optional<Integer> size,
                              @RequestParam("sortField") Optional<String> sortField,
                              @RequestParam("sortDir") Optional<String> sortDir,
                              @RequestParam(value = "categoryId", required = false) Optional<Integer> categoryId,
                              @RequestParam(value = "search", required = false) Optional<String> search,
                              @RequestParam(value = "available", required = false) Optional<Boolean> available,
                              @RequestParam(value = "error", required = false) Optional<String> error) {

        int currentPage = page.orElse(1);
        int pageSize = size.orElse(10);
        String currentSortField = sortField.orElse("title");
        String currentSortDir = sortDir.orElse("asc");

        Sort sort = Sort.by(Sort.Direction.fromString(currentSortDir), currentSortField);
        PageRequest pageRequest = PageRequest.of(currentPage - 1, pageSize, sort);
        Page<Book> bookPage;

        User user = authenticationService.getCurrentUser();

        if (user == null || user.getRole().getName().equals("client")) {
            bookPage = bookService.getAllDistinctBooksByFilters(search, categoryId, pageRequest);
        }
        else {
            bookPage = bookService.getAllBooksByFilters(search, categoryId, available, pageRequest);
        }

        model.addAttribute("filteredCategoryId", categoryId.orElse(null));
        model.addAttribute("search", search.orElse(null));
        model.addAttribute("available", available.orElse(null));
        model.addAttribute("bookPage", bookPage);
        model.addAttribute("currentPage", currentPage);
        model.addAttribute("size", pageSize);
        model.addAttribute("sortField", currentSortField);
        model.addAttribute("sortDir", currentSortDir);
        model.addAttribute("reverseSortDir", currentSortDir.equals("asc") ? "desc" : "asc");
        model.addAttribute("currentUserRole", user != null ? user.getRole().getName() : null);
        model.addAttribute("authenticated", authenticationService.checkIfAuthenticated());
        model.addAttribute("categories", categoryService.getAllCategories());
        model.addAttribute("error", error.orElse(null));
        return "books/pageableBooks";
    }


    @RequestMapping("/{id}")
    public String getBook(@PathVariable int id, Model model) {
        Book book = bookService.getBookById(id);
        log.info("Book: {}", book);
        model.addAttribute("book", book);
        model.addAttribute("authenticated", authenticationService.checkIfAuthenticated());
        model.addAttribute("currentUserRole", authenticationService.getCurrentUser() != null ? authenticationService.getCurrentUser().getRole().getName() : null);
        return "books/bookDetails"; // templates/books/bookDetails.html
    }

    public void logErrors(List<String> objectErrors, List<String> fieldErrors) {
        if (!objectErrors.isEmpty()) {
            log.error("Errors not related to fields: {}", objectErrors);
        }
        if (!fieldErrors.isEmpty()) {
            log.error("Field errors: {}", fieldErrors);
        }
    }


}
