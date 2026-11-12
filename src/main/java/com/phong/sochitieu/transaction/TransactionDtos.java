package com.phong.sochitieu.transaction;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
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

            /** Chi phí đi kèm khoản thu (không bắt buộc, bỏ trống = 0). Bỏ qua với khoản chi. */
            @PositiveOrZero(message = "Chi phí không được âm")
            Long cost,

            @NotBlank(message = "Vui lòng chọn danh mục")
            @Size(max = 50, message = "Danh mục tối đa 50 ký tự")
            String category,

            @NotNull(message = "Vui lòng chọn ngày")
            LocalDate date,

            @Size(max = 100, message = "Ghi chú tối đa 100 ký tự")
            String memo) {

        public long costOrZero() {
            return cost == null ? 0 : cost;
        }
    }

    public record TransactionResponse(Long id, TransactionType type, long amount, long cost, long net,
                                      String category, LocalDate date, String memo) {
        public static TransactionResponse from(Transaction t) {
            return new TransactionResponse(t.getId(), t.getType(), t.getAmount(), t.getCost(), t.getNet(),
                    t.getCategory(), t.getDate(), t.getMemo());
        }
    }
}
