package com.phong.sochitieu.summary;

import com.phong.sochitieu.common.MonthParam;
import com.phong.sochitieu.transaction.Transaction;
import com.phong.sochitieu.transaction.TransactionRepository;
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
     * income       : tổng thu (chưa trừ chi phí đi kèm)
     * incomeCost   : tổng chi phí đi kèm các khoản thu (xăng xe, phí nền tảng...)
     * netIncome    : thu nhập thực nhận = income - incomeCost
     * expense      : tổng các khoản chi
     * balance      : còn lại = netIncome - expense
     * incomeGoal / goalPercent / goalRemaining: mục tiêu thực nhận tháng và tiến độ
     * dailyLimit   : số tiền có thể tiêu mỗi ngày còn lại để không vượt ngân sách (chỉ tháng hiện tại)
     * currentBalance: số dư hiện tại (mọi thời điểm), null nếu người dùng chưa nhập số dư
     */
    public record MonthlySummary(String month, long income, long incomeCost, long netIncome, long expense, long balance,
                                 long budget, Integer budgetUsedPercent, Integer daysLeft, Long dailyLimit,
                                 long incomeGoal, Integer goalPercent, Long goalRemaining,
                                 Long currentBalance,
                                 List<CategoryTotal> expenseByCategory) {
    }

    private final TransactionService transactionService;
    private final TransactionRepository transactionRepository;
    private final MonthParam monthParam;

    public SummaryController(TransactionService transactionService, TransactionRepository transactionRepository,
                             MonthParam monthParam) {
        this.transactionService = transactionService;
        this.transactionRepository = transactionRepository;
        this.monthParam = monthParam;
    }

    @GetMapping
    public MonthlySummary summary(@AuthenticationPrincipal User user,
                                  @RequestParam(required = false) String month) {
        YearMonth ym = monthParam.parse(month);
        List<Transaction> items = transactionService.findByMonth(user.getId(), ym);

        List<Transaction> incomes = items.stream().filter(t -> t.getType() == TransactionType.INCOME).toList();
        long income = incomes.stream().mapToLong(Transaction::getAmount).sum();
        long incomeCost = incomes.stream().mapToLong(Transaction::getCost).sum();
        long netIncome = income - incomeCost;
        long expense = items.stream().filter(t -> t.getType() == TransactionType.EXPENSE)
                .mapToLong(Transaction::getAmount).sum();

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

        long goal = user.getMonthlyIncomeGoal();
        Integer goalPercent = goal > 0 ? (int) Math.round(Math.max(0, netIncome) * 100.0 / goal) : null;
        Long goalRemaining = goal > 0 ? Math.max(0, goal - netIncome) : null;

        Long currentBalance = user.isBalanceSet()
                ? user.getOpeningBalance() + transactionRepository.sumAllTimeNet(user.getId())
                : null;

        return new MonthlySummary(ym.toString(), income, incomeCost, netIncome, expense, netIncome - expense,
                budget, usedPercent, daysLeft, dailyLimit, goal, goalPercent, goalRemaining, currentBalance, categories);
    }

    private static double round1(double v) {
        return Math.round(v * 10) / 10.0;
    }
}
