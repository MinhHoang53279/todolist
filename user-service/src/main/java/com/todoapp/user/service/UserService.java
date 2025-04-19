package com.todoapp.user.service;

import com.todoapp.user.model.User;
import com.todoapp.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.lang.RuntimeException;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public User createUser(User user) {
        log.debug("Attempting to create user with username: {}", user.getUsername());
        if (userRepository.existsByUsername(user.getUsername())) {
            log.warn("Username {} already exists", user.getUsername());
            throw new RuntimeException("Username already exists");
        }
        if (userRepository.existsByEmail(user.getEmail())) {
            log.warn("Email {} already exists", user.getEmail());
            throw new RuntimeException("Email already exists");
        }
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        User savedUser = userRepository.save(user);
        log.info("User created successfully with ID: {}", savedUser.getId());
        return savedUser;
    }

    public User findByUsername(String username) {
        log.debug("Finding user by username: {}", username);
        return userRepository.findByUsername(username)
                .orElseThrow(() -> {
                    log.warn("User not found with username: {}", username);
                    return new RuntimeException("User not found with username: " + username);
                });
    }

    // Phương thức này có thể không cần thiết nếu không dùng đến
    // public User findByEmail(String email) {
    //     log.debug("Finding user by email: {}", email);
    //     return userRepository.findByEmail(email)
    //             .orElseThrow(() -> {
    //                  log.warn("User not found with email: {}", email);
    //                  return new RuntimeException("User not found with email: " + email);
    //              });
    // }
} 