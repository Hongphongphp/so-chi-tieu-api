package com.phong.sochitieu.auth;

import com.phong.sochitieu.user.UserDtos.UserResponse;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public final class AuthDtos {

    private AuthDtos() {
    }

    public record RegisterRequest(
            @NotBlank(message = "Vui lòng nhập email")
            @Email(message = "Email không hợp lệ")
            String email,

            @NotBlank(message = "Vui lòng nhập mật khẩu")
            @Size(min = 6, max = 72, message = "Mật khẩu cần từ 6 đến 72 ký tự")
            String password,

            @NotBlank(message = "Vui lòng nhập tên hiển thị")
            @Size(max = 60, message = "Tên tối đa 60 ký tự")
            String displayName) {
    }

    public record LoginRequest(
            @NotBlank(message = "Vui lòng nhập email") String email,
            @NotBlank(message = "Vui lòng nhập mật khẩu") String password) {
    }

    public record AuthResponse(String token, UserResponse user) {
    }
}
