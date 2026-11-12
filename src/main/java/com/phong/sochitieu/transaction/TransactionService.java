package com.phong.sochitieu.transaction;

import com.phong.sochitieu.common.ApiException;
import com.phong.sochitieu.transaction.TransactionDtos.TransactionRequest;
import com.phong.sochitieu.transaction.TransactionDtos.TransactionResponse;
import com.phong.sochitieu.user.User;
import com.phong.sochitieu.user.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.YearMonth;
import java.util.List;

@Service
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;

    public TransactionService(TransactionRepository transactionRepository, UserRepository userRepository) {
        this.transactionRepository = transactionRepository;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public List<Transaction> findByMonth(Long userId, YearMonth month) {
        return transactionRepository.findByUserIdAndDateBetweenOrderByDateDescIdDesc(
                userId, month.atDay(1), month.atEndOfMonth());
    }

    @Transactional
    public TransactionResponse create(Long userId, TransactionRequest req) {
        User userRef = userRepository.getReferenceById(userId);
        Transaction saved = transactionRepository.save(new Transaction(
                userRef, req.type(), req.amount(), req.category().trim(), req.date(), clean(req.memo())));
        return TransactionResponse.from(saved);
    }

    @Transactional
    public TransactionResponse update(Long userId, Long id, TransactionRequest req) {
        Transaction tx = findOwned(userId, id);
        tx.update(req.type(), req.amount(), req.category().trim(), req.date(), clean(req.memo()));
        return TransactionResponse.from(tx);
    }

    @Transactional
    public void delete(Long userId, Long id) {
        transactionRepository.delete(findOwned(userId, id));
    }

    private Transaction findOwned(Long userId, Long id) {
        return transactionRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Không tìm thấy khoản này"));
    }

    private static String clean(String memo) {
        if (memo == null || memo.isBlank()) return null;
        return memo.trim();
    }
}
