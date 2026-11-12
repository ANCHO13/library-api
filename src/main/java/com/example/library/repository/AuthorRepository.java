package com.example.library.repository;

import com.example.library.dto.AuthorBookCount;
import com.example.library.entity.Author;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AuthorRepository extends JpaRepository<Author, Long> {

    // Method-name query: exact (case-insensitive) match on both names.
    List<Author> findByFirstNameIgnoreCaseAndLastNameIgnoreCase(String firstName, String lastName);

    boolean existsByEmailIgnoreCase(String email);

    // Custom query: how many books each author has, most productive first.
    @Query("""
            select a.id as authorId,
                   concat(a.firstName, ' ', a.lastName) as authorName,
                   count(b) as bookCount
            from Author a
            left join a.books b
            group by a.id, a.firstName, a.lastName
            order by count(b) desc
            """)
    List<AuthorBookCount> countBooksPerAuthor();
}
