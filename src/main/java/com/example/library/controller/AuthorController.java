package com.example.library.controller;

import com.example.library.dto.AuthorBookCount;
import com.example.library.dto.AuthorRequest;
import com.example.library.dto.AuthorResponse;
import com.example.library.dto.BookResponse;
import com.example.library.service.AuthorService;
import com.example.library.service.BookService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/authors")
public class AuthorController {

    private final AuthorService authorService;
    private final BookService bookService;

    public AuthorController(AuthorService authorService, BookService bookService) {
        this.authorService = authorService;
        this.bookService = bookService;
    }

    // GET /api/authors                               -> all authors
    // GET /api/authors?firstName=jane&lastName=austen -> search by full name
    @GetMapping
    public List<AuthorResponse> list(@RequestParam(required = false) String firstName,
                                     @RequestParam(required = false) String lastName) {
        if (firstName != null && lastName != null) {
            return authorService.searchByName(firstName, lastName);
        }
        return authorService.findAll();
    }

    @GetMapping("/{id}")
    public AuthorResponse getById(@PathVariable Long id) {
        return authorService.findById(id);
    }

    // GET /api/authors/1/books?maxPrice=10 -> this author's books up to a price
    @GetMapping("/{id}/books")
    public List<BookResponse> booksOfAuthor(@PathVariable Long id,
                                            @RequestParam(defaultValue = "1000000") BigDecimal maxPrice) {
        return bookService.findByAuthorAndMaxPrice(id, maxPrice);
    }

    // GET /api/authors/stats/book-counts -> number of books per author
    @GetMapping("/stats/book-counts")
    public List<AuthorBookCount> bookCounts() {
        return authorService.bookCounts();
    }

    @PostMapping
    public ResponseEntity<AuthorResponse> create(@Valid @RequestBody AuthorRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authorService.create(request));
    }

    @PutMapping("/{id}")
    public AuthorResponse update(@PathVariable Long id, @Valid @RequestBody AuthorRequest request) {
        return authorService.update(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        authorService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
