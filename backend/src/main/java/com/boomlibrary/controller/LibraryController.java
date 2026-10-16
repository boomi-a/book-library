package com.boomlibrary.controller;

import com.boomlibrary.dto.request.LibraryRequest;
import com.boomlibrary.dto.response.LibraryResponse;
import com.boomlibrary.service.LibraryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/libraries")
@RequiredArgsConstructor
public class LibraryController {

    private final LibraryService libraryService;

    @GetMapping
    public ResponseEntity<List<LibraryResponse>> getLibraries() {
        return ResponseEntity.ok(libraryService.getLibraries());
    }

    @PostMapping
    public ResponseEntity<LibraryResponse> createLibrary(@Valid @RequestBody LibraryRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(libraryService.createLibrary(request));
    }
}
