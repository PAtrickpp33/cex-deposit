package com.example.ethreader.service;

import com.example.ethreader.model.User;
import com.example.ethreader.repository.UserRepository;
import com.example.ethreader.util.PasswordUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;

@Service
public class AdminInitializationService {

    private static final Logger logger = LoggerFactory.getLogger(AdminInitializationService.class);
    private static final String DEFAULT_ADMIN_USERNAME = "admin";
    private static final String DEFAULT_ADMIN_PASSWORD = "admin";
    private static final String DEFAULT_ADMIN_EMAIL = "admin@ethreader.com";

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordUtil passwordUtil;

    @PostConstruct
    public void init() {
        try {
            // Check if admin user exists
            if (!userRepository.existsByUsername(DEFAULT_ADMIN_USERNAME)) {
                // Create default admin user
                User admin = new User();
                admin.setUsername(DEFAULT_ADMIN_USERNAME);
                admin.setPassword(passwordUtil.encode(DEFAULT_ADMIN_PASSWORD));
                admin.setEmail(DEFAULT_ADMIN_EMAIL);
                admin.setRole(User.UserRole.ADMIN);
                
                userRepository.save(admin);
                logger.info("Default admin user created: username={}, password={}", 
                        DEFAULT_ADMIN_USERNAME, DEFAULT_ADMIN_PASSWORD);
            } else {
                // Update existing admin user to ensure it has ADMIN role
                User admin = userRepository.findByUsername(DEFAULT_ADMIN_USERNAME)
                        .orElse(null);
                if (admin != null && admin.getRole() != User.UserRole.ADMIN) {
                    admin.setRole(User.UserRole.ADMIN);
                    userRepository.save(admin);
                    logger.info("Updated existing admin user role to ADMIN");
                }
            }
        } catch (Exception e) {
            logger.error("Error initializing admin user", e);
        }
    }
}

