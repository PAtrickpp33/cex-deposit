package com.example.ethreader.controller;

import com.example.ethreader.dto.LoginRequest;
import com.example.ethreader.dto.LoginResponse;
import com.example.ethreader.dto.RegisterRequest;
import com.example.ethreader.model.User;
import com.example.ethreader.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    @Autowired
    private UserService userService;

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest request) {
        try {
            User user = userService.register(
                    request.getUsername(),
                    request.getPassword(),
                    request.getEmail()
            );

            Map<String, Object> response = new HashMap<>();
            response.put("message", "User registered successfully");
            response.put("username", user.getUsername());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        try {
            String token = userService.login(request.getUsername(), request.getPassword());
            
            LoginResponse response = new LoginResponse();
            response.setToken(token);
            response.setUsername(request.getUsername());
            response.setMessage("Login successful");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
}

