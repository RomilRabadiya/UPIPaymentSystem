package com.example.UPIPaymentSystem.Repo_Service;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.example.UPIPaymentSystem.Entity.User;

import jakarta.transaction.Transactional;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, String> {

    // For checking duplicates during registration
    boolean existsByEmail(String email);
    boolean existsByUpiId(String upiId);

    // Find user by mobile number (ID)
    User findByMobile(String mobile);

    // Find user by UPI ID
    User findByUpiId(String upiId);
   
    boolean existsByMobile(String mobile);
    
    @Transactional
    void deleteByMobile(String mobile);
    
    @Query("SELECT u FROM User u WHERE " +
 	       "(LOWER(u.name) LIKE LOWER(CONCAT('%',:query,'%')) OR " +
 	       "u.mobile LIKE CONCAT('%',:query,'%')) AND " +
 	       "u.mobile <> :loggedMobile")
    List<User> searchUsers(String query, String loggedMobile);
}
