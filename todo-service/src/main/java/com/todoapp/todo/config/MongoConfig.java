package com.todoapp.todo.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.config.EnableMongoAuditing;

@Configuration
@EnableMongoAuditing // Bật tính năng Auditing
public class MongoConfig {
    // Có thể thêm các cấu hình khác cho MongoDB ở đây nếu cần
} 