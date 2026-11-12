package com.phong.sochitieu.transaction;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    /** Tất cả khoản của một user trong khoảng ngày, mới nhất trước. */
    List<Transaction> findByUserIdAndDateBetweenOrderByDateDescIdDesc(Long userId, LocalDate from, LocalDate to);

    /** Chỉ tìm thấy khi khoản đó thuộc đúng user, để không ai sửa/xóa được dữ liệu người khác. */
    Optional<Transaction> findByIdAndUserId(Long id, Long userId);

    /** Tổng (thu thực nhận - chi) của một user từ trước đến nay. */
    @Query("""
            select coalesce(sum(case when t.type = com.phong.sochitieu.transaction.TransactionType.INCOME
                                     then t.amount - t.cost else -t.amount end), 0)
            from Transaction t where t.user.id = :userId
            """)
    long sumAllTimeNet(@Param("userId") Long userId);
}
