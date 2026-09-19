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

    @Query("""
            SELECT b FROM Book b
            WHERE b.user.id = :userId
              AND (:search IS NULL OR
                   LOWER(b.bookName) LIKE LOWER(CONCAT('%', :search, '%'))
                   OR LOWER(b.author) LIKE LOWER(CONCAT('%', :search, '%')))
              AND (:category IS NULL OR b.category = :category)
              AND (:isRead IS NULL OR b.read = :isRead)
            ORDER BY b.createdAt DESC
            """)
    List<Book> searchBooks(
            @Param("userId") UUID userId,
            @Param("search") String search,
            @Param("category") String category,
            @Param("isRead") Boolean isRead
    );
}
