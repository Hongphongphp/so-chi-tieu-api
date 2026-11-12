package com.phong.sochitieu.transaction;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    /** Tất cả khoản của một user trong khoảng ngày, mới nhất trước. */
    List<Transaction> findByUserIdAndDateBetweenOrderByDateDescIdDesc(Long userId, LocalDate from, LocalDate to);

    /** Chỉ tìm thấy khi khoản đó thuộc đúng user, để không ai sửa/xóa được dữ liệu người khác. */
    Optional<Transaction> findByIdAndUserId(Long id, Long userId);
}
