package com.todoapp.todo.model;

import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.mongodb.core.index.Indexed;
import java.time.LocalDateTime;

@Data
@Document(collection = "todos")
public class Todo {
    @Id
    private String id;

    private String title;

    private String description;

    private boolean completed = false;

    @Indexed
    private String userId;

    // Tự động quản lý bởi Spring Data MongoDB Auditing
    @CreatedDate
    @Field("created_at") // Tên cột trong DB
    private LocalDateTime createdAt;

    // Sẽ được cập nhật thủ công trong service khi completed = true
    @Field("completed_at")
    private LocalDateTime completedAt;

    // Có thể thêm @LastModifiedDate nếu muốn theo dõi lần cập nhật cuối
    // @LastModifiedDate
    // @Field("updated_at")
    // private LocalDateTime updatedAt;
} 