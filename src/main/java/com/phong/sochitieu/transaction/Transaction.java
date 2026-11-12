package com.phong.sochitieu.transaction;

import com.phong.sochitieu.user.User;
import jakarta.persistence.*;

import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(name = "transactions", indexes = @Index(name = "idx_tx_user_date", columnList = "user_id, tx_date"))
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private TransactionType type;

    /** Số tiền KRW, luôn dương. Loại thu/chi nằm ở type. */
    @Column(nullable = false)
    private long amount;

    @Column(nullable = false, length = 50)
    private String category;

    @Column(name = "tx_date", nullable = false)
    private LocalDate date;

    @Column(length = 100)
    private String memo;

    @Column(nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    protected Transaction() {
    }

    public Transaction(User user, TransactionType type, long amount, String category, LocalDate date, String memo) {
        this.user = user;
        update(type, amount, category, date, memo);
    }

    public void update(TransactionType type, long amount, String category, LocalDate date, String memo) {
        this.type = type;
        this.amount = amount;
        this.category = category;
        this.date = date;
        this.memo = memo;
    }

    public Long getId() { return id; }
    public User getUser() { return user; }
    public TransactionType getType() { return type; }
    public long getAmount() { return amount; }
    public String getCategory() { return category; }
    public LocalDate getDate() { return date; }
    public String getMemo() { return memo; }
    public Instant getCreatedAt() { return createdAt; }
}
