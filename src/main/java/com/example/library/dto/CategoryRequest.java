package com.example.library.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CategoryRequest(

        @NotBlank(message = "Name is required")
        @Size(min = 2, max = 60, message = "Name must be 2-60 characters")
        String name,

        @Size(max = 255, message = "Description must be at most 255 characters")
        String description
) {
}
