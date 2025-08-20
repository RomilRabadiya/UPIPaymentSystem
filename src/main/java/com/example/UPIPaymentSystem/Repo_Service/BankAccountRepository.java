package com.example.UPIPaymentSystem.Repo_Service;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.UPIPaymentSystem.Entity.BankAccount;
import com.example.UPIPaymentSystem.Entity.User;

import java.util.List;
import java.util.Optional;

public interface BankAccountRepository extends JpaRepository<BankAccount, Long>
{
    List<BankAccount> findByUser_Mobile(String mobile);
    Optional<BankAccount> findByAccountNumber(String accountNumber);
    BankAccount findByAccountNumberAndIfscCode(String accountNumber, String ifscCode);
}
