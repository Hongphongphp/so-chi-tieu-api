package com.phong.sochitieu.summary;

import com.phong.sochitieu.common.MonthParam;
import com.phong.sochitieu.transaction.Transaction;
import com.phong.sochitieu.transaction.TransactionService;
import com.phong.sochitieu.transaction.TransactionType;
import com.phong.sochitieu.user.User;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/summary")
public class SummaryController {

    public record CategoryTotal(String category, long total, double percent) {
    }

    /**
     * Tổng kết một tháng.
     * dailyLimit: số tiền có thể tiêu mỗi ngày còn lại để không vượt ngân sách
     * (chỉ tính cho tháng hiện tại và khi đã đặt ngân sách).
     */
    public record MonthlySummary(String month, long income, long expense, long balance,
                                 long budget, Integer budgetUsedPercent, Integer daysLeft, Long dailyLimit,
                                 List<CategoryTotal> expenseByCategory) {
    }

    private final TransactionService transactionService;
    private final MonthParam monthParam;

    public SummaryController(TransactionService transactionService, MonthParam monthParam) {
        this.transactionService = transactionService;
        this.monthParam = monthParam;
    }

    @GetMapping
    public MonthlySummary summary(@AuthenticationPrincipal User user,
                                  @RequestParam(required = false) String month) {
        YearMonth ym = monthParam.parse(month);
        List<Transaction> items = transactionService.findByMonth(user.getId(), ym);

        long income = sum(items, TransactionType.INCOME);
        long expense = sum(items, TransactionType.EXPENSE);

        Map<String, Long> byCat = items.stream()
                .filter(t -> t.getType() == TransactionType.EXPENSE)
                .collect(Collectors.groupingBy(Transaction::getCategory, Collectors.summingLong(Transaction::getAmount)));
        List<CategoryTotal> categories = byCat.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .map(e -> new CategoryTotal(e.getKey(), e.getValue(), round1(e.getValue() * 100.0 / expense)))
                .toList();

        long budget = user.getMonthlyBudget();
        Integer usedPercent = budget > 0 ? (int) Math.round(expense * 100.0 / budget) : null;

        Integer daysLeft = null;
        Long dailyLimit = null;
        LocalDate today = monthParam.today();
        if (YearMonth.from(today).equals(ym)) {
            daysLeft = ym.lengthOfMonth() - today.getDayOfMonth() + 1;
            if (budget > 0) {
                dailyLimit = Math.max(0, (budget - expense) / daysLeft);
            }
        }

        return new MonthlySummary(ym.toString(), income, expense, income - expense,
                budget, usedPercent, daysLeft, dailyLimit, categories);
    }

    private static long sum(List<Transaction> items, TransactionType type) {
        return items.stream().filter(t -> t.getType() == type).mapToLong(Transaction::getAmount).sum();
    }

    private static double round1(double v) {
        return Math.round(v * 10) / 10.0;
    }
}
