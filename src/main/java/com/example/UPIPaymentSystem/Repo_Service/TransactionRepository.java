package com.example.UPIPaymentSystem.Repo_Service;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.UPIPaymentSystem.Entity.Transaction;

import java.time.LocalDateTime;
import java.util.List;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {

	@Query("SELECT t FROM Transaction t "
	         + "WHERE (t.fromAccount.user.mobile = :mobile OR t.toAccount.user.mobile = :mobile) "
	         + "AND t.timestamp >= :from AND t.timestamp < :to "
	         + "ORDER BY t.timestamp ASC")
	    List<Transaction> findByUserMobileAndTimestampBetween(
	            @Param("mobile") String mobile,
	            @Param("from") LocalDateTime from,
	            @Param("to") LocalDateTime to);
	
    // If you want to query by BankAccount PK (Long id)
    List<Transaction> findByFromAccount_Id(Long accountId);
    List<Transaction> findByToAccount_Id(Long accountId);

    // If you want to query by BankAccount accountNumber (String field)
    List<Transaction> findByFromAccount_AccountNumberOrToAccount_AccountNumber(String fromAccountNumber, String toAccountNumber);
}
