package com.example.books_rental.repository;

import com.example.books_rental.model.entities.Book;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface BookRepository extends JpaRepository<Book, Integer> {
    @Query("""
    SELECT b FROM Book b WHERE b.title like %:title%
    """)
    List<Book> filterByTitle(String title);
    @Query("""
    SELECT b FROM Book b WHERE b.author like %:author%
    """)
    List<Book> filterByAuthor(String author);
    @Query("""
    SELECT b FROM Book b JOIN b.categories c WHERE :categoryId = c.id
    """)
    List<Book> findAllByCategoryId(Integer categoryId);
    @Query("""
SELECT b FROM Book b
WHERE b.id IN (
    SELECT MAX(b2.id)
    FROM Book b2
    WHERE LENGTH(COALESCE(b2.description, '')) = (
        SELECT MAX(LENGTH(COALESCE(b3.description, '')))
        FROM Book b3
        WHERE b3.author = b2.author
          AND b3.title = b2.title
          AND b3.publishingHouse = b2.publishingHouse
    )
    GROUP BY b2.author, b2.title, b2.publishingHouse
)
       """)
    Page<Book> findAllByTitleAndAuthorAndPublishingHouse(Pageable pageable);
    @Query("""
SELECT b FROM Book b JOIN b.categories c
WHERE :categoryId = c.id
""")
    Page<Book> findAllByCategoryId(Integer categoryId, Pageable pageable);
    @Query("""
SELECT b FROM Book b
WHERE (LOWER(b.description) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(b.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(b.author) LIKE LOWER(CONCAT('%', :keyword, '%'))) AND b.id IN (
    SELECT MAX(b2.id)
    FROM Book b2
    WHERE LENGTH(COALESCE(b2.description, '')) = (
        SELECT MAX(LENGTH(COALESCE(b3.description, '')))
        FROM Book b3
        WHERE b3.author = b2.author
          AND b3.title = b2.title
          AND b3.publishingHouse = b2.publishingHouse
    )
    GROUP BY b2.author, b2.title, b2.publishingHouse
)
       """)
    Page<Book> findAllDistinctByKeyword(String keyword, Pageable pageable);
    @Query("""
SELECT b FROM Book b JOIN b.categories c
WHERE :categoryId = c.id AND b.id IN (
    SELECT MAX(b2.id)
    FROM Book b2
    WHERE LENGTH(COALESCE(b2.description, '')) = (
        SELECT MAX(LENGTH(COALESCE(b3.description, '')))
        FROM Book b3
        WHERE b3.author = b2.author
          AND b3.title = b2.title
          AND b3.publishingHouse = b2.publishingHouse
    )
    GROUP BY b2.author, b2.title, b2.publishingHouse
)
       """)
    Page<Book> findAllDistinctByCategoryId(Integer categoryId, Pageable pageable);
    @Query("""
SELECT b FROM Book b JOIN b.categories c
WHERE :categoryId = c.id AND (LOWER(b.description) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(b.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(b.author) LIKE LOWER(CONCAT('%', :keyword, '%'))) AND b.id IN (
    SELECT MAX(b2.id)
    FROM Book b2
    WHERE LENGTH(COALESCE(b2.description, '')) = (
        SELECT MAX(LENGTH(COALESCE(b3.description, '')))
        FROM Book b3
        WHERE b3.author = b2.author
          AND b3.title = b2.title
          AND b3.publishingHouse = b2.publishingHouse
    )
    GROUP BY b2.author, b2.title, b2.publishingHouse
)
       """)
    Page<Book> findAllDistinctByCategoryIdAndKeyword(Integer categoryId, String keyword, Pageable pageable);

    Page<Book> findAllByStatus(Boolean status, Pageable pageable);

    @Query("""
SELECT b FROM Book b JOIN b.categories c
    WHERE (:categoryId = c.id) AND b.status = :status
    """)
    Page<Book> findAllByStatusAndCategoryId(Boolean status, Integer categoryId, Pageable pageable);

@Query("""
SELECT b FROM Book b
    WHERE LOWER(b.description) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(b.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(b.author) LIKE LOWER(CONCAT('%', :keyword, '%'))
""")
    Page<Book> findAllByKeyword(String keyword, Pageable pageable);


    @Query("""
SELECT b FROM Book b
    WHERE (LOWER(b.description) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(b.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(b.author) LIKE LOWER(CONCAT('%', :keyword, '%'))) AND b.status = :status
""")
    Page<Book> findAllByKeywordAndStatus(String keyword, Boolean status, Pageable pageable);

    @Query("""
SELECT b FROM Book b JOIN b.categories c
    WHERE (LOWER(b.description) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(b.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(b.author) LIKE LOWER(CONCAT('%', :keyword, '%'))) AND (:categoryId = c.id)
""")
    Page<Book> findAllByKeywordAndCategoryId(String keyword, Integer categoryId, Pageable pageable);

    @Query("""
SELECT b FROM Book b JOIN b.categories c
    WHERE (LOWER(b.description) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(b.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(b.author) LIKE LOWER(CONCAT('%', :keyword, '%'))) AND (c.id = :categoryId) AND b.status = :status
""")
    Page<Book> findAllByKeywordAndStatusAndCategoryId(String keyword, Boolean status, Integer categoryId, Pageable pageable);


}
