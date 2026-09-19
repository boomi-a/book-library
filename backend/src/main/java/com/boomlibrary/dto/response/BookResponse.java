package com.boomlibrary.dto.response;

import java.time.Instant;
import java.util.UUID;

public record BookResponse(
        UUID id,
        String bookName,
        String author,
        String publisher,
        String category,
        Instant boughtAt,
        boolean read,
        String comments,
        Instant createdAt,
        Instant updatedAt
) {}
