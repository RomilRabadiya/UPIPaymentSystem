package com.example.UPIPaymentSystem.Security;


import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.Map;

@Component
//SPring boot Automaticality Generate instences of that class and use that Instences using @AutoWired 

//In this Class We Have Main Three Method
//		-Generate JWT Token By calling Jwts.builder()
//		-GetToken By call Jwts.parser()
//		-Compare Token

public class JwtUtil {
    @Value("${app.jwt.secret:CHANGE_THIS_TO_A_STRONG_SECRET_KEY_256_BITS_MINIMUM}")
    private String secret;
    
    private final long expirationMs = 1000L * 60 * 60 * 24; // 24 hours

    private Key getSigningKey() 
    {
        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        
        // Always hash the secret to ensure exactly 32 bytes (256 bits) for HMAC-SHA256
        // This ensures security regardless of input secret length
        try 
        {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            keyBytes = digest.digest(keyBytes);
        } 
        catch (NoSuchAlgorithmException e) 
        {
            // Fallback: ensure at least 32 bytes by padding or truncating
            if (keyBytes.length < 32) 
            {
                byte[] paddedKey = new byte[32];
                System.arraycopy(keyBytes, 0, paddedKey, 0, keyBytes.length);
                // Fill remaining bytes by repeating the secret
                for (int i = keyBytes.length; i < 32; i++) 
                {
                    paddedKey[i] = keyBytes[i % keyBytes.length];
                }
                keyBytes = paddedKey;
            } 
            else if (keyBytes.length > 32) {
                // Truncate to 32 bytes
                byte[] truncatedKey = new byte[32];
                System.arraycopy(keyBytes, 0, truncatedKey, 0, 32);
                keyBytes = truncatedKey;
            }
        }
        
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public String generateToken(String subject, Map<String, Object> claims) 
    {
    	System.out.println("We can Generate Token okk");
    	System.out.println("We can Generate Token okk");
    	System.out.println("We can Generate Token okk");
    	System.out.println("We can Generate Token okk");
    	Key key = getSigningKey();

    	return Jwts.builder()
    	        .setClaims(claims)
    	        .setSubject(subject)
    	        .setExpiration(new Date(System.currentTimeMillis() + expirationMs))
    	        .signWith(key)
    	        .compact();
    }

    public Claims getClaims(String token) 
    {
        Key key = getSigningKey();
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public boolean validateToken(String token) {
        try {
            getClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException ex) {
            return false;
        }
    }
}