package com.boomlibrary.dto.request;

import jakarta.validation.constraints.NotBlank;

import java.time.Instant;

public record BookRequest(
        @NotBlank String bookName,
        String author,
        String publisher,
        String category,
        Instant boughtAt,
        boolean read,
        String comments
) {}
