package com.example.UPIPaymentSystem.Entity.DTOs;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public class TransferRequest {

    @NotBlank(message = "Recipient account number is required.")
    @Pattern(regexp = "\\d{8,20}", message = "Account number must be 8–20 digits.")
    private String toAccountNumber;

    @NotBlank(message = "IFSC Code is required.")
    @Pattern(regexp = "^[A-Z]{4}0[A-Z0-9]{6}$", message = "Invalid IFSC format.")
    private String toIfscCode;

    @NotNull(message = "Amount is required.")
    @DecimalMin(value = "1.00", message = "Minimum transfer amount is ₹1.")
    private BigDecimal amount;

	public String getToAccountNumber() {
		return toAccountNumber;
	}

	public void setToAccountNumber(String toAccountNumber) {
		this.toAccountNumber = toAccountNumber;
	}

	public String getToIfscCode() {
		return toIfscCode;
	}

	public void setToIfscCode(String toIfscCode) {
		this.toIfscCode = toIfscCode;
	}

	public BigDecimal getAmount() {
		return amount;
	}

	public void setAmount(BigDecimal amount) {
		this.amount = amount;
	}

    // getters & setters
}
