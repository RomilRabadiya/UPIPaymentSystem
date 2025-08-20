package com.example.UPIPaymentSystem.Security;

import javax.sql.DataSource;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.JdbcUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;

@Configuration
public class SecurityConfig {

    @Bean
    public UserDetailsService userDetailsService(DataSource dataSource) {
        JdbcUserDetailsManager manager = new JdbcUserDetailsManager(dataSource);

        manager.setUsersByUsernameQuery(
            "SELECT mobile AS username, pin AS password, true AS enabled FROM users WHERE mobile = ?"
        );

        // You can skip authorities if not using roles, but better to return an empty list
        manager.setAuthoritiesByUsernameQuery(
            "SELECT mobile AS username, 'ROLE_USER' AS authority FROM users WHERE mobile = ?"
        );

        return manager;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        // Allows plain-text PINs without requiring a {noop} prefix
        return NoOpPasswordEncoder.getInstance();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(
                    "/login",
                    "/register",
                    "/register-process",
                    "/css/**", "/js/**"
                ).permitAll()
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/login")
                .loginProcessingUrl("/authenticateTheUser")
                .defaultSuccessUrl("/dashboard", true)
                .usernameParameter("mobile")
                .passwordParameter("pin")
                .permitAll()
            )
            .logout(logout -> logout
                .logoutSuccessUrl("/login?logout=true")
                .permitAll()
             )
	        .exceptionHandling(ex -> ex
	                .accessDeniedPage("/jobs/access-denied")
	         );

        return http.build();
    }
}
