package com.example.UPIPaymentSystem.Entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.List;

@Entity
@Table(name = "users")
public class User
{
    @Id
    @NotBlank(message = "Mobile number is required")
    @Size(min = 10, max = 10, message = "Mobile number must be 10 digits")
    @Pattern(regexp = "^[6-9]\\d{9}$", message = "Invalid mobile number format")
    @Column(nullable = false, unique = true, length = 10)
    private String mobile;

    
    @NotBlank(message = "UPI ID is required")
//    validation in pattern
    @Pattern(regexp = "^[a-zA-Z0-9._-]+@[a-zA-Z]+$", 
             message = "UPI ID must be in format: name@bank")
    @Column(unique = true, nullable = false)
    private String upiId;

    
    @NotBlank(message = "Name is required")
//    validate the size
    @Size(min = 2, max = 50, message = "Name must be 2–50 characters")
    private String name;

    
    @NotBlank(message = "Email is required")
//    ensures it matches a standard email pattern
    @Email(message = "Invalid email address")
    @Column(unique = true, nullable = false)
    private String email;

    
    @NotBlank(message = "PIN is required")
    @Pattern(regexp = "^[0-9]{3,6}$", message = "PIN must be 3–6 digits")
    private String pin;

    
    // One user can have multiple bank accounts
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<BankAccount> accounts;
    
    public User() {}

    public User(String mobile, String upiId, String name, String email, String pin)
    {
        this.mobile = mobile;
        this.upiId = upiId;
        this.name = name;
        this.email = email;
        this.pin = pin;
    }
    
    
    
    public BankAccount getAccountByNumber(String accountNumber) 
    {
        if (accounts == null) return null;

        for (BankAccount account : accounts) {
            if (account.getAccountNumber().equals(accountNumber)) {
                return account;
            }
        }
        return null; // if not found
    }
    
    public boolean hasPrimaryBankAccount()
    {
        if (accounts == null) return false;

        for (BankAccount account : accounts) 
        {
            if (account.isPrimaryAccount()) 
            {
                return true;
            }
        }
        return false; // if not found    	
    }
    
    

    // Getters & Setters
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
