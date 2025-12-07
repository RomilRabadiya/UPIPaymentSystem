package com.example.UPIPaymentSystem.Entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;

@Entity
@Table(name = "bank_accounts")
public class BankAccount
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Bank account number
    @NotBlank(message = "Account number cannot be empty")
    @Size(min = 8, max = 20, message = "Account number must be between 8 and 20 characters")
    @Column(unique = true, nullable = false, length = 20)
    private String accountNumber;

    // IFSC Code
    @NotBlank(message = "IFSC Code cannot be empty")
    @Pattern(regexp = "^[A-Z]{4}0[A-Z0-9]{6}$", message = "Invalid IFSC Code format")
    @Column(nullable = false, length = 11)
    private String ifscCode;

    // Bank name
    @NotBlank(message = "Bank name cannot be empty")
    @Size(min = 2, message = "Bank name must have at least 2 characters")
    @Column(nullable = false)
    private String bankName;

    // Current balance
    @NotNull(message = "Balance is required")
    @DecimalMin(value = "0.00", inclusive = true, message = "Balance cannot be negative")
    @Digits(integer = 15, fraction = 2, message = "Invalid balance format")
    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal balance;

    // Account type (Savings, Current, etc.)
    @NotBlank(message = "Account type is required")
    @Pattern(regexp = "SAVINGS|CURRENT", message = "Account type must be SAVINGS or CURRENT")
    @Column(nullable = false)
    private String accountType;

    // mark primary account
    @Column(nullable = false)
    private boolean primaryAccount = false;

    @ManyToOne
    @JoinColumn(name = "foreginkey_user_mobile", referencedColumnName = "mobile")
    private User user;

    public BankAccount() {}

    public BankAccount(String accountNumber, String ifscCode, String bankName, BigDecimal balance, String accountType)
    {
        this.accountNumber = accountNumber;
        this.ifscCode = ifscCode;
        this.bankName = bankName;
        this.balance = balance;
        this.accountType = accountType;
    }

    // Getters & Setters
    public boolean isPrimaryAccount() {return primaryAccount;}
    public void setPrimaryAccount(boolean primaryAccount) {this.primaryAccount = primaryAccount;}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getAccountNumber() { return accountNumber; }
    public void setAccountNumber(String accountNumber) { this.accountNumber = accountNumber; }

    public String getIfscCode() { return ifscCode; }
    public void setIfscCode(String ifscCode) { this.ifscCode = ifscCode; }

    public String getBankName() { return bankName; }
    public void setBankName(String bankName) { this.bankName = bankName; }

    public BigDecimal getBalance() { return balance; }
    public void setBalance(BigDecimal balance) { this.balance = balance; }

    public String getAccountType() { return accountType; }
    public void setAccountType(String accountType) { this.accountType = accountType; }

    public void setUser(User user2) {
        this.user=user2;
    }

    public User getUser() {
        return user;
    }
}