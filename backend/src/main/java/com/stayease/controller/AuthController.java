package com.stayease.controller;

import com.stayease.dto.AuthDtos.*;
import com.stayease.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService auth;

    public AuthController(AuthService a) {
        auth = a;
    }

    @PostMapping("/register")
    public ResponseEntity<Void> register(@Valid @RequestBody RegisterRequest r) {
        System.out.println("[AuthController.register] email=" + r.email());
        auth.register(r);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest r) {
        System.out.println("[AuthController.login] email=" + r.email());
        return ResponseEntity.ok(auth.login(r));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout() {
        auth.logout();
        return ResponseEntity.noContent().build();
    }
}
