package com.todoapp.todo.repository;

import com.todoapp.todo.model.Todo;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface TodoRepository extends MongoRepository<Todo, String> {
    List<Todo> findByUserId(String userId);
    List<Todo> findByUserIdAndCompleted(String userId, boolean completed);
} 