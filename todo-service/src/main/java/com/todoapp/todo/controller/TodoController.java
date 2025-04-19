package com.todoapp.todo.controller;

import com.todoapp.todo.model.Todo;
import com.todoapp.todo.service.TodoService;
import com.todoapp.todo.util.JwtUtil; // Ensure this util exists and works
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/todos") // Base path should be /todos after prefix stripping
@RequiredArgsConstructor
public class TodoController {

    private final TodoService todoService;
    private final JwtUtil jwtUtil;

    // Helper method to get userId from token
    private String getUserIdFromToken(String authorizationHeader) {
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            throw new RuntimeException("Missing or invalid Authorization header");
        }
        String token = authorizationHeader.substring(7);
        try {
            return jwtUtil.extractUsername(token); // Extract username from token
        } catch (Exception e) {
             throw new RuntimeException("Invalid token");
        }
    }

    @GetMapping
    public ResponseEntity<List<Todo>> getTodos(@RequestHeader("Authorization") String authorizationHeader) {
        try {
            String userId = getUserIdFromToken(authorizationHeader);
            return ResponseEntity.ok(todoService.getTodosByUserId(userId));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    @PostMapping
    public ResponseEntity<?> createTodo(@RequestHeader("Authorization") String authorizationHeader,
                                           @RequestBody TodoRequest todoRequest) {
        try {
            String userId = getUserIdFromToken(authorizationHeader);
            if (todoRequest.getTitle() == null || todoRequest.getTitle().isEmpty()) {
                return ResponseEntity.badRequest().body("Title cannot be empty");
            }
            Todo createdTodo = todoService.createTodo(userId, todoRequest.getTitle());
            return new ResponseEntity<>(createdTodo, HttpStatus.CREATED);
        } catch (RuntimeException e) {
             return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateTodo(@PathVariable String id,
                                           @RequestHeader("Authorization") String authorizationHeader,
                                           @RequestBody Todo todo) {
        try {
            String userId = getUserIdFromToken(authorizationHeader);
            // Ensure the body's user ID isn't trying to hijack (or ignore it)
            todo.setUserId(userId); // Make sure userId is set correctly
            Todo updated = todoService.updateTodo(id, userId, todo);
            return ResponseEntity.ok(updated);
        } catch (RuntimeException e) {
            if (e.getMessage().contains("Unauthorized")) {
                 return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            } else if (e.getMessage().contains("not found")) {
                 return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }
             return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build(); // Or more specific error
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTodo(@PathVariable String id,
                                           @RequestHeader("Authorization") String authorizationHeader) {
        try {
            String userId = getUserIdFromToken(authorizationHeader);
            todoService.deleteTodo(id, userId);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
             if (e.getMessage().contains("Unauthorized")) {
                 return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            } else if (e.getMessage().contains("not found")) {
                 return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }
             return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}

// DTO for creating todo
@lombok.Data
class TodoRequest {
    private String title;
} 