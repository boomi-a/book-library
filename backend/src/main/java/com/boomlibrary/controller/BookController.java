package com.boomlibrary.controller;

import com.boomlibrary.dto.request.BookRequest;
import com.boomlibrary.dto.response.BookImportResponse;
import com.boomlibrary.dto.response.BookResponse;
import com.boomlibrary.service.BookService;
import com.boomlibrary.service.ExcelBookParser;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@RestController
@RequestMapping("/api/books")
@RequiredArgsConstructor
public class BookController {

    private final BookService bookService;
    private final ExcelBookParser excelBookParser;

    @GetMapping
    public ResponseEntity<List<BookResponse>> getBooks(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) Boolean isRead,
            @RequestParam(required = false) UUID libraryId) {
        return ResponseEntity.ok(bookService.getBooks(search, category, isRead, libraryId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<BookResponse> getBook(@PathVariable UUID id) {
        return ResponseEntity.ok(bookService.getBook(id));
    }

    @PostMapping
    public ResponseEntity<BookResponse> createBook(@Valid @RequestBody BookRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(bookService.createBook(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<BookResponse> updateBook(
            @PathVariable UUID id, @Valid @RequestBody BookRequest request) {
        return ResponseEntity.ok(bookService.updateBook(id, request));
    }

    @PostMapping(value = "/import", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<BookImportResponse> importBooks(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "libraryId", required = false) UUID libraryId,
            @RequestParam(value = "sheet", required = false) String sheet) throws IOException {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("Please choose an Excel file to import");
        }
        String filename = Objects.toString(file.getOriginalFilename(), "").toLowerCase();
        if (!filename.endsWith(".xlsx")) {
            throw new IllegalArgumentException("Only .xlsx files are supported");
        }

        ExcelBookParser.ParseResult parsed = excelBookParser.parse(file.getInputStream(), sheet);
        BookService.ImportSummary summary = bookService.importBooks(parsed.books(), libraryId);
        return ResponseEntity.ok(new BookImportResponse(
                summary.inserted(), summary.updated(), summary.skipped(),
                parsed.errors(), parsed.columns(), parsed.unmatchedHeaders()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBook(@PathVariable UUID id) {
        bookService.deleteBook(id);
        return ResponseEntity.noContent().build();
    }
}
