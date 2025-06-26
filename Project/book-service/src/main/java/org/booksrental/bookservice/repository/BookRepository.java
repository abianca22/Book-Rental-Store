package org.booksrental.bookservice.repository;

import org.booksrental.bookservice.model.entity.Book;
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
    List<Book> findAllDistinct();
    @Query("""
SELECT b FROM Book b JOIN b.categories c
WHERE :categoryId = c.id
""")
    List<Book> findAllByCategoryId(Integer categoryId);
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
    List<Book> findAllDistinctByKeyword(String keyword);
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
    List<Book> findAllDistinctByCategoryId(Integer categoryId);
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
    List<Book> findAllDistinctByCategoryIdAndKeyword(Integer categoryId, String keyword);

    List<Book> findAllByStatus(Boolean status);

    @Query("""
SELECT b FROM Book b JOIN b.categories c
    WHERE (:categoryId = c.id) AND b.status = :status
    """)
    List<Book> findAllByStatusAndCategoryId(Boolean status, Integer categoryId);

    @Query("""
SELECT b FROM Book b
    WHERE LOWER(b.description) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(b.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(b.author) LIKE LOWER(CONCAT('%', :keyword, '%'))
""")
    List<Book> findAllByKeyword(String keyword);


    @Query("""
SELECT b FROM Book b
    WHERE (LOWER(b.description) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(b.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(b.author) LIKE LOWER(CONCAT('%', :keyword, '%'))) AND b.status = :status
""")
    List<Book> findAllByKeywordAndStatus(String keyword, Boolean status);

    @Query("""
SELECT b FROM Book b JOIN b.categories c
    WHERE (LOWER(b.description) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(b.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(b.author) LIKE LOWER(CONCAT('%', :keyword, '%'))) AND (:categoryId = c.id)
""")
    List<Book> findAllByKeywordAndCategoryId(String keyword, Integer categoryId);

    @Query("""
SELECT b FROM Book b JOIN b.categories c
    WHERE (LOWER(b.description) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(b.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(b.author) LIKE LOWER(CONCAT('%', :keyword, '%'))) AND (c.id = :categoryId) AND b.status = :status
""")
    List<Book> findAllByKeywordAndStatusAndCategoryId(String keyword, Boolean status, Integer categoryId);


}

