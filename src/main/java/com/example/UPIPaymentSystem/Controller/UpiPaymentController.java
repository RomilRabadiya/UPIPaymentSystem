package com.example.UPIPaymentSystem.Controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.UPIPaymentSystem.Entity.BankAccount;
import com.example.UPIPaymentSystem.Entity.Transaction;
import com.example.UPIPaymentSystem.Entity.User;
import com.example.UPIPaymentSystem.Repo_Service.BankAccountService;
import com.example.UPIPaymentSystem.Repo_Service.TransactionService;
import com.example.UPIPaymentSystem.Repo_Service.UserService;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Controller
public class UpiPaymentController {

    @Autowired
    private BankAccountService bankAccountService;

    @Autowired
    private UserService userService;
    
    @Autowired
    private TransactionService transactionService;
    
    @GetMapping("/bank/upi")
    public String showUpiPaymentPage(@RequestParam("accountId") String accountId, Model model) {
        
        // Load the bank account by account number
        BankAccount bankAccount = bankAccountService.findByAccountNumber(accountId);

        // Pass account to the UPI payment page
        model.addAttribute("bankAccount", bankAccount);

        return "Bank/upi-payment"; // Thymeleaf template for UPI
    }
    
    @PostMapping("/bank/upi/process")
    public String processUpiRequest(
            @RequestParam("fromAccountNumber") String fromAccountNumber,
            @RequestParam("upiId") String upiId,
            @RequestParam("amount") BigDecimal amount,
            Model model) {
    	
    	// 1. Find sender account
        BankAccount fromAccount = bankAccountService.findByAccountNumber(fromAccountNumber);

        if (fromAccount == null) 
        {
        	model.addAttribute("bankAccount", fromAccount);
            model.addAttribute("error", "Sender account not found!");
            return "Bank/upi-payment";
        }

        // 2. Check balance
        if (fromAccount.getBalance().compareTo(amount) < 0) 
        {
            model.addAttribute("bankAccount", fromAccount);
            model.addAttribute("error", "Insufficient balance!");
            return "Bank/upi-payment";
        }
        
        //MMIMP    :   Money Transfer to user Primary BankAccount Of User
        BankAccount toAccount = bankAccountService.findByUpiId(upiId);
        
        if(toAccount==null)
        {
        	model.addAttribute("bankAccount", fromAccount);
            model.addAttribute("error", "Recipient account not found! Or May be Recipient account Not have primary Acoount");
            return "Bank/upi-payment";
        }
        
        if (fromAccount.getUser().equals(toAccount.getUser()))
        {
        	//Same Account
        	model.addAttribute("bankAccount", fromAccount);
            model.addAttribute("error", "Invalid Transaction: Same UPI So Please Enter Different UPI or Perfer Bank Transfer to Transact in same upi One Bank To Another Bank.");
            return "Bank/upi-payment";  // Transaction happen in Same Account
        }
    	
        // Pass details to PIN page
        model.addAttribute("fromAccountNumber", fromAccountNumber);
        model.addAttribute("toAccountNumber", toAccount.getAccountNumber());
        model.addAttribute("amount", amount);
        model.addAttribute("upiId", upiId);

        return "Bank/upi-pin";
    }
    
    @PostMapping("/bank/upi/confirm")
    public String confirmUpiPayment(
            @RequestParam("fromAccountNumber") String fromAccountNumber,
            @RequestParam("toAccountNumber") String toAccountNumber,
            @RequestParam("amount") BigDecimal amount,
            @RequestParam("upiId") String upiId,
            @RequestParam("pin") String pin,
            Model model,
            RedirectAttributes redirectAttributes) 
    {
    	// 1. Get authenticated user
    	Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth != null && auth.isAuthenticated()) {

            UserDetails userDetails = (UserDetails) auth.getPrincipal();
            
            String mobile = userDetails.getUsername();

            // Find user by mobile
            User user = userService.findById(mobile).orElse(null);
            if (user == null) 
            {
                model.addAttribute("error", "User not found");
                return "error";
            }

	        // 2. Validate PIN
	        if (!user.getPin().equals(pin)) 
	        {
	        	//inValidate PIN
	        	model.addAttribute("fromAccountNumber", fromAccountNumber);
	            model.addAttribute("toAccountNumber", toAccountNumber);
	            model.addAttribute("amount", amount);
	            model.addAttribute("error", "Invalid Transaction PIN!");
	            return "Bank/upi-pin";  // stay on PIN page
	        }

	        BankAccount fromAccount = bankAccountService.findByAccountNumber(fromAccountNumber);
	        BankAccount toAccount = bankAccountService.findByAccountNumber(toAccountNumber);
	        
	        // 3. Do transfer
	        fromAccount.setBalance(fromAccount.getBalance().subtract(amount));
	        toAccount.setBalance(toAccount.getBalance().add(amount));
	
	        bankAccountService.update(fromAccount);
	        bankAccountService.update(toAccount);
	
	        // 4. Success
	        model.addAttribute("bankAccount", fromAccount);
	        
	        Transaction transaction = new Transaction(
	                fromAccount,
	                toAccount,
	                amount,
	                "SUCCESS",
	                LocalDateTime.now(),
	                "UPI Payment to " + upiId
	        );
	        transactionService.save(transaction);
	        
	        // PROBLEM: Direct POST response  (Disadvantage of POST Method)
	        // model.addAttribute("success", "Transfer completed");
	        // return "Bank/bank-operation";
	        
	        // IMPORTANT: Use RedirectAttributes to pass success message
	        // This prevents message duplication on page reload
	        redirectAttributes.addFlashAttribute("success", "₹" + amount + " transferred successfully to " + toAccountNumber);

	        // SECURITY FIX: Redirect after POST to prevent duplicate transactions on browser reload
	        // Pattern: POST-Redirect-GET (PRG) - prevents form resubmission
	        return "redirect:/bank/success?accountId=" + fromAccountNumber;
        }
	        
        return "Authentication/access-denied";
    }
}