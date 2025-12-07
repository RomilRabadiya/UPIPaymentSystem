package com.example.UPIPaymentSystem.Controller;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.UPIPaymentSystem.AuthenticationServices.EmailService;
import com.example.UPIPaymentSystem.Entity.BankAccount;
import com.example.UPIPaymentSystem.Entity.Transaction;
import com.example.UPIPaymentSystem.Entity.User;
import com.example.UPIPaymentSystem.Repo_Service.BankAccountService;
import com.example.UPIPaymentSystem.Repo_Service.TransactionService;

@Controller
public class QrCodePayment {
	
	@Autowired
	BankAccountService bankAccountService;
	@Autowired
	TransactionService transactionService;
	@Autowired
	EmailService emailService;
	
	@GetMapping("/QrCodePayment")
	public String QrCodeMethod()
	{
		return "Bank/QrCodePayment/scan-qr";
	}
	
	// ============================
    // POST: Accept Scanned UPI Data
    // ============================
    @PostMapping("/QrCodePayment/submit-upi")
    public String submitUpi(
            @RequestParam("upi") String upiId,
            Model model) 
    {
        User user=bankAccountService.getUserFromAuthentication();
        BankAccount fromAccount=bankAccountService.findByMobileNumber(user.getMobile());
        
        if(fromAccount==null)
        {
        	model.addAttribute("error","You not have any primary bank Account , So add Bank Account");
        	return "redirect:/dashboard";
        }
        
        BankAccount toAccount=bankAccountService.findByUpiId(upiId);
        
        if(toAccount==null)
        {
        	model.addAttribute("error","Failed to find Account From QR Code Try Again");
        	return "Bank/QrCodePayment/scan-qr"; 
        }
        
        
        if (fromAccount.getUser().equals(toAccount.getUser()))
        {
        	//Same Account
            model.addAttribute("error", "Invalid Transaction: Same UPI So Please Scan Different QR Code");
            return "Bank/QrCodePayment/scan-qr"; 
        }
        
        model.addAttribute("toAccount",toAccount);
        model.addAttribute("fromAccount",fromAccount);
        
        // You can redirect to any confirmation/payment page
        return "Bank/QrCodePayment/payment-form";
    }
    
    
    @PostMapping("/QrCodePayment/confirm-payment")
    public String confirmPayment(
            @RequestParam("fromAcc") String fromAcc,
            @RequestParam("toAcc") String toAcc,
            @RequestParam("amount") double amount,
            Model model) {

        model.addAttribute("fromAcc", fromAcc);
        model.addAttribute("toAcc", toAcc);
        model.addAttribute("amount", amount);

        return "Bank/QrCodePayment/enter-pin";
    }
    
    @PostMapping("/QrCodePayment/verify-pin")
    public String verifyPin(
            @RequestParam("fromAcc") String fromAcc,
            @RequestParam("toAcc") String toAcc,
            @RequestParam("amount")BigDecimal  amount,
            @RequestParam("pin") String pin,
            RedirectAttributes redirect,
            Model model) {

        BankAccount sender = bankAccountService.findByAccountNumber(fromAcc);
        BankAccount receiver = bankAccountService.findByAccountNumber(toAcc);

        // 1. Validate PIN
        if (!sender.getUser().getPin().equals(pin)) 
        {
            redirect.addFlashAttribute("error", "Invalid PIN. Try again.");
            redirect.addFlashAttribute("fromAcc", fromAcc);
            redirect.addFlashAttribute("toAcc", toAcc);
            redirect.addFlashAttribute("amount", amount);
            return "redirect:/QrCodePayment/enter-pin";
        }

        // 2. Insufficient balance
        if (sender.getBalance().compareTo(amount) < 0) 
        {
        	redirect.addFlashAttribute("error", "Invalid PIN. Try again.");
            redirect.addFlashAttribute("fromAcc", fromAcc);
            redirect.addFlashAttribute("toAcc", toAcc);
            redirect.addFlashAttribute("amount", amount);
            return "redirect:/QrCodePayment/enter-pin";
        }
        
        // 3. Do transfer
        sender.setBalance(sender.getBalance().subtract(amount));
        receiver.setBalance(receiver.getBalance().add(amount));

        bankAccountService.update(sender);
        bankAccountService.update(receiver);

        // 4. Success
        model.addAttribute("bankAccount", sender);
        
        Transaction transaction = new Transaction(
        		sender,
        		receiver,
                amount,
                "SUCCESS",
                LocalDateTime.now(),
                "QR Code Payment"
        );
        transactionService.save(transaction);
        
        emailService.sendTransactionEmail(
                sender.getUser().getEmail(),
                sender.getUser().getName(),
                "QR Code Payment",
                sender.getAccountNumber(),
                receiver.getAccountNumber(),
                amount.toString(),
                transaction.getId().toString()
        );


        
        // PROBLEM: Direct POST response  (Disadvantage of POST Method)
        // model.addAttribute("success", "Transfer completed");
        // return "Bank/bank-operation";
        
        // IMPORTANT: Use RedirectAttributes to pass success message
        // This prevents message duplication on page reload
        redirect.addFlashAttribute("success", "₹" + amount + " transferred successfully to " + receiver);

        // SECURITY FIX: Redirect after POST to prevent duplicate transactions on browser reload
        // Pattern: POST-Redirect-GET (PRG) - prevents form resubmission
        return "redirect:/bank/success?accountId=" + sender;
    }

}