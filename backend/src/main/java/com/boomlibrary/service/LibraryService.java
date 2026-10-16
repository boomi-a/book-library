package com.boomlibrary.service;

import com.boomlibrary.dto.request.LibraryRequest;
import com.boomlibrary.dto.response.LibraryResponse;
import com.boomlibrary.entity.Library;
import com.boomlibrary.entity.User;
import com.boomlibrary.exception.ResourceNotFoundException;
import com.boomlibrary.repository.LibraryRepository;
import com.boomlibrary.repository.UserRepository;
import com.boomlibrary.util.TextNormalizer;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class LibraryService {

    static final String DEFAULT_NAME = "My Library";

    private final LibraryRepository libraryRepository;
    private final UserRepository userRepository;

    @Transactional
    public List<LibraryResponse> getLibraries() {
        User user = currentUser();
        resolve(user, null); // makes sure the user has at least one library
        return libraryRepository.findByUserIdOrderByCreatedAtAsc(user.getId())
                .stream().map(this::toResponse).toList();
    }

    @Transactional
    public LibraryResponse createLibrary(LibraryRequest request) {
        User user = currentUser();
        String name = TextNormalizer.clean(request.name());
        if (name == null) {
            throw new IllegalArgumentException("Library name is required");
        }
        if (libraryRepository.existsByUserIdAndNameIgnoreCase(user.getId(), name)) {
            throw new IllegalArgumentException("A library named '" + name + "' already exists");
        }
        Library library = libraryRepository.save(Library.builder().user(user).name(name).build());
        return toResponse(library);
    }

    /**
     * The library a request should use: the given one when it belongs to the user, otherwise
     * the user's first library (created on demand if the user has none).
     */
    @Transactional
    public Library resolve(User user, UUID libraryId) {
        if (libraryId != null) {
            return libraryRepository.findByIdAndUserId(libraryId, user.getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Library not found"));
        }
        return libraryRepository.findFirstByUserIdOrderByCreatedAtAsc(user.getId())
                .orElseGet(() -> libraryRepository.save(
                        Library.builder().user(user).name(DEFAULT_NAME).build()));
    }

    private User currentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }

    private LibraryResponse toResponse(Library library) {
        return new LibraryResponse(
                library.getId(),
                library.getName(),
                library.getCreatedAt() != null ? library.getCreatedAt().toInstant() : null);
    }
}
