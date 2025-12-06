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
public class BankTransferController {

    @Autowired
    private BankAccountService bankAccountService;

    @Autowired
    private UserService userService;
    
    @Autowired
    private TransactionService transactionService;
    
    @GetMapping("/bank/transfer")
    public String bankTransfer(@RequestParam("accountId") String accountNumber, Model model)
    {
        BankAccount bankAccount = bankAccountService.findByAccountNumber(accountNumber);

        model.addAttribute("bankAccount", bankAccount);

    	return "Bank/BankTransfer/bank-transfer";
    }
    
    @PostMapping("/bank/transfer/process")
    public String processBankTransfer(
            @RequestParam("fromAccountNumber") String fromAccountNumber,
            @RequestParam("toAccountNumber") String toAccountNumber,
            @RequestParam("toIfscCode") String toIfscCode,
            @RequestParam("amount") BigDecimal amount,
            Model model) 
    {

        // 1. Find sender account
        BankAccount fromAccount = bankAccountService.findByAccountNumber(fromAccountNumber);

        if (fromAccount == null) 
        {
        	model.addAttribute("bankAccount", fromAccount);
            model.addAttribute("error", "Sender account not found!");
            return "Bank/BankTransfer/bank-transfer";
        }

        // 2. Find recipient account by account number + IFSC
        BankAccount toAccount = bankAccountService.findByAccountNumberAndIfscCode(toAccountNumber, toIfscCode);

        if (toAccount == null) 
        {
            model.addAttribute("bankAccount", fromAccount);
            model.addAttribute("error", "Recipient account not found!");
            return "Bank/BankTransfer/bank-transfer";
        }
        
        if(fromAccount.getAccountNumber().equals(toAccount.getAccountNumber()))
        {
        	model.addAttribute("bankAccount", fromAccount);
            model.addAttribute("error", "From And To Account is same");
            return "Bank/BankTransfer/bank-transfer";
        }

        // 3. Check balance
        if (fromAccount.getBalance().compareTo(amount) < 0) 
        {
            model.addAttribute("bankAccount", fromAccount);
            model.addAttribute("error", "Insufficient balance!");
            return "Bank/BankTransfer/bank-transfer";
        }
        
        // Pass details to PIN entry page
        model.addAttribute("fromAccountNumber", fromAccountNumber);
        model.addAttribute("toAccountNumber", toAccountNumber);
        model.addAttribute("toIfscCode", toIfscCode);
        model.addAttribute("amount", amount);

        return "Bank/BankTransfer/bank-transfer-pin";  // PIN entry page
    }
    
    @PostMapping("/bank/transfer/confirm")
    public String confirmBankTransfer(
            @RequestParam("fromAccountNumber") String fromAccountNumber,
            @RequestParam("toAccountNumber") String toAccountNumber,
            @RequestParam("toIfscCode") String toIfscCode,
            @RequestParam("amount") BigDecimal amount,
            
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
            User user = bankAccountService.getUserFromAuthentication();
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
	            model.addAttribute("toIfscCode", toIfscCode);
	            model.addAttribute("amount", amount);
	            model.addAttribute("error", "Invalid Transaction PIN!");
	            return "Bank/BankTransfer/bank-transfer-pin";  // stay on PIN page
	        }

	        // 3. Perform transfer
	        BankAccount fromAccount = bankAccountService.findByAccountNumber(fromAccountNumber);
	        BankAccount toAccount = bankAccountService.findByAccountNumberAndIfscCode(toAccountNumber, toIfscCode);

	        fromAccount.setBalance(fromAccount.getBalance().subtract(amount));
	        toAccount.setBalance(toAccount.getBalance().add(amount));

	        bankAccountService.update(fromAccount);
	        bankAccountService.update(toAccount);

	        // 4. Success message
	        model.addAttribute("bankAccount", fromAccount);
	        
	        Transaction transaction = new Transaction(
	                fromAccount,
	                toAccount,
	                amount,
	                "SUCCESS",
	                LocalDateTime.now(),
	                "Bank Transfer to " + toAccount.getAccountNumber()
	        );
	        transactionService.save(transaction);
	        
	        // PROBLEM: Direct POST response
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