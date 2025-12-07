package com.example.UPIPaymentSystem.Repo_Service;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.example.UPIPaymentSystem.Entity.BankAccount;
import com.example.UPIPaymentSystem.Entity.User;
import com.example.UPIPaymentSystem.Security.JwtUtil;

import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpSession;

import java.util.List;
import java.util.Optional;

@Service
public class BankAccountService
{
    @Autowired
    private BankAccountRepository bankAccountRepository;
    
    @Autowired
    private HttpSession session;
    
    @Autowired
    private JwtUtil jwtUtil;


    // ------------------------------------------------------
    // Extract User Mobile Number From JWT Token
    // ------------------------------------------------------
    public String extractMobileFromToken(String authHeader) 
    {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return null;
        }

        String token = authHeader.substring(7);

        if (!jwtUtil.validateToken(token)) {
            return null;
        }

        // SUBJECT = MOBILE NUMBER
        return jwtUtil.getClaims(token).getSubject();
    }


    public BankAccount save(BankAccount account)
    {
        return bankAccountRepository.save(account);
    }

    public List<BankAccount> getAccountsByUser(String mobile)
    {
        return bankAccountRepository.findByUser_Mobile(mobile);
    }

    public BankAccount getAccountById(Long id)
    {
        return bankAccountRepository.findById(id).orElse(null);
    }

    public BankAccount update(BankAccount account)
    {
        return bankAccountRepository.save(account);
    }
    
    public BankAccount findByAccountNumber(String accountNumber)
    {
        try
        {
            return bankAccountRepository.findByAccountNumber(accountNumber).orElse(null);
        }
        catch (Exception e)
        {
            System.out.println("Error finding account: " + e.getMessage());
            return null;
        }
    }
    
    public BankAccount findByAccountNumberAndIfscCode(String accountNumber, String ifscCode) {
        return bankAccountRepository.findByAccountNumberAndIfscCode(accountNumber, ifscCode);
    }
    
    
    
    
    public boolean setPrimaryAccount(String userMobile, String accountNumber) 
	{
		User user = userRepository.findByMobile(userMobile);
	    if (user == null) 
	    {
	    	return false;
	    }

	    // Reset all to false
	    for (BankAccount acc : user.getAccounts()) 
	    {
	        acc.setPrimaryAccount(false);
	    }

	    // Set the chosen account as primary
	    BankAccount chosen = user.getAccountByNumber(accountNumber);
	    if (chosen == null) return false;

	    chosen.setPrimaryAccount(true);

	    userRepository.save(user); // saves cascade to accounts
	    return true;
	}

    
    
    
    @Autowired
    private UserRepository userRepository;

    public BankAccount findByUpiId(String upiId) 
    {
        // Find the user by UPI ID
        User user = userRepository.findByUpiId(upiId);
        
        if (user == null) 
        {
            return null; // recipient not found
        }
        
        List<BankAccount> listBankAccounts=user.getAccounts();

        // Get one of their bank accounts
        if (listBankAccounts != null && !listBankAccounts.isEmpty()) 
        {
            
        	for(BankAccount b:listBankAccounts)
        	{
        		if(b.isPrimaryAccount())
        		{
        			return b;
        		}
        	}
        }

        return null;
    }

	public BankAccount findByMobileNumber(String mobile) {
		User user = userRepository.findByMobile(mobile);
		        
        if (user == null) 
        {
            return null; // recipient not found
        }
        
        List<BankAccount> listBankAccounts=user.getAccounts();

        // Get one of their bank accounts
        if (listBankAccounts != null && !listBankAccounts.isEmpty()) 
        {
            
        	for(BankAccount b:listBankAccounts)
        	{
        		if(b.isPrimaryAccount())
        		{
        			return b;
        		}
        	}
        }

        return null;

	}
	
	public User getUserFromAuthentication()
	{
		String token = null;
        if (session != null) {
            Object sessionToken = session.getAttribute("jwtToken");
            if (sessionToken != null) {
                token = sessionToken.toString();
                System.out.println("✓ Token found in SESSION");
            }
        }
        if (StringUtils.hasText(token)) 
        {
            try 
            {
                if (jwtUtil.validateToken(token)) 
                {
                    System.out.println("✓ Token is valid - Setting authentication");
                    
                    // Extract information from token
                    Claims claims = jwtUtil.getClaims(token);
                    String mobile = claims.getSubject();  // user identifier (mobile number)
                    
                    User u=userRepository.findByMobile(mobile);
                    if(u==null)
                    {
                    	
                    }
                    
                    return u;
                }
            }
            catch(Exception e)
            {
            	
            }
        }
        return null;
	}
}
