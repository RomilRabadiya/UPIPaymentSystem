package com.example.UPIPaymentSystem.Repo_Service;

import org.springframework.data.jpa.repository.JpaRepository;
import com.example.UPIPaymentSystem.Entity.User;

import jakarta.transaction.Transactional;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, String> {

    // For checking duplicates during registration
    boolean existsByEmail(String email);
    boolean existsByUpiId(String upiId);

    // Find user by mobile number (ID)
    User findByMobile(String mobile);

    // Find user by UPI ID
    Optional<User> findByUpiId(String upiId);
   
    boolean existsByMobile(String mobile);
    
    @Transactional
    void deleteByMobile(String mobile);
}
