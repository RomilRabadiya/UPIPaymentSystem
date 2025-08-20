package com.example.UPIPaymentSystem.Repo_Service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.UPIPaymentSystem.Entity.User;

import java.util.Optional;

@Service
public class UserService
{
    @Autowired
    private UserRepository userRepository;

    // Register a new user
    public User save(User user)
    {
        return userRepository.save(user);
    }

    // Find user by ID (mobile number)
    public Optional<User> findById(String mobile)
    {
        return userRepository.findById(mobile);
    }
    
    
    public boolean existsById(String mobile)
    {
        return userRepository.existsById(mobile);
    }
    
    
    public User findByUpiId(String upiId)
    {
    	return userRepository.findByUpiId(upiId);
    }
    
    public User findByMobile(String mobile) {
        return userRepository.findByMobile(mobile);
    }
}