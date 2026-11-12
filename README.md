# Library API

Spring Boot backend for managing a library: authors, books and categories.

- Java 17+, Spring Boot 3.3, Spring Data JPA, H2 (file database, no setup needed)
- Layered packages: `controller`, `service`, `repository`, `entity`, plus `dto`, `config`, `exception`

## Run

```
mvn spring-boot:run
```

The app starts on http://localhost:8080 and inserts a few sample authors, books and categories on first start.

- Upload form: http://localhost:8080/upload.html
- H2 console: http://localhost:8080/h2-console (JDBC URL `jdbc:h2:file:./data/librarydb`, user `sa`, empty password)

## Data model

| Entity | Relationships |
|---|---|
| Author | `@OneToMany` books |
| Book | `@ManyToOne` author, `@ManyToMany` categories (join table `book_categories`) |
| Category | `@ManyToMany` books (inverse side) |

## Endpoints

| Method | Path | Notes |
|---|---|---|
| GET | `/api/books` | Paginated + sorted. Params: `page`, `size`, `sortBy` (id, title, price, publicationYear, isbn), `direction` (asc/desc), `title`, `minYear` |
| GET | `/api/books/search` | `category` (required), `minPrice`, `maxPrice`, plus paging params |
| GET | `/api/books/without-cover` | Books with no uploaded cover |
| GET | `/api/books/{id}` | |
| POST | `/api/books` | JSON body, validated |
| PUT | `/api/books/{id}` | JSON body, validated |
| DELETE | `/api/books/{id}` | |
| POST | `/api/books/{id}/cover` | `multipart/form-data`, field `file`. Saved in `uploads/`, path stored in `books.cover_image_path` |
| GET | `/api/authors` | Optional `firstName` + `lastName` search |
| GET | `/api/authors/{id}` | |
| GET | `/api/authors/{id}/books` | Optional `maxPrice` |
| GET | `/api/authors/stats/book-counts` | Books per author |
| POST / PUT / DELETE | `/api/authors`, `/api/authors/{id}` | |
| GET | `/api/categories` | Optional `minBooks` |
| GET / POST / PUT / DELETE | `/api/categories`, `/api/categories/{id}` | |

## Examples

```
curl "http://localhost:8080/api/books?page=0&size=2&sortBy=price&direction=desc"

curl -X POST http://localhost:8080/api/authors -H "Content-Type: application/json" \
  -d '{"firstName":"Leo","lastName":"Tolstoy","email":"tolstoy@example.com"}'

curl -X POST http://localhost:8080/api/books -H "Content-Type: application/json" \
  -d '{"title":"War and Peace","isbn":"9780199232765","price":14.5,"publicationYear":1869,"authorId":3,"categoryIds":[1,2]}'

curl -X POST http://localhost:8080/api/books/1/cover -F "file=@cover.png"
```

## Where each requirement is implemented

| Requirement | Location |
|---|---|
| Stereotype annotations | `@RestController` controllers, `@Service` services, `@Repository` repositories, `@Configuration` in `config` |
| Constructor injection (no field `@Autowired`) | every controller, service and config class |
| 3 entities, 2 relationship types | `entity/Author`, `entity/Book`, `entity/Category` |
| Method-name queries | `BookRepository.findByTitleContainingIgnoreCaseAndPublicationYearGreaterThanEqual`, `BookRepository.findByAuthorIdAndPriceLessThanEqual`, `AuthorRepository.findByFirstNameIgnoreCaseAndLastNameIgnoreCase` |
| `@Query` queries | `BookRepository.findByCategoryAndPriceRange`, `BookRepository.findBooksWithoutCover`, `AuthorRepository.countBooksPerAuthor`, `CategoryRepository.findCategoriesWithAtLeast` |
| `@Transactional` | every create / update / delete / upload method in the services |
| `@PathVariable`, `@RequestParam`, `@RequestBody` | all three controllers |
| Validation + `@Valid` | `dto/*Request` records, POST and PUT endpoints; errors formatted in `GlobalExceptionHandler` |
| File upload | `BookController.uploadCover`, `FileStorageService`, `static/upload.html` |
| Pagination and sorting | `BookService.buildPageable` (`PageRequest`, `Sort`), `GET /api/books`, `GET /api/books/search` |
