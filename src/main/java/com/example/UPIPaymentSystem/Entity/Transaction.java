package com.example.UPIPaymentSystem.Entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "transactions")
public class Transaction
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Sender bank account
    @ManyToOne
    @JoinColumn(name = "from_account_id")
    private BankAccount fromAccount;

    // Receiver bank account
    @ManyToOne
    @JoinColumn(name = "to_account_id")
    private BankAccount toAccount;

    // Transaction amount
    private BigDecimal amount;

    // Status of the transaction (SUCCESS, FAILED, PENDING)
    private String status;

    // Time of the transaction
    private LocalDateTime timestamp;

    // Optional description (e.g., "Rent Payment", "UPI QR Payment")
    private String description;

    public Transaction() {}

    public Transaction(BankAccount fromAccount, BankAccount toAccount, BigDecimal amount,
                       String status, LocalDateTime timestamp, String description)
    {
        this.fromAccount = fromAccount;
        this.toAccount = toAccount;
        this.amount = amount;
        this.status = status;
        this.timestamp = timestamp;
        this.description = description;
    }
    
    

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public BankAccount getFromAccount() { return fromAccount; }
    public void setFromAccount(BankAccount fromAccount) { this.fromAccount = fromAccount; }

    public BankAccount getToAccount() { return toAccount; }
    public void setToAccount(BankAccount toAccount) { this.toAccount = toAccount; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}
