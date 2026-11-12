package com.phong.sochitieu.transaction;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public final class TransactionDtos {

    private TransactionDtos() {
    }

    public record TransactionRequest(
            @NotNull(message = "Chọn loại INCOME hoặc EXPENSE")
            TransactionType type,

            @NotNull(message = "Vui lòng nhập số tiền")
            @Positive(message = "Số tiền phải lớn hơn 0")
            Long amount,

            @NotBlank(message = "Vui lòng chọn danh mục")
            @Size(max = 50, message = "Danh mục tối đa 50 ký tự")
            String category,

            @NotNull(message = "Vui lòng chọn ngày")
            LocalDate date,

            @Size(max = 100, message = "Ghi chú tối đa 100 ký tự")
            String memo) {
    }

    public record TransactionResponse(Long id, TransactionType type, long amount,
                                      String category, LocalDate date, String memo) {
        public static TransactionResponse from(Transaction t) {
            return new TransactionResponse(t.getId(), t.getType(), t.getAmount(),
                    t.getCategory(), t.getDate(), t.getMemo());
        }
    }
}
