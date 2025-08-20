package com.example.UPIPaymentSystem.Repo_Service;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.UPIPaymentSystem.Entity.User;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, String>
{
    boolean existsById(String mobile);

	User findByUpiId(String upiId);

	User findByMobile(String mobile);
}
