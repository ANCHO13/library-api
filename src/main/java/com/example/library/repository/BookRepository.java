package com.example.library.repository;

import com.example.library.entity.Book;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface BookRepository extends JpaRepository<Book, Long> {

    // Method-name query #1: title search + minimum year, paginated and sorted.
    Page<Book> findByTitleContainingIgnoreCaseAndPublicationYearGreaterThanEqual(
            String title, Integer publicationYear, Pageable pageable);

    // Method-name query #2: books of one author up to a maximum price.
    List<Book> findByAuthorIdAndPriceLessThanEqual(Long authorId, BigDecimal maxPrice);

    boolean existsByIsbn(String isbn);

    // Custom query #1: books in a category within a price range, paginated.
    @Query(value = """
            select distinct b from Book b
            join b.categories c
            where lower(c.name) = lower(:category)
              and b.price between :minPrice and :maxPrice
            """,
            countQuery = """
            select count(distinct b) from Book b
            join b.categories c
            where lower(c.name) = lower(:category)
              and b.price between :minPrice and :maxPrice
            """)
    Page<Book> findByCategoryAndPriceRange(@Param("category") String category,
                                           @Param("minPrice") BigDecimal minPrice,
                                           @Param("maxPrice") BigDecimal maxPrice,
                                           Pageable pageable);

    // Custom query #2: books that still have no uploaded cover.
    @Query("select b from Book b join fetch b.author where b.coverImagePath is null order by b.title")
    List<Book> findBooksWithoutCover();
}
