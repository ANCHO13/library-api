package com.example.library.repository;

import com.example.library.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

    boolean existsByNameIgnoreCase(String name);

    // @Query: categories with at least N books
    @Query("select c from Category c where size(c.books) >= :minBooks order by c.name")
    List<Category> findCategoriesWithAtLeast(@Param("minBooks") int minBooks);
}
