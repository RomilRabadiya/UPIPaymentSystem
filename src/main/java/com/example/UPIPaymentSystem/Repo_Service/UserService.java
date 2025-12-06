package com.example.UPIPaymentSystem.Repo_Service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.UPIPaymentSystem.Entity.User;
import com.example.UPIPaymentSystem.Security.JwtUtil;

import java.util.HashMap;
import java.util.Map;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;

    @Autowired
    public UserService(UserRepository userRepository, JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.jwtUtil = jwtUtil;
    }

    // =============================
    // Save new user (disabled)
    // =============================
    public User save(User user) {
        return userRepository.save(user);
    }

    // =============================
    // Enable user after OTP verify
    // =============================
    public User RegisterUser(String mobile) 
    {
        User user = userRepository.findByMobile(mobile);

        userRepository.save(user);

        // Generate token (return value can be used if needed)
        generateTokenForUser(user);
        
        return user;
    }

    // =============================
    // Login (mobile + pin) - Returns JWT Token
    // =============================
    public String login(String mobile, String pin) 
    {
        User user = userRepository.findByMobile(mobile);

        if (!user.getPin().equals(pin))
            throw new RuntimeException("Invalid credentials");

        return generateTokenForUser(user);
    }

    // =============================
    // Generate JWT - Returns Token String
    // =============================
    public String generateTokenForUser(User user) 
    {
        Map<String, Object> claims = new HashMap<>();
        claims.put("mobile", user.getMobile());
        claims.put("email", user.getEmail());
        claims.put("name", user.getName());

        return jwtUtil.generateToken(user.getMobile(),claims);
    }

    
    // =============================
    // REQUIRED METHODS FOR VALIDATION
    // =============================
    
    public boolean existsByMobile(String mobile) {
        return userRepository.existsByMobile(mobile);
    }

    public boolean existsByUpiId(String upiId) {
        return userRepository.existsByUpiId(upiId);
    }

    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    // Get user by mobile number
    public User getUserByMobile(String mobile) {
        return userRepository.findByMobile(mobile);
    }
    
 // New method to remove user by mobile
    public void removeByMobile(String mobile) {
        userRepository.deleteByMobile(mobile);
    }
}