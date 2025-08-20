package com.example.UPIPaymentSystem.Controller;

import com.example.UPIPaymentSystem.Entity.BankAccount;
import com.example.UPIPaymentSystem.Entity.User;
import com.example.UPIPaymentSystem.Repo_Service.BankAccountService;
import com.example.UPIPaymentSystem.Repo_Service.UserService;

import jakarta.validation.Valid;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Controller
public class AuthController
{
    @Autowired
    private UserService userService;
    
    @Autowired
    private BankAccountService bankAccountService;

    @GetMapping("/login")
    public String loginPage()
    {
        return "Authentication/login-page";
    }


    @GetMapping("/dashboard")
    public String dashboard(Model model)
    {
    	Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth != null && auth.isAuthenticated() ) 
        {

            UserDetails userDetails = (UserDetails) auth.getPrincipal();
            String mobile = userDetails.getUsername(); 
            User user = userService.findById(mobile).orElse(null);

	        if (user != null)
	        {
	            // Fetch bank accounts for the user
	            List<BankAccount> bankAccounts = bankAccountService.getAccountsByUser(mobile);
	
	            if (bankAccounts.isEmpty())
	            {
	                model.addAttribute("bankMessage", "No bank account linked. Please add one.");
	            }
	            else
	            {
	                model.addAttribute("bankList", bankAccounts);
	            }
	            
	            boolean flag=user.hasPrimaryBankAccount();
	            
	            model.addAttribute("hasPrimaryBankAccount", flag);
	            model.addAttribute("user", user);
	
	            // Redirect to dashboard after login
	            return "Authentication/dashboard";
	        }
        }
        return "Authentication/access-denied";
    }

    
    
    @GetMapping("/register")
    public String registerPage(Model model)
    {
    	model.addAttribute("user", new User());
        return "Authentication/register";
    }

    // Process registration
    
    @PostMapping("/register-process")
    public String registerUser(
            @Valid @ModelAttribute("user") User user,
            BindingResult result,
            Model model) 
    {
    	if (userService.existsById(user.getMobile()))
        {
            model.addAttribute("error", "Mobile number already registered");
            return "Authentication/register";
        }
        if (result.hasErrors()) {
            return "Authentication/register"; // return register page again
        }

        try {
            userService.save(user);
            model.addAttribute("message", "User registered successfully");
        } 
        catch (DataIntegrityViolationException e) 
        {
        	model.addAttribute("error", "Registration failed: " + e.getMessage());
            // check which field is duplicate
            if (e.getMessage().contains("mobile")) 
            {
                model.addAttribute("error", "Mobile number already registered");
            } 
            else if (e.getMessage().contains("upi_id")) 
            {
                model.addAttribute("error", "UPI ID already registered");
            } 
            else if (e.getMessage().contains("email")) 
            {
                model.addAttribute("error", "Email already registered");
            } 
            else 
            {
                model.addAttribute("error", "Duplicate entry found!");
            }
            return "Authentication/register";

        } 
        catch (Exception e) {
            model.addAttribute("error", "Registration failed: " + e.getMessage());
            return "Authentication/register";
        }

        return "Authentication/login-page";
    }
}
