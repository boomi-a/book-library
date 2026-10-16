package com.boomlibrary.repository;

import com.boomlibrary.entity.Library;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface LibraryRepository extends JpaRepository<Library, UUID> {

    Optional<Library> findByIdAndUserId(UUID id, UUID userId);

    Optional<Library> findFirstByUserIdOrderByCreatedAtAsc(UUID userId);

    List<Library> findByUserIdOrderByCreatedAtAsc(UUID userId);

    boolean existsByUserIdAndNameIgnoreCase(UUID userId, String name);
}
