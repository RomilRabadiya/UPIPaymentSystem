package com.example.UPIPaymentSystem.Repo_Service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.UPIPaymentSystem.Entity.BankAccount;
import com.example.UPIPaymentSystem.Entity.Transaction;
import com.example.UPIPaymentSystem.Entity.User;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class TransactionService {

    @Autowired
    private TransactionRepository transactionRepository;

    public Transaction save(Transaction transaction) {
        transaction.setTimestamp(LocalDateTime.now());
        return transactionRepository.save(transaction);
    }
    
    public List<Transaction> getSentTransactions(Long accountId) 
    {
        return transactionRepository.findByFromAccount_Id(accountId);
    }

    public List<Transaction> getReceivedTransactions(Long accountId) 
    {
        return transactionRepository.findByToAccount_Id(accountId);
    }

    public List<Transaction> getAllTransactions() 
    {
        return transactionRepository.findAll();
    }

    
    public List<Transaction> getTransactionsForAccount(String accountNumber) 
    {
        return transactionRepository
                .findByFromAccount_AccountNumberOrToAccount_AccountNumber(accountNumber, accountNumber);
    }
    
    
    
    public List<Transaction> getTransactionsForUser(User user) 
    {
        List<Transaction> transactions = new ArrayList<>();
        
        if (user.getAccounts() != null) 
        {
            for (BankAccount account : user.getAccounts()) 
            {
                transactions.addAll
                (
                    transactionRepository.findByFromAccount_AccountNumberOrToAccount_AccountNumber
                    (
                            account.getAccountNumber(),
                            account.getAccountNumber()
                    )
                );
            }
        }
        return transactions;
    }

}