package com.example.UPIPaymentSystem.Repo_Service;

import org.springframework.data.jpa.repository.JpaRepository;
import com.example.UPIPaymentSystem.Entity.Transaction;
import java.util.List;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    // If you want to query by BankAccount PK (Long id)
    List<Transaction> findByFromAccount_Id(Long accountId);
    List<Transaction> findByToAccount_Id(Long accountId);

    // If you want to query by BankAccount accountNumber (String field)
    List<Transaction> findByFromAccount_AccountNumberOrToAccount_AccountNumber(String fromAccountNumber, String toAccountNumber);
}
