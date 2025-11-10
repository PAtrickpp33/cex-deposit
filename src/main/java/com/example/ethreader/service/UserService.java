package com.example.ethreader.service;

import com.example.ethreader.model.User;
import com.example.ethreader.repository.UserRepository;
import com.example.ethreader.util.JwtUtil;
import com.example.ethreader.util.PasswordUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordUtil passwordUtil;

    @Autowired
    private JwtUtil jwtUtil;

    public User register(String username, String password, String email) {
        // Check if username already exists
        if (userRepository.existsByUsername(username)) {
            throw new RuntimeException("Username already exists");
        }

        // Check if email already exists
        if (userRepository.existsByEmail(email)) {
            throw new RuntimeException("Email already exists");
        }

        // Create new user
        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordUtil.encode(password));
        user.setEmail(email);
        user.setRole(User.UserRole.USER); // Default role is USER

        return userRepository.save(user);
    }

    public String login(String username, String password) {
        Optional<User> userOpt = userRepository.findByUsername(username);
        
        if (userOpt.isEmpty()) {
            throw new RuntimeException("Invalid username or password");
        }

        User user = userOpt.get();
        
        if (!passwordUtil.matches(password, user.getPassword())) {
            throw new RuntimeException("Invalid username or password");
        }

        // Generate JWT token
        return jwtUtil.generateToken(username);
    }

    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public Optional<User> findById(String id) {
        return userRepository.findById(id);
    }

    public boolean isAdmin(String username) {
        Optional<User> userOpt = userRepository.findByUsername(username);
        return userOpt.isPresent() && userOpt.get().getRole() == User.UserRole.ADMIN;
    }

    public java.util.List<User> getAllUsers() {
        return userRepository.findAll();
    }
}

