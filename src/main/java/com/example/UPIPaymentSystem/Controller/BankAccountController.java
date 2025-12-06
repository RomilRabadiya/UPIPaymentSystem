package com.example.UPIPaymentSystem.Controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import com.example.UPIPaymentSystem.Entity.BankAccount;
import com.example.UPIPaymentSystem.Entity.Transaction;
import com.example.UPIPaymentSystem.Entity.User;
import com.example.UPIPaymentSystem.Repo_Service.BankAccountService;
import com.example.UPIPaymentSystem.Repo_Service.TransactionService;
import com.example.UPIPaymentSystem.Repo_Service.UserService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class BankAccountController {

    @Autowired
    private BankAccountService bankAccountService;

    @Autowired
    private UserService userService;
    
    @Autowired
    private TransactionService transactionService;

    @GetMapping("/add-bank")
    public String showAddBankForm(Model model) {
        model.addAttribute("bankAccount", new BankAccount());
        return "Bank/AddBank/add-bank-form";
    }

    @PostMapping("/add-bank-process")
    public String saveBankAccount(@ModelAttribute BankAccount bankAccount, Model model) {
    		User user=bankAccountService.getUserFromAuthentication();
            String mobile=user.getMobile();

            //Check if this account is already linked to the user
            BankAccount existingAccount = bankAccountService.findByAccountNumber(
                    bankAccount.getAccountNumber()
            );
            
            if (existingAccount != null) 
            {
                model.addAttribute("error", "This bank account is already linked to your profile.");

                List<BankAccount> bankAccounts = bankAccountService.getAccountsByUser(mobile);
                model.addAttribute("bankList", bankAccounts);
                model.addAttribute("user", user);
                
                return "Authentication/dashboard"; // Show dashboard with error
            }

            // Link bank account to user
            bankAccount.setUser(user);
            bankAccountService.save(bankAccount);

            // Fetch updated accounts
            List<BankAccount> bankAccounts = bankAccountService.getAccountsByUser(mobile);
            model.addAttribute("bankList", bankAccounts);
            model.addAttribute("user", user);

            return "Authentication/dashboard";
    }
    
    @PostMapping("/bank/set-primary")
    public String setPrimaryBank(@RequestParam String accountId,Model model) 
    {
    	User user=bankAccountService.getUserFromAuthentication();
        String mobile=user.getMobile();
        boolean updated = bankAccountService.setPrimaryAccount(mobile, accountId);
	    return "redirect:/dashboard";	    
    }
    
    @GetMapping("/bank/process")
    public String processBank(@RequestParam("accountId") String accountNumber, Model model) {
        // Use the AccountNumber to fetch bank account details from DB
        BankAccount bankAccount = bankAccountService.findByAccountNumber(accountNumber);

        model.addAttribute("bankAccount", bankAccount);
        
        List<Transaction> transactions = transactionService.getTransactionsForAccount(accountNumber);

        List<Map<String, Object>> formattedTransactions = 
    		transactions.stream()
            .sorted((tx1, tx2) -> tx2.getTimestamp().compareTo(tx1.getTimestamp()))
    		.map(tx -> {
    			
	            Map<String, Object> map = new HashMap<>();
	            map.put("timestamp", tx.getTimestamp());
	
	            // If account is sender → negative, else positive
	            if (tx.getFromAccount().getAccountNumber().equals(accountNumber)) 
	            {
	                map.put("amount", "-₹" + tx.getAmount());
	            } 
	            else {
	                map.put("amount", "+₹" + tx.getAmount());
	            }
	
	            map.put("description", tx.getDescription());
	            map.put("status", tx.getStatus());
	            return map;
	            
	        }).toList();

        model.addAttribute("transactions", formattedTransactions);

        // Return the page to display/operate on this account
        return "Bank/bank-operation";
    }
    
    //For SECURITY FIX Of Redirect after POST to prevent duplicate transactions on browser reload
    @GetMapping("/bank/success")
    public String showTransferSuccess(@RequestParam("accountId") String accountNumber, Model model) {
        // Load the account to show updated balance
        BankAccount bankAccount = bankAccountService.findByAccountNumber(accountNumber);
        
        if (bankAccount == null) {
            return "redirect:/dashboard";
        }
        
        model.addAttribute("bankAccount", bankAccount);
        
        //for Display Transaction
        List<Transaction> transactions = transactionService.getTransactionsForAccount(accountNumber);

        List<Map<String, Object>> formattedTransactions = 
        		transactions.stream()
                .sorted((tx1, tx2) -> tx2.getTimestamp().compareTo(tx1.getTimestamp()))
        		.map(tx -> {
        			
		            Map<String, Object> map = new HashMap<>();
		            map.put("timestamp", tx.getTimestamp());
		
		            // If account is sender → negative, else positive
		            if (tx.getFromAccount().getAccountNumber().equals(accountNumber)) 
		            {
		                map.put("amount", "-₹" + tx.getAmount());
		            } 
		            else {
		                map.put("amount", "+₹" + tx.getAmount());
		            }
		
		            map.put("description", tx.getDescription());
		            map.put("status", tx.getStatus());
		            return map;
		            
		        }).toList();

        model.addAttribute("transactions", formattedTransactions);
        
        return "Bank/bank-operation"; // or create a dedicated success page
    }
}