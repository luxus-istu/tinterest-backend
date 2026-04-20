package com.luxus.tinterest.controller;


import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1")
public class RegistrationController {

    @PostMapping("/auth/test")
    public String testMethod() {
        return "Test method!";
    }

    @PostMapping("/auth/register")
    public ResponseEntity<?> register() {
        return ResponseEntity.ok(null);
    }
}
