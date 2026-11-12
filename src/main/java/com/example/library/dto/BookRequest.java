package com.example.library.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.Set;

public record BookRequest(

        @NotBlank(message = "Title is required")
        @Size(max = 200, message = "Title must be at most 200 characters")
        String title,

        @NotBlank(message = "ISBN is required")
        @Size(min = 10, max = 20, message = "ISBN must be 10-20 characters")
        String isbn,

        @NotNull(message = "Price is required")
        @DecimalMin(value = "0.0", inclusive = false, message = "Price must be greater than 0")
        BigDecimal price,

        @NotNull(message = "Publication year is required")
        @Min(value = 1450, message = "Publication year must be 1450 or later")
        @Max(value = 2100, message = "Publication year must be 2100 or earlier")
        Integer publicationYear,

        @NotNull(message = "Author id is required")
        Long authorId,

        @NotEmpty(message = "At least one category id is required")
        Set<Long> categoryIds
) {
}
