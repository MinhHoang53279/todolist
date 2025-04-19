package com.todoapp.user.controller;

import com.todoapp.user.dto.LoginResponse;
import com.todoapp.user.model.User;
import com.todoapp.user.service.UserService;
import com.todoapp.user.util.JwtUtil;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users") // Path sau khi gateway strip prefix /api
@RequiredArgsConstructor
@Slf4j
public class UserController {

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody User user) {
        // Thêm validation cơ bản
        if (user.getUsername() == null || user.getUsername().isEmpty() ||
            user.getPassword() == null || user.getPassword().isEmpty() ||
            user.getEmail() == null || user.getEmail().isEmpty()) {
            return ResponseEntity.badRequest().body("Username, password, and email are required.");
        }
        try {
            User createdUser = userService.createUser(user);
            // Không trả về password
            createdUser.setPassword(null);
            return new ResponseEntity<>(createdUser, HttpStatus.CREATED);
        } catch (RuntimeException e) {
            log.error("Registration failed for username: {}", user.getUsername(), e);
            // Trả về lỗi cụ thể hơn nếu có thể
            if (e.getMessage().contains("already exists")) {
                return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
            }
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Registration failed.");
        }
    }

    // Endpoint này có thể không cần thiết nếu chỉ dùng để login
    // @GetMapping("/{username}")
    // public ResponseEntity<User> getUser(@PathVariable String username) {
    //     try {
    //         User user = userService.findByUsername(username);
    //         user.setPassword(null);
    //         return ResponseEntity.ok(user);
    //     } catch (RuntimeException e) {
    //         return ResponseEntity.notFound().build();
    //     }
    // }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest) {
        log.info("Login attempt for username: {}", loginRequest.getUsername());
        if (loginRequest.getUsername() == null || loginRequest.getPassword() == null) {
             return ResponseEntity.badRequest().body("Username and password are required.");
        }
        try {
            User user = userService.findByUsername(loginRequest.getUsername());
            if (passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
                String token = jwtUtil.generateToken(user.getUsername());
                log.info("Login successful for username: {}", loginRequest.getUsername());
                return ResponseEntity.ok(new LoginResponse(token));
            } else {
                log.warn("Invalid credentials for username: {}", loginRequest.getUsername());
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid username or password");
            }
        } catch (RuntimeException e) {
            // Bắt lỗi "User not found" cụ thể hơn
            if (e.getMessage().contains("not found")) {
                 log.warn("Login attempt for non-existent username: {}", loginRequest.getUsername());
                 return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid username or password");
            }
            log.error("Login failed for username: {}", loginRequest.getUsername(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Login failed.");
        }
    }
}

// DTO cho request login
@Data
class LoginRequest {
    private String username;
    private String password;
} 