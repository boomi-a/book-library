package com.boomlibrary.dto.response;

import java.time.Instant;
import java.util.UUID;

public record LibraryResponse(UUID id, String name, Instant createdAt) {}
