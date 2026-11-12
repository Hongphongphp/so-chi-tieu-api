package com.phong.sochitieu.transaction;

import com.phong.sochitieu.common.MonthParam;
import com.phong.sochitieu.transaction.TransactionDtos.TransactionRequest;
import com.phong.sochitieu.transaction.TransactionDtos.TransactionResponse;
import com.phong.sochitieu.user.User;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/transactions")
public class TransactionController {

    private final TransactionService service;
    private final MonthParam monthParam;

    public TransactionController(TransactionService service, MonthParam monthParam) {
        this.service = service;
        this.monthParam = monthParam;
    }

    /** GET /api/transactions?month=2026-11  (bỏ month thì lấy tháng hiện tại) */
    @GetMapping
    public List<TransactionResponse> list(@AuthenticationPrincipal User user,
                                          @RequestParam(required = false) String month) {
        return service.findByMonth(user.getId(), monthParam.parse(month)).stream()
                .map(TransactionResponse::from)
                .toList();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TransactionResponse create(@AuthenticationPrincipal User user,
                                      @Valid @RequestBody TransactionRequest request) {
        return service.create(user.getId(), request);
    }

    @PutMapping("/{id}")
    public TransactionResponse update(@AuthenticationPrincipal User user, @PathVariable Long id,
                                      @Valid @RequestBody TransactionRequest request) {
        return service.update(user.getId(), id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@AuthenticationPrincipal User user, @PathVariable Long id) {
        service.delete(user.getId(), id);
    }
}
