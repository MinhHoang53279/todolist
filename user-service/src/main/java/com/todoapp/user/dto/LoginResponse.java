package com.todoapp.user.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponse {
    private String token;
    // Có thể thêm các thông tin khác nếu cần, ví dụ: username, roles
} 