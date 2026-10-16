package com.boomlibrary.repository;

import com.boomlibrary.entity.Book;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BookRepository extends JpaRepository<Book, UUID> {

    Optional<Book> findByIdAndUserId(UUID id, UUID userId);

    List<Book> findByUserIdAndLibraryId(UUID userId, UUID libraryId);

    @Query("""
            SELECT b FROM Book b
            WHERE b.user.id = :userId
              AND b.library.id = :libraryId
              AND (CAST(:search AS string) IS NULL OR
                   LOWER(b.bookName) LIKE LOWER(CONCAT('%', CAST(:search AS string), '%'))
                   OR LOWER(b.author) LIKE LOWER(CONCAT('%', CAST(:search AS string), '%')))
              AND (:category IS NULL OR b.category = :category)
              AND (:isRead IS NULL OR b.read = :isRead)
            ORDER BY b.createdAt DESC
            """)
    List<Book> searchBooks(
            @Param("userId") UUID userId,
            @Param("libraryId") UUID libraryId,
            @Param("search") String search,
            @Param("category") String category,
            @Param("isRead") Boolean isRead
    );
}
