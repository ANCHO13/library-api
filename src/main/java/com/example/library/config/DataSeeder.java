package com.example.library.config;

import com.example.library.entity.Author;
import com.example.library.entity.Book;
import com.example.library.entity.Category;
import com.example.library.repository.AuthorRepository;
import com.example.library.repository.BookRepository;
import com.example.library.repository.CategoryRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.math.BigDecimal;
import java.util.Set;

// Inserts sample data on first start
@Configuration
public class DataSeeder {

    private final AuthorRepository authorRepository;
    private final BookRepository bookRepository;
    private final CategoryRepository categoryRepository;

    public DataSeeder(AuthorRepository authorRepository,
                      BookRepository bookRepository,
                      CategoryRepository categoryRepository) {
        this.authorRepository = authorRepository;
        this.bookRepository = bookRepository;
        this.categoryRepository = categoryRepository;
    }

    @Bean
    public CommandLineRunner seedData() {
        return args -> {
            if (authorRepository.count() > 0) {
                return;
            }

            Category fiction = categoryRepository.save(new Category("Fiction", "Novels and stories"));
            Category classic = categoryRepository.save(new Category("Classic", "Books that stood the test of time"));
            Category dystopia = categoryRepository.save(new Category("Dystopia", "Dark visions of society"));

            Author orwell = authorRepository.save(new Author("George", "Orwell", "orwell@example.com"));
            Author austen = authorRepository.save(new Author("Jane", "Austen", "austen@example.com"));

            bookRepository.save(book("1984", "9780451524935", "12.99", 1949, orwell, Set.of(fiction, classic, dystopia)));
            bookRepository.save(book("Animal Farm", "9780451526342", "9.50", 1945, orwell, Set.of(fiction, classic)));
            bookRepository.save(book("Pride and Prejudice", "9780141439518", "8.75", 1813, austen, Set.of(fiction, classic)));
            bookRepository.save(book("Emma", "9780141439587", "10.20", 1815, austen, Set.of(fiction)));
        };
    }

    private Book book(String title, String isbn, String price, int year, Author author, Set<Category> categories) {
        Book book = new Book();
        book.setTitle(title);
        book.setIsbn(isbn);
        book.setPrice(new BigDecimal(price));
        book.setPublicationYear(year);
        book.setAuthor(author);
        book.getCategories().addAll(categories);
        return book;
    }
}
