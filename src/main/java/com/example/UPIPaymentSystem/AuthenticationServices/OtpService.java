package com.example.UPIPaymentSystem.AuthenticationServices;


import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Random;


//In this File We can Generate OTP And Store To the session and after that send OTP To EmailService 
//And EmailService Send Email TO particular Email   

//In Our 

@Service
public class OtpService {
    private static final String OTP_KEY = "REGISTER_OTP";
    private static final String OTP_EXPIRY_KEY = "REGISTER_OTP_EXPIRY";
    private static final String OTP_MOBILE_KEY = "REGISTER_OTP_MOBILE";

    private final EmailService emailService;

    public OtpService(EmailService emailService) {
        this.emailService = emailService;
    }

    // create OTP, store in session and send email
    public void createAndSendOtp(HttpSession session, String mobile, String email) 
    {
        String otp = generateOtp(6);
        Instant expiresAt = Instant.now().plus(1, ChronoUnit.MINUTES);
        
        
        //Create Session Of otp and also store expire time = 10 min and mobile number for verification
        session.setAttribute(OTP_KEY, otp);
        session.setAttribute(OTP_EXPIRY_KEY, expiresAt);
        session.setAttribute(OTP_MOBILE_KEY, mobile);
        
        
        //Send message to email service
        String subject = "Your UPI System verification OTP";
        String text = "Your OTP is: " + otp + "\nIt will expire at: " + expiresAt + " (UTC).";
        emailService.sendOtpEmail(email, subject, text);
    }

    
    private String generateOtp(int length) 
    {
        Random rng = new Random();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) sb.append(rng.nextInt(10));
        return sb.toString();
    }

    
    
    // verify OTP stored in session
    public boolean verifyOtp(HttpSession session, String mobile, String otp) 
    {
        Object storedMobile = session.getAttribute(OTP_MOBILE_KEY);
        Object storedOtp = session.getAttribute(OTP_KEY);
        Object storedExpiry = session.getAttribute(OTP_EXPIRY_KEY);

        if (storedMobile == null || storedOtp == null || storedExpiry == null) return false;
        if (!storedMobile.equals(mobile)) return false; // mobile mismatch
        Instant expiry = (Instant) storedExpiry;
        if (Instant.now().isAfter(expiry)) return false;
        if (!storedOtp.equals(otp)) return false;

        // consume otp
        session.removeAttribute(OTP_KEY);
        session.removeAttribute(OTP_EXPIRY_KEY);
        session.removeAttribute(OTP_MOBILE_KEY);
        return true;
    }

    
    // allow querying remaining seconds (for frontend timer)
    public long getRemainingSeconds(HttpSession session) 
    {
    	//After 1 min OTP Can Expired
        Object storedExpiry = session.getAttribute(OTP_EXPIRY_KEY);
        if (storedExpiry == null) return 0;
        Instant expiry = (Instant) storedExpiry;
        long secs = Instant.now().until(expiry, ChronoUnit.SECONDS);
        return Math.max(secs, 0);
    }
    
 // Update OTP expiry time (used when resending OTP)
    public void resetOtpExpiry(HttpSession session, int minutes) {
        Instant newExpiry = Instant.now().plus(minutes, ChronoUnit.MINUTES);
        session.setAttribute(OTP_EXPIRY_KEY, newExpiry);
    }

}
