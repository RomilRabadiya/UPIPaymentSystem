package com.example.UPIPaymentSystem.Controller;

import java.util.List;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import com.example.UPIPaymentSystem.AuthenticationServices.OtpService;
import com.example.UPIPaymentSystem.Entity.BankAccount;
import com.example.UPIPaymentSystem.Entity.User;
import com.example.UPIPaymentSystem.Entity.DTOs.LoginDto;
import com.example.UPIPaymentSystem.Repo_Service.BankAccountService;
import com.example.UPIPaymentSystem.Repo_Service.UserService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

@Controller
public class AuthController {

    private final UserService authService;
    private final OtpService otpService;
    private final BankAccountService bankAccountService;
    public AuthController(UserService authService, OtpService otpService,BankAccountService bankAccountService) {
        this.authService = authService;
        this.otpService = otpService;
        this.bankAccountService = bankAccountService;
    }
    
    @GetMapping("/dashboard")
    public String dashboard(Model model)
    {
    	User user=bankAccountService.getUserFromAuthentication();
    	String mobile=user.getMobile();
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

    // ================================
    // Show Register Page
    // ================================
    @GetMapping("/register")
    public String showRegisterForm(Model model) {
        model.addAttribute("user", new User());
        return "Authentication/register";
    }
    
	 // ================================
	 // Submit Register Form + Validation + Send OTP
	 // ================================
	 @PostMapping("/auth/register")
	 public String register(
	         @Valid @ModelAttribute("user") User user,
	         BindingResult result,
	         HttpSession session,
	         Model model) {
	
	     // 1. If validation fails → return to registration page
	     if (result.hasErrors()) {
	         return "Authentication/register";
	     }
	
	     // 2. If user already exists → show error
	     if (authService.existsByMobile(user.getMobile())) {
	         model.addAttribute("error", "Mobile number already registered");
	         return "Authentication/register";
	     }
	
	     if (authService.existsByUpiId(user.getUpiId())) {
	         model.addAttribute("error", "UPI ID already taken");
	         return "Authentication/register";
	     }
	
	     // 3. Store user temporarily in session
	     session.setAttribute("tempUser", user);
	
	     // 4. Create + send OTP
	     otpService.createAndSendOtp(session, user.getMobile(), user.getEmail());
	
	     // 5. Get timer
	     long remaining = otpService.getRemainingSeconds(session);
	
	     model.addAttribute("mobile", user.getMobile());
	     model.addAttribute("remaining", remaining);
	     model.addAttribute("email", user.getEmail());
	
	     return "Authentication/verify-otp"; // do NOT save yet
	 }
	
	 // ================================
	 // Resend OTP
	 // ================================
	 @PostMapping("/ResendOtp")
	 public String resendOtp(HttpSession session, Model model) 
	 {
	     User user = (User) session.getAttribute("tempUser");
	     if (user == null) {
	         model.addAttribute("error", "Session expired. Please register again.");
	         return "redirect:/register";
	     }
	
	     otpService.createAndSendOtp(session, user.getMobile(), user.getEmail());
	     long remaining = otpService.getRemainingSeconds(session);
	
	     model.addAttribute("mobile", user.getMobile());
	     model.addAttribute("remaining", remaining);
	     model.addAttribute("email", user.getEmail());
	
	     return "Authentication/verify-otp";
	 }
	
	 // ================================
	 // Verify OTP -> Activate User
	 // ================================
	 @PostMapping("/verify-registration-otp")
	 public String verifyRegistrationOtp(
	         @RequestParam String mobile,
	         @RequestParam String otp,
	         HttpSession session,
	         Model model) {
	
	     boolean ok = otpService.verifyOtp(session, mobile, otp);
	
	     if (!ok) {
	         model.addAttribute("error", "Invalid or expired OTP");
	         model.addAttribute("mobile", mobile);
	         return "Authentication/verify-otp";
	     }
	
	     // OTP verified → now save user
	     User user = (User) session.getAttribute("tempUser");
	     if (user == null) {
	         model.addAttribute("error", "Session expired. Please register again.");
	         return "redirect:/register";
	     }
	
	     authService.save(user);   // Save only after OTP verification
	     session.removeAttribute("tempUser"); // Clean session
	
	     model.addAttribute("user", user);
	     return "Authentication/dashboard";
	 }

    
    
    
    @PostMapping("/login")
    public String loginUser(@Valid @ModelAttribute("loginDto") LoginDto loginDto,
                            BindingResult bindingResult,
                            HttpSession session,
                            Model model) 
    {

        if (bindingResult.hasErrors()) 
        {
            return "Authentication/login";   // return form with validation errors
        }

        try 
        {
            // Login and get JWT token
            String jwtToken = authService.login(loginDto.getMobile(), loginDto.getPin());
            
            User user = authService.getUserByMobile(loginDto.getMobile());
            model.addAttribute("user", user);
            
            session.setAttribute("jwtToken",jwtToken);
            
            return "redirect:dashboard";
        } 
        catch (Exception e) 
        {
            model.addAttribute("error", e.getMessage());
            return "Authentication/login";
        }
    }

    @GetMapping("/login")
    public String showLoginForm(Model model) {
        model.addAttribute("loginDto", new LoginDto());
        return "Authentication/login";
    }
    
    // ================================
    // Test Endpoint - Requires JWT Authentication
    // ================================
    @GetMapping("/Test")
    public String testEndpoint(Model model)
    {
        // Get authenticated user information from JWT token
        // The JwtAuthenticationFilter sets this in SecurityContext after validating the token
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication != null && authentication.isAuthenticated()) 
        {
            // The principal is the mobile number (subject from JWT token)
            String mobile = authentication.getName();
            
            // Get user details from database
            try {
                User user = authService.getUserByMobile(mobile);
                model.addAttribute("user", user);
                model.addAttribute("message", "JWT Authentication successful! Welcome, " + user.getName());
                model.addAttribute("mobile", mobile);
            } catch (Exception e) {
                model.addAttribute("error", "User not found: " + e.getMessage());
            }
        } 
        else {
            model.addAttribute("error", "Not authenticated");
        }
        
        return "Authentication/Test"; 
    }
}
