package com.example.library.controller;

import com.example.library.dto.BookRequest;
import com.example.library.dto.BookResponse;
import com.example.library.dto.PageResponse;
import com.example.library.service.BookService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
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
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/books")
public class BookController {

    private final BookService bookService;

    public BookController(BookService bookService) {
        this.bookService = bookService;
    }

    // GET /api/books?page=0&size=5&sortBy=price&direction=desc&title=farm&minYear=1900
    @GetMapping
    public PageResponse<BookResponse> list(@RequestParam(defaultValue = "") String title,
                                           @RequestParam(defaultValue = "0") int minYear,
                                           @RequestParam(defaultValue = "0") int page,
                                           @RequestParam(defaultValue = "10") int size,
                                           @RequestParam(defaultValue = "title") String sortBy,
                                           @RequestParam(defaultValue = "asc") String direction) {
        return bookService.findAll(title, minYear, page, size, sortBy, direction);
    }

    // GET /api/books/search?category=classic&minPrice=5&maxPrice=10&page=0&size=5
    @GetMapping("/search")
    public PageResponse<BookResponse> search(@RequestParam String category,
                                             @RequestParam(defaultValue = "0") BigDecimal minPrice,
                                             @RequestParam(defaultValue = "1000000") BigDecimal maxPrice,
                                             @RequestParam(defaultValue = "0") int page,
                                             @RequestParam(defaultValue = "10") int size,
                                             @RequestParam(defaultValue = "title") String sortBy,
                                             @RequestParam(defaultValue = "asc") String direction) {
        return bookService.searchByCategoryAndPrice(category, minPrice, maxPrice, page, size, sortBy, direction);
    }

    // GET /api/books/without-cover -> books that have no uploaded cover yet
    @GetMapping("/without-cover")
    public List<BookResponse> withoutCover() {
        return bookService.findWithoutCover();
    }

    @GetMapping("/{id}")
    public BookResponse getById(@PathVariable Long id) {
        return bookService.findById(id);
    }

    @PostMapping
    public ResponseEntity<BookResponse> create(@Valid @RequestBody BookRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(bookService.create(request));
    }

    @PutMapping("/{id}")
    public BookResponse update(@PathVariable Long id, @Valid @RequestBody BookRequest request) {
        return bookService.update(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        bookService.delete(id);
        return ResponseEntity.noContent().build();
    }

    // POST /api/books/1/cover  (multipart/form-data, field name "file")
    @PostMapping(value = "/{id}/cover", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public BookResponse uploadCover(@PathVariable Long id, @RequestParam("file") MultipartFile file) {
        return bookService.uploadCover(id, file);
    }
}
