package com.boomlibrary.service;

import com.boomlibrary.dto.response.UserResponse;
import com.boomlibrary.entity.User;
import com.boomlibrary.exception.ResourceNotFoundException;
import com.boomlibrary.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public List<UserResponse> getAllUsers() {
        return userRepository.findAll().stream().map(this::toResponse).toList();
    }

    public UserResponse getUserById(UUID userId) {
        return toResponse(userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found")));
    }

    public UserResponse setUserActive(UUID userId, boolean active) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        user.setActive(active);
        return toResponse(userRepository.save(user));
    }

    private UserResponse toResponse(User u) {
        return new UserResponse(
                u.getId(),
                u.getEmail(),
                u.getUsername(),
                u.getRole().name(),
                u.isActive(),
                u.getCreatedAt().toInstant()
        );
    }
}
