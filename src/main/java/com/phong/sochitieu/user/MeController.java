package com.phong.sochitieu.user;

import com.phong.sochitieu.common.ApiException;
import com.phong.sochitieu.transaction.TransactionRepository;
import com.phong.sochitieu.user.UserDtos.BalanceRequest;
import com.phong.sochitieu.user.UserDtos.BudgetRequest;
import com.phong.sochitieu.user.UserDtos.GoalRequest;
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
    private final TransactionRepository transactionRepository;

    public MeController(UserRepository userRepository, TransactionRepository transactionRepository) {
        this.userRepository = userRepository;
        this.transactionRepository = transactionRepository;
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

    @PutMapping("/goal")
    @Transactional
    public UserResponse updateGoal(@AuthenticationPrincipal User principal,
                                   @Valid @RequestBody GoalRequest request) {
        User user = userRepository.findById(principal.getId())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Không tìm thấy tài khoản"));
        user.setMonthlyIncomeGoal(request.monthlyIncomeGoal());
        return UserResponse.from(user);
    }

    /** Người dùng nhập số dư thật hiện tại; lưu lại số dư gốc để các khoản ghi sau tự cộng/trừ vào. */
    @PutMapping("/balance")
    @Transactional
    public UserResponse updateBalance(@AuthenticationPrincipal User principal,
                                      @Valid @RequestBody BalanceRequest request) {
        User user = userRepository.findById(principal.getId())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Không tìm thấy tài khoản"));
        long allTimeNet = transactionRepository.sumAllTimeNet(user.getId());
        user.setOpeningBalance(request.currentBalance() - allTimeNet);
        return UserResponse.from(user);
    }
}
