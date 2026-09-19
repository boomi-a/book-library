package com.boomlibrary.service;

import com.boomlibrary.dto.request.BookRequest;
import com.boomlibrary.dto.response.BookResponse;
import com.boomlibrary.entity.Book;
import com.boomlibrary.entity.User;
import com.boomlibrary.exception.ResourceNotFoundException;
import com.boomlibrary.repository.BookRepository;
import com.boomlibrary.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BookService {

    private final BookRepository bookRepository;
    private final UserRepository userRepository;

    private UUID getCurrentUserId() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"))
                .getId();
    }

    public List<BookResponse> getBooks(String search, String category, Boolean isRead) {
        UUID userId = getCurrentUserId();
        return bookRepository.searchBooks(userId, search, category, isRead)
                .stream().map(this::toResponse).toList();
    }

    public BookResponse getBook(UUID bookId) {
        UUID userId = getCurrentUserId();
        Book book = bookRepository.findByIdAndUserId(bookId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Book not found"));
        return toResponse(book);
    }

    public BookResponse createBook(BookRequest request) {
        UUID userId = getCurrentUserId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Book book = Book.builder()
                .user(user)
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
        UUID userId = getCurrentUserId();
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

    public void deleteBook(UUID bookId) {
        UUID userId = getCurrentUserId();
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
