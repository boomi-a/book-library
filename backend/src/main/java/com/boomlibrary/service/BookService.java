package com.boomlibrary.service;

import com.boomlibrary.dto.request.BookRequest;
import com.boomlibrary.dto.response.BookResponse;
import com.boomlibrary.entity.Book;
import com.boomlibrary.entity.Library;
import com.boomlibrary.entity.User;
import com.boomlibrary.exception.ResourceNotFoundException;
import com.boomlibrary.repository.BookRepository;
import com.boomlibrary.repository.UserRepository;
import com.boomlibrary.util.TextNormalizer;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BookService {

    private final BookRepository bookRepository;
    private final UserRepository userRepository;
    private final LibraryService libraryService;

    private User currentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }

    public List<BookResponse> getBooks(String search, String category, Boolean isRead, UUID libraryId) {
        User user = currentUser();
        Library library = libraryService.resolve(user, libraryId);
        return bookRepository.searchBooks(user.getId(), library.getId(), search, category, isRead)
                .stream().map(this::toResponse).toList();
    }

    public BookResponse getBook(UUID bookId) {
        UUID userId = currentUser().getId();
        Book book = bookRepository.findByIdAndUserId(bookId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Book not found"));
        return toResponse(book);
    }

    public BookResponse createBook(BookRequest request) {
        User user = currentUser();
        Library library = libraryService.resolve(user, request.libraryId());

        Book book = Book.builder()
                .user(user)
                .library(library)
                .bookName(request.bookName())
                .author(request.author())
                .publisher(request.publisher())
                .category(request.category())
                .boughtAt(request.boughtAt() != null ? request.boughtAt().atOffset(ZoneOffset.UTC) : null)
                .read(request.read())
                .comments(request.comments())
                .build();

        return toResponse(bookRepository.save(book));
    }

    public BookResponse updateBook(UUID bookId, BookRequest request) {
        UUID userId = currentUser().getId();
        Book book = bookRepository.findByIdAndUserId(bookId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Book not found"));

        book.setBookName(request.bookName());
        book.setAuthor(request.author());
        book.setPublisher(request.publisher());
        book.setCategory(request.category());
        book.setBoughtAt(request.boughtAt() != null ? request.boughtAt().atOffset(ZoneOffset.UTC) : null);
        book.setRead(request.read());
        book.setComments(request.comments());

        return toResponse(bookRepository.save(book));
    }

    public record ImportSummary(int inserted, int updated, int skipped) {}

    /**
     * Imports books into one library without creating duplicates. A book is the same book
     * when its title matches (ignoring case and extra spaces) within that library. A title
     * already there gets only its blank fields filled; repeated titles in the file are skipped.
     */
    @Transactional
    public ImportSummary importBooks(List<BookRequest> requests, UUID libraryId) {
        User user = currentUser();
        Library library = libraryService.resolve(user, libraryId);

        Map<String, Book> existing = new HashMap<>();
        for (Book book : bookRepository.findByUserIdAndLibraryId(user.getId(), library.getId())) {
            existing.putIfAbsent(TextNormalizer.bookKey(book.getBookName()), book);
        }

        Set<String> seenInFile = new HashSet<>();
        List<Book> newBooks = new ArrayList<>();
        List<Book> filledBooks = new ArrayList<>();
        int skipped = 0;

        for (BookRequest request : requests) {
            String key = TextNormalizer.bookKey(request.bookName());
            if (!seenInFile.add(key)) {
                skipped++;
                continue;
            }
            Book match = existing.get(key);
            if (match == null) {
                newBooks.add(toEntity(user, library, request));
            } else if (fillBlankFields(match, request)) {
                filledBooks.add(match);
            } else {
                skipped++;
            }
        }

        bookRepository.saveAll(newBooks);
        bookRepository.saveAll(filledBooks);
        return new ImportSummary(newBooks.size(), filledBooks.size(), skipped);
    }

    private Book toEntity(User user, Library library, BookRequest request) {
        return Book.builder()
                .user(user)
                .library(library)
                .bookName(TextNormalizer.clean(request.bookName()))
                .author(TextNormalizer.clean(request.author()))
                .publisher(TextNormalizer.clean(request.publisher()))
                .category(TextNormalizer.clean(request.category()))
                .boughtAt(request.boughtAt() != null ? request.boughtAt().atOffset(ZoneOffset.UTC) : null)
                .read(request.read())
                .comments(TextNormalizer.clean(request.comments()))
                .build();
    }

    /** Fills only the fields the existing book leaves empty. Returns true if anything changed. */
    private boolean fillBlankFields(Book book, BookRequest request) {
        boolean changed = false;
        if (isBlank(book.getAuthor()) && TextNormalizer.clean(request.author()) != null) {
            book.setAuthor(TextNormalizer.clean(request.author()));
            changed = true;
        }
        if (isBlank(book.getPublisher()) && TextNormalizer.clean(request.publisher()) != null) {
            book.setPublisher(TextNormalizer.clean(request.publisher()));
            changed = true;
        }
        if (isBlank(book.getCategory()) && TextNormalizer.clean(request.category()) != null) {
            book.setCategory(TextNormalizer.clean(request.category()));
            changed = true;
        }
        if (book.getBoughtAt() == null && request.boughtAt() != null) {
            book.setBoughtAt(request.boughtAt().atOffset(ZoneOffset.UTC));
            changed = true;
        }
        if (isBlank(book.getComments()) && TextNormalizer.clean(request.comments()) != null) {
            book.setComments(TextNormalizer.clean(request.comments()));
            changed = true;
        }
        return changed;
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    public void deleteBook(UUID bookId) {
        UUID userId = currentUser().getId();
        Book book = bookRepository.findByIdAndUserId(bookId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Book not found"));
        bookRepository.delete(book);
    }

    private BookResponse toResponse(Book b) {
        return new BookResponse(
                b.getId(),
                b.getBookName(),
                b.getAuthor(),
                b.getPublisher(),
                b.getCategory(),
                b.getBoughtAt() != null ? b.getBoughtAt().toInstant() : null,
                b.isRead(),
                b.getComments(),
                b.getCreatedAt().toInstant(),
                b.getUpdatedAt().toInstant()
        );
    }
}
