package com.example.UPIPaymentSystem.Security;

import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;

    public JwtAuthenticationFilter(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {
    	
    	String requestURI = request.getRequestURI();
    	System.out.println("In Filter method - Checking JWT Authentication");
    	System.out.println("Request URI: " + requestURI);

        
    	
    	
    	String token = null;
         HttpSession session = request.getSession(false);
         if (session != null) {
             Object sessionToken = session.getAttribute("jwtToken");
             if (sessionToken != null) {
                 token = sessionToken.toString();
                 System.out.println("✓ Token found in SESSION");
             }
         }


        // Validate and process token if found
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

                    // Important: Only set authentication if not already set
                    if (SecurityContextHolder.getContext().getAuthentication() == null) 
                    {
                        UsernamePasswordAuthenticationToken authentication =
                                new UsernamePasswordAuthenticationToken(
                                        mobile,
                                        null,
                                        null // No authorities for now
                                );

                        // Set authentication into Security Context
                        SecurityContextHolder.getContext().setAuthentication(authentication);
                        System.out.println("✓ Authentication set for user: " + mobile);
                    }
                    else 
                    {
                        System.out.println("Authentication already set - skipping");
                    }
                } 
                else 
                {
                    System.out.println("✗ Token validation failed - invalid or expired");
                }
            }
            catch (Exception e) 
            {
                System.out.println("✗ Error processing token: " + e.getMessage());
                e.printStackTrace();
                // Don't set authentication if token processing fails
            }
        }
        else 
        {
            System.out.println("✗ No token found - Request will be rejected if endpoint requires authentication");
        }

        // Continue filter chain (always continue - let Spring Security handle authorization)
        filterChain.doFilter(request, response);
    }
}
