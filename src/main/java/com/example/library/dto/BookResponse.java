package com.example.library.dto;

import java.math.BigDecimal;
import java.util.List;

public record BookResponse(
        Long id,
        String title,
        String isbn,
        BigDecimal price,
        Integer publicationYear,
        String coverImagePath,
        Long authorId,
        String authorName,
        List<String> categories
) {
}
