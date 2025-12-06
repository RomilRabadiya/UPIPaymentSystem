package com.example.UPIPaymentSystem.Entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.List;

@Entity
@Table(name = "users")
public class User {
    // ---------------- MOBILE NUMBER ----------------
    @Id
    @NotBlank(message = "Mobile number is required")
    @Size(min = 10, max = 10, message = "Mobile number must be exactly 10 digits")
    @Pattern(regexp = "^[6-9]\\d{9}$", message = "Mobile number must start with 6–9 and be 10 digits")
    @Column(nullable = false, unique = true, length = 10)
    private String mobile;

    // ---------------- UPI ID ----------------
    @NotBlank(message = "UPI ID is required")
    @Pattern(
        regexp = "^[a-zA-Z0-9._-]{3,50}@[a-zA-Z]{3,20}$",
        message = "UPI ID must be in format: name@bank (example: rahul@upi)"
    )
    @Column(unique = true, nullable = false)
    private String upiId;

    // ---------------- NAME ----------------
    @NotBlank(message = "Name is required")
    @Size(min = 2, max = 50, message = "Name must be 2–50 characters long")
    @Pattern(regexp = "^[A-Za-z ]+$", message = "Name must contain only alphabets and spaces")
    private String name;

    // ---------------- EMAIL ----------------
    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    @Size(max = 100, message = "Email cannot exceed 100 characters")
    private String email;

    // ---------------- PIN ----------------
    @NotBlank(message = "PIN is required")
    @Pattern(regexp = "^[0-9]{4,6}$", message = "PIN must be 4–6 digits")
    private String pin;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<BankAccount> accounts;

    public User() {}

    public User(String mobile, String upiId, String name, String email, String pin) {
        this.mobile = mobile;
        this.upiId = upiId;
        this.name = name;
        this.email = email;
        this.pin = pin;
    }

    // ----- CUSTOM METHODS -----

    public BankAccount getAccountByNumber(String accountNumber) {
        if (accounts == null) return null;

        return accounts.stream()
                .filter(a -> a.getAccountNumber().equals(accountNumber))
                .findFirst()
                .orElse(null);
    }

    public boolean hasPrimaryBankAccount() {
        if (accounts == null) return false;

        return accounts.stream().anyMatch(BankAccount::isPrimaryAccount);
    }

    // ----- GETTERS & SETTERS -----

    public String getMobile() { return mobile; }
    public void setMobile(String mobile) { this.mobile = mobile; }

    public String getUpiId() { return upiId; }
    public void setUpiId(String upiId) { this.upiId = upiId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPin() { return pin; }
    public void setPin(String pin) { this.pin = pin; }

    public List<BankAccount> getAccounts() { return accounts; }
    public void setAccounts(List<BankAccount> accounts) { this.accounts = accounts; }
}
