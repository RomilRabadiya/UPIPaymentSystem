package com.example.UPIPaymentSystem.Security;

import javax.sql.DataSource;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.JdbcUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class SecurityConfig {

    private final JwtUtil jwtUtil;
    private final JwtAuthenticationEntryPoint JwtAuthenticationEntryPoint;
    public SecurityConfig(JwtUtil jwtUtil,JwtAuthenticationEntryPoint JwtAuthenticationEntryPoint) {
        this.jwtUtil = jwtUtil;
        this.JwtAuthenticationEntryPoint=JwtAuthenticationEntryPoint;
    }

    @Bean
    public UserDetailsService userDetailsService(DataSource dataSource) {
        JdbcUserDetailsManager manager = new JdbcUserDetailsManager(dataSource);

        manager.setUsersByUsernameQuery(
                "SELECT mobile AS username, pin AS password, true AS enabled FROM users WHERE mobile = ?"
        );
        
        return manager;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return NoOpPasswordEncoder.getInstance(); // or BCryptPasswordEncoder
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http
                .csrf(csrf -> csrf.disable())

                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/register",
                                "/auth/register",
                                "/ResendOtp",
                                "/verify-registration-otp",
                                "/login",
                                "/css/**",
                                "/js/**",
                                "/images/**"
                        ).permitAll()
                        .anyRequest().authenticated()
                )

                // ***** JWT AUTHENTICATION CONFIGURATION *****
		        .sessionManagement(session ->
		                session.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
		        )
		        
		        .addFilterBefore(
                        new JwtAuthenticationFilter(jwtUtil),
                        UsernamePasswordAuthenticationFilter.class
                )

                .exceptionHandling(ex -> ex
                		//If Other than Allowed endpoint You cann't enter to System and Redirect:
                		//to JwtAuthenticationEntryPoint commence method at which We will define Default URL
                        .authenticationEntryPoint(JwtAuthenticationEntryPoint) 
                        .accessDeniedPage("/Authentication/access-denied")
                );

        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}
