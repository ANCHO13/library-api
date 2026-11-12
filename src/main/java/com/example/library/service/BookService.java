package com.example.library.service;

import com.example.library.dto.BookRequest;
import com.example.library.dto.BookResponse;
import com.example.library.dto.PageResponse;
import com.example.library.entity.Author;
import com.example.library.entity.Book;
import com.example.library.entity.Category;
import com.example.library.exception.DuplicateResourceException;
import com.example.library.exception.ResourceNotFoundException;
import com.example.library.repository.AuthorRepository;
import com.example.library.repository.BookRepository;
import com.example.library.repository.CategoryRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class BookService {

    private static final Set<String> SORTABLE_FIELDS = Set.of("id", "title", "price", "publicationYear", "isbn");
    private static final int MAX_PAGE_SIZE = 100;

    private final BookRepository bookRepository;
    private final AuthorRepository authorRepository;
    private final CategoryRepository categoryRepository;
    private final FileStorageService fileStorageService;

    public BookService(BookRepository bookRepository,
                       AuthorRepository authorRepository,
                       CategoryRepository categoryRepository,
                       FileStorageService fileStorageService) {
        this.bookRepository = bookRepository;
        this.authorRepository = authorRepository;
        this.categoryRepository = categoryRepository;
        this.fileStorageService = fileStorageService;
    }

    /**
     * Paginated, sorted list with an optional title filter and minimum year.
     */
    @Transactional(readOnly = true)
    public PageResponse<BookResponse> findAll(String title, int minYear,
                                              int page, int size, String sortBy, String direction) {
        Pageable pageable = buildPageable(page, size, sortBy, direction);
        Page<BookResponse> result = bookRepository
                .findByTitleContainingIgnoreCaseAndPublicationYearGreaterThanEqual(title, minYear, pageable)
                .map(this::toResponse);
        return PageResponse.from(result);
    }

    /**
     * Paginated, sorted list of the books in one category within a price range.
     */
    @Transactional(readOnly = true)
    public PageResponse<BookResponse> searchByCategoryAndPrice(String category, BigDecimal minPrice, BigDecimal maxPrice,
                                                               int page, int size, String sortBy, String direction) {
        if (minPrice.compareTo(maxPrice) > 0) {
            throw new IllegalArgumentException("minPrice must not be greater than maxPrice");
        }
        Pageable pageable = buildPageable(page, size, sortBy, direction);
        Page<BookResponse> result = bookRepository
                .findByCategoryAndPriceRange(category, minPrice, maxPrice, pageable)
                .map(this::toResponse);
        return PageResponse.from(result);
    }

    @Transactional(readOnly = true)
    public List<BookResponse> findByAuthorAndMaxPrice(Long authorId, BigDecimal maxPrice) {
        if (!authorRepository.existsById(authorId)) {
            throw new ResourceNotFoundException("Author not found with id " + authorId);
        }
        return bookRepository.findByAuthorIdAndPriceLessThanEqual(authorId, maxPrice).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<BookResponse> findWithoutCover() {
        return bookRepository.findBooksWithoutCover().stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public BookResponse findById(Long id) {
        return toResponse(getBook(id));
    }

    @Transactional
    public BookResponse create(BookRequest request) {
        if (bookRepository.existsByIsbn(request.isbn())) {
            throw new DuplicateResourceException("A book with ISBN " + request.isbn() + " already exists");
        }
        Book book = new Book();
        applyRequest(book, request);
        return toResponse(bookRepository.save(book));
    }

    @Transactional
    public BookResponse update(Long id, BookRequest request) {
        Book book = getBook(id);
        boolean isbnChanged = !book.getIsbn().equals(request.isbn());
        if (isbnChanged && bookRepository.existsByIsbn(request.isbn())) {
            throw new DuplicateResourceException("A book with ISBN " + request.isbn() + " already exists");
        }
        applyRequest(book, request);
        return toResponse(bookRepository.save(book));
    }

    @Transactional
    public void delete(Long id) {
        Book book = getBook(id);
        String coverPath = book.getCoverImagePath();
        bookRepository.delete(book);
        fileStorageService.delete(coverPath);
    }

    /**
     * Stores the uploaded file on disk and records its path on the book.
     */
    @Transactional
    public BookResponse uploadCover(Long id, MultipartFile file) {
        Book book = getBook(id);
        String oldPath = book.getCoverImagePath();
        String newPath = fileStorageService.store(file);
        book.setCoverImagePath(newPath);
        Book saved = bookRepository.save(book);
        fileStorageService.delete(oldPath);
        return toResponse(saved);
    }

    private void applyRequest(Book book, BookRequest request) {
        Author author = authorRepository.findById(request.authorId())
                .orElseThrow(() -> new ResourceNotFoundException("Author not found with id " + request.authorId()));

        List<Category> categories = categoryRepository.findAllById(request.categoryIds());
        if (categories.size() != request.categoryIds().size()) {
            throw new ResourceNotFoundException("One or more categories were not found: " + request.categoryIds());
        }

        book.setTitle(request.title());
        book.setIsbn(request.isbn());
        book.setPrice(request.price());
        book.setPublicationYear(request.publicationYear());
        book.setAuthor(author);
        book.setCategories(new HashSet<>(categories));
    }

    private Pageable buildPageable(int page, int size, String sortBy, String direction) {
        if (page < 0) {
            throw new IllegalArgumentException("page must be 0 or greater");
        }
        if (size < 1 || size > MAX_PAGE_SIZE) {
            throw new IllegalArgumentException("size must be between 1 and " + MAX_PAGE_SIZE);
        }
        if (!SORTABLE_FIELDS.contains(sortBy)) {
            throw new IllegalArgumentException("sortBy must be one of " + SORTABLE_FIELDS);
        }
        Sort sort = "desc".equalsIgnoreCase(direction)
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();
        return PageRequest.of(page, size, sort);
    }

    private Book getBook(Long id) {
        return bookRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Book not found with id " + id));
    }

    private BookResponse toResponse(Book book) {
        Author author = book.getAuthor();
        List<String> categoryNames = book.getCategories().stream()
                .map(Category::getName)
                .sorted()
                .toList();
        return new BookResponse(
                book.getId(),
                book.getTitle(),
                book.getIsbn(),
                book.getPrice(),
                book.getPublicationYear(),
                book.getCoverImagePath(),
                author.getId(),
                author.getFirstName() + " " + author.getLastName(),
                categoryNames
        );
    }
}
