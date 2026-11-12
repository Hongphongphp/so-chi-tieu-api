package com.phong.sochitieu.user;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

public final class UserDtos {

    private UserDtos() {
    }

    public record UserResponse(Long id, String email, String displayName, long monthlyBudget) {
        public static UserResponse from(User u) {
            return new UserResponse(u.getId(), u.getEmail(), u.getDisplayName(), u.getMonthlyBudget());
        }
    }

    public record BudgetRequest(
            @NotNull(message = "Vui lòng nhập ngân sách")
            @PositiveOrZero(message = "Ngân sách không được âm")
            Long monthlyBudget) {
    }
}
