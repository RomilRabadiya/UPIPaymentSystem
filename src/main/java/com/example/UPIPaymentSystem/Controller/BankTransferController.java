package com.example.UPIPaymentSystem.Controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.UPIPaymentSystem.AuthenticationServices.EmailService;
import com.example.UPIPaymentSystem.Entity.BankAccount;
import com.example.UPIPaymentSystem.Entity.Transaction;
import com.example.UPIPaymentSystem.Entity.User;
import com.example.UPIPaymentSystem.Entity.DTOs.TransferRequest;
import com.example.UPIPaymentSystem.Repo_Service.BankAccountService;
import com.example.UPIPaymentSystem.Repo_Service.TransactionService;
import com.example.UPIPaymentSystem.Repo_Service.UserService;

import jakarta.validation.Valid;

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
    
    @Autowired
    private EmailService emailService;
    
    @GetMapping("/bank/transfer")
    public String bankTransfer(@RequestParam("accountId") String accountNumber, Model model)
    {
        // find the source account
        BankAccount bankAccount = bankAccountService.findByAccountNumber(accountNumber);

        if (bankAccount == null)
        {
            model.addAttribute("error", "Account not found!");
            return "Authentication/dashboard";
        }

        // add account + empty dto for validation
        model.addAttribute("bankAccount", bankAccount);
        model.addAttribute("transferRequest", new TransferRequest());

        return "Bank/BankTransfer/bank-transfer";
    }

    @PostMapping("/bank/transfer/process")
    public String processBankTransfer(
            @RequestParam("fromAccountNumber") String fromAccountNumber,

            @Valid @ModelAttribute("transferRequest") TransferRequest transferRequest,
            BindingResult bindingResult,

            Model model)
    {
        // find the sender account
        BankAccount fromAccount = bankAccountService.findByAccountNumber(fromAccountNumber);

        if (fromAccount == null)
        {
            model.addAttribute("error", "Sender account not found!");
            return "Bank/BankTransfer/bank-transfer";
        }

        // if validation failed → return form with errors
        if (bindingResult.hasErrors())
        {
            model.addAttribute("bankAccount", fromAccount);
            return "Bank/BankTransfer/bank-transfer";
        }

        // get values from DTO
        String toAccountNumber = transferRequest.getToAccountNumber();
        String toIfscCode = transferRequest.getToIfscCode();
        BigDecimal amount = transferRequest.getAmount();

        // find the recipient
        BankAccount toAccount = bankAccountService.findByAccountNumberAndIfscCode(toAccountNumber, toIfscCode);

        if (toAccount == null)
        {
            model.addAttribute("bankAccount", fromAccount);
            model.addAttribute("error", "Recipient account not found!");
            return "Bank/BankTransfer/bank-transfer";
        }

        // same account check
        if (fromAccount.getAccountNumber().equals(toAccount.getAccountNumber()))
        {
            model.addAttribute("bankAccount", fromAccount);
            model.addAttribute("error", "From and To account cannot be the same.");
            return "Bank/BankTransfer/bank-transfer";
        }

        // check balance
        if (fromAccount.getBalance().compareTo(amount) < 0)
        {
            model.addAttribute("bankAccount", fromAccount);
            model.addAttribute("error", "Insufficient balance!");
            return "Bank/BankTransfer/bank-transfer";
        }

        // pass valid details → go to PIN page
        model.addAttribute("fromAccountNumber", fromAccountNumber);
        model.addAttribute("toAccountNumber", toAccountNumber);
        model.addAttribute("toIfscCode", toIfscCode);
        model.addAttribute("amount", amount);

        return "Bank/BankTransfer/bank-transfer-pin";
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
        
        
        emailService.sendTransactionEmail(
        		fromAccount.getUser().getEmail(),
        		fromAccount.getUser().getName(),
                "Bank Transfer",
                fromAccount.getAccountNumber(),       // auto-masked
                toAccount.getAccountNumber(),       // auto-masked
                amount.toString(),
                transaction.getId().toString()
        );

        
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
}