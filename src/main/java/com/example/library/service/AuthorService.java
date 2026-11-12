package com.example.library.service;

import com.example.library.dto.AuthorBookCount;
import com.example.library.dto.AuthorRequest;
import com.example.library.dto.AuthorResponse;
import com.example.library.entity.Author;
import com.example.library.exception.DuplicateResourceException;
import com.example.library.exception.ResourceNotFoundException;
import com.example.library.repository.AuthorRepository;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AuthorService {

    private final AuthorRepository authorRepository;

    public AuthorService(AuthorRepository authorRepository) {
        this.authorRepository = authorRepository;
    }

    @Transactional(readOnly = true)
    public List<AuthorResponse> findAll() {
        return authorRepository.findAll(Sort.by("lastName", "firstName")).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<AuthorResponse> searchByName(String firstName, String lastName) {
        return authorRepository.findByFirstNameIgnoreCaseAndLastNameIgnoreCase(firstName, lastName).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public AuthorResponse findById(Long id) {
        return toResponse(getAuthor(id));
    }

    @Transactional(readOnly = true)
    public List<AuthorBookCount> bookCounts() {
        return authorRepository.countBooksPerAuthor();
    }

    @Transactional
    public AuthorResponse create(AuthorRequest request) {
        if (authorRepository.existsByEmailIgnoreCase(request.email())) {
            throw new DuplicateResourceException("An author with email " + request.email() + " already exists");
        }
        Author author = new Author(request.firstName(), request.lastName(), request.email());
        return toResponse(authorRepository.save(author));
    }

    @Transactional
    public AuthorResponse update(Long id, AuthorRequest request) {
        Author author = getAuthor(id);
        boolean emailChanged = !author.getEmail().equalsIgnoreCase(request.email());
        if (emailChanged && authorRepository.existsByEmailIgnoreCase(request.email())) {
            throw new DuplicateResourceException("An author with email " + request.email() + " already exists");
        }
        author.setFirstName(request.firstName());
        author.setLastName(request.lastName());
        author.setEmail(request.email());
        return toResponse(authorRepository.save(author));
    }

    @Transactional
    public void delete(Long id) {
        // Cascades to the author's books
        authorRepository.delete(getAuthor(id));
    }

    private Author getAuthor(Long id) {
        return authorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Author not found with id " + id));
    }

    private AuthorResponse toResponse(Author author) {
        return new AuthorResponse(author.getId(), author.getFirstName(), author.getLastName(), author.getEmail());
    }
}
