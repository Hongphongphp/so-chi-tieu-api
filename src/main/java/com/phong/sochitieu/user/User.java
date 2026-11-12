package com.phong.sochitieu.user;

import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 120)
    private String email;

    @Column(nullable = false)
    private String passwordHash;

    @Column(nullable = false, length = 60)
    private String displayName;

    /** Ngân sách chi tiêu mỗi tháng, đơn vị KRW. 0 = chưa đặt. */
    @Column(nullable = false)
    private long monthlyBudget = 0;

    @Column(nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    protected User() {
    }

    public User(String email, String passwordHash, String displayName) {
        this.email = email;
        this.passwordHash = passwordHash;
        this.displayName = displayName;
    }

    public Long getId() { return id; }
    public String getEmail() { return email; }
    public String getPasswordHash() { return passwordHash; }
    public String getDisplayName() { return displayName; }
    public long getMonthlyBudget() { return monthlyBudget; }
    public Instant getCreatedAt() { return createdAt; }

    public void setMonthlyBudget(long monthlyBudget) { this.monthlyBudget = monthlyBudget; }
    public void setDisplayName(String displayName) { this.displayName = displayName; }
}
