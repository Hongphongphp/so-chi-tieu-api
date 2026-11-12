package com.phong.sochitieu.user;

import com.phong.sochitieu.common.ApiException;
import com.phong.sochitieu.user.UserDtos.BudgetRequest;
import com.phong.sochitieu.user.UserDtos.UserResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/me")
public class MeController {

    private final UserRepository userRepository;

    public MeController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @GetMapping
    public UserResponse me(@AuthenticationPrincipal User user) {
        return UserResponse.from(user);
    }

    @PutMapping("/budget")
    @Transactional
    public UserResponse updateBudget(@AuthenticationPrincipal User principal,
                                     @Valid @RequestBody BudgetRequest request) {
        User user = userRepository.findById(principal.getId())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Không tìm thấy tài khoản"));
        user.setMonthlyBudget(request.monthlyBudget());
        return UserResponse.from(user);
    }
}
