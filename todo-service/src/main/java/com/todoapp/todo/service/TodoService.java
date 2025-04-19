package com.todoapp.todo.service;

import com.todoapp.todo.model.Todo;
import com.todoapp.todo.repository.TodoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class TodoService {

    private final TodoRepository todoRepository;

    public List<Todo> getTodosByUserId(String userId) {
        log.debug("Fetching todos for userId: {}", userId);
        return todoRepository.findByUserId(userId);
    }

    @Transactional
    public Todo createTodo(String userId, String title) {
        log.info("Creating todo for userId: {} with title: {}", userId, title);
        Todo todo = new Todo();
        todo.setUserId(userId);
        todo.setTitle(title);
        // createdAt sẽ được tự động đặt bởi @CreatedDate
        // todo.setCreatedAt(LocalDateTime.now()); // Không cần thiết nữa
        return todoRepository.save(todo);
    }

    @Transactional
    public Todo updateTodo(String id, String userId, Todo updatedTodo) {
        log.debug("Updating todo with id: {} for userId: {}", id, userId);
        Todo existingTodo = todoRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Update failed: Todo not found with id: {}", id);
                    return new RuntimeException("Todo not found");
                });

        if (!existingTodo.getUserId().equals(userId)) {
            log.warn("Unauthorized attempt to update todo id: {} by userId: {}", id, userId);
            throw new RuntimeException("Unauthorized access to todo");
        }

        boolean statusChanged = existingTodo.isCompleted() != updatedTodo.isCompleted();

        existingTodo.setTitle(updatedTodo.getTitle());
        existingTodo.setCompleted(updatedTodo.isCompleted());

        // Chỉ cập nhật completedAt nếu trạng thái completed thay đổi
        if (statusChanged) {
            if (existingTodo.isCompleted()) {
                log.debug("Marking todo id: {} as completed at {}", id, LocalDateTime.now());
                existingTodo.setCompletedAt(LocalDateTime.now());
            } else {
                log.debug("Marking todo id: {} as not completed, clearing completedAt", id);
                existingTodo.setCompletedAt(null); // Đặt lại thành null nếu không hoàn thành
            }
        }
        // Lưu ý: updatedAt sẽ tự động cập nhật nếu bạn thêm @LastModifiedDate

        Todo savedTodo = todoRepository.save(existingTodo);
        log.info("Todo updated successfully for id: {}", id);
        return savedTodo;
    }

    @Transactional
    public void deleteTodo(String id, String userId) {
        log.info("Deleting todo with id: {} for userId: {}", id, userId);
        Todo existingTodo = todoRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Delete failed: Todo not found with id: {}", id);
                    return new RuntimeException("Todo not found");
                });

        if (!existingTodo.getUserId().equals(userId)) {
            log.warn("Unauthorized attempt to delete todo id: {} by userId: {}", id, userId);
            throw new RuntimeException("Unauthorized access to todo");
        }
        todoRepository.deleteById(id);
        log.info("Todo deleted successfully for id: {}", id);
    }
} 