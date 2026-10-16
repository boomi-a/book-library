package com.boomlibrary.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record LibraryRequest(@NotBlank @Size(max = 100) String name) {}
