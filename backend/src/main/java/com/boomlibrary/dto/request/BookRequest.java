package com.boomlibrary.dto.request;

import jakarta.validation.constraints.NotBlank;

import java.time.Instant;
import java.util.UUID;

/** libraryId is used on create and import; the default library is used when it is null. */
public record BookRequest(
        @NotBlank String bookName,
        String author,
        String publisher,
        String category,
        Instant boughtAt,
        boolean read,
        String comments,
        UUID libraryId
) {}
