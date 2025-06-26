package org.booksrental.bookservice.model;



import org.booksrental.bookservice.model.entity.Book;
import org.booksrental.bookservice.model.entity.Category;
import org.booksrental.bookservice.repository.BookRepository;
import org.booksrental.bookservice.repository.CategoryRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.List;


@Component
@Profile("dev")
public class DataLoader implements CommandLineRunner {

    private final BookRepository bookRepository;
    private final CategoryRepository categoryRepository;

    public DataLoader(BookRepository bookRepository,
                          CategoryRepository categoryRepository) {
        this.bookRepository = bookRepository;
        this.categoryRepository = categoryRepository;
    }

    @Override
    public void run(String... args) {
        if(categoryRepository.findByName("Fantasy").isEmpty()) {
            categoryRepository.save(Category.builder().name("Fantasy").build());
        }
        Category fantasy = categoryRepository.findByName("Fantasy").get();
        if(categoryRepository.findByName("Horror").isEmpty()) {
            categoryRepository.save(Category.builder().name("Horror").description("Books that are intended to scare the reader.").build());
        }
        Category horror = categoryRepository.findByName("Horror").get();

        if (bookRepository.findAll().isEmpty()) {
            Book book = Book.builder()
                    .title("Harry Potter și Piatra Filozofală")
                    .author("J.K. Rowling")
                    .publishingHouse("Bloomsbury")
                    .rentalPrice(12.5f)
                    .extensionPrice(2.5f)
                    .description("Prima carte din seria Harry Potter.")
                    .status(false)
                    .categories(List.of(fantasy))
                    .build();

            bookRepository.save(book);
            Book book2 = Book.builder()
                    .title("Harry Potter și Camera Secretelor")
                    .author("J.K. Rowling")
                    .publishingHouse("Bloomsbury")
                    .rentalPrice(12.5f)
                    .extensionPrice(2.5f)
                    .description("A doua carte din seria Harry Potter.")
                    .status(true)
                    .categories(List.of(fantasy))
                    .build();
            bookRepository.save(book2);
            Book book3 = Book.builder()
                    .title("Harry Potter și Camera Secretelor")
                    .author("J.K. Rowling")
                    .publishingHouse("Bloomsbury")
                    .rentalPrice(12.5f)
                    .extensionPrice(2.5f)
                    .description("A doua carte din seria Harry Potter.")
                    .status(false)
                    .categories(List.of(fantasy))
                    .build();
            bookRepository.save(book3);
        }
    }
}
