package com.example.UPIPaymentSystem.Controller;

import com.example.UPIPaymentSystem.Entity.Transaction;
import com.example.UPIPaymentSystem.Entity.User;
import com.example.UPIPaymentSystem.Repo_Service.BankAccountService;
import com.example.UPIPaymentSystem.Repo_Service.TransactionService;
import com.example.UPIPaymentSystem.Repo_Service.UserService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class TransactionController 
{
	@Autowired
	private BankAccountService bankAccountService;
    @Autowired
    private TransactionService transactionService;
    
    @Autowired
    private UserService userService;

    // Show all transactions
    @GetMapping("/transactions")
    public String showUserTransactions(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth != null && auth.isAuthenticated()) {
            UserDetails userDetails = (UserDetails) auth.getPrincipal();
            String mobile = userDetails.getUsername();
            
			// find logged-in user
            User user = bankAccountService.getUserFromAuthentication();
            if (user == null) {
                model.addAttribute("error", "User not found!");
                return "error";
            }

            // fetch only user's transactions
            List<Transaction> transactions = transactionService.getTransactionsForUser(user);

            model.addAttribute("transactions", transactions);
            return "Bank/Transactions/transaction-list";  // same Thymeleaf table
        }

        return "Authentication/access-denied";
    }
    
    
    @GetMapping("/bank/transactions/{accountNumber}")
    public String getTransactions(@PathVariable String accountNumber, Model model) {
        List<Transaction> transactions = transactionService.getTransactionsForAccount(accountNumber);

        List<Map<String, Object>> formattedTransactions = transactions.stream().map(tx -> {
            Map<String, Object> map = new HashMap<>();
            map.put("timestamp", tx.getTimestamp());

            // If account is sender → negative, else positive
            if (tx.getFromAccount().getAccountNumber().equals(accountNumber)) {
                map.put("amount", "-₹" + tx.getAmount());
            } else {
                map.put("amount", "+₹" + tx.getAmount());
            }

            map.put("description", tx.getDescription());
            map.put("status", tx.getStatus());
            return map;
        }).toList();

        model.addAttribute("transactions", formattedTransactions);
        model.addAttribute("accountNumber", accountNumber);

        return "Bank/Transactions/transactions-OnHome"; // thymeleaf template
    }


}