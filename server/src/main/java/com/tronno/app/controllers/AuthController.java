package com.tronno.app.controllers;

import com.tronno.app.dtos.AuthDtos.*;
import com.tronno.app.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService auth;

    @PostMapping("/register")
    public AuthResponse register(@Valid @RequestBody RegisterRequest r) {
        return auth.register(r);
    }

    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest r) {
        return auth.login(r);
    }

    @GetMapping("/me")
    public AuthResponse me(Authentication a) {
        return auth.me((String) a.getPrincipal());
    }

    @PutMapping("/me")
    public AuthResponse updateMe(Authentication a, @Valid @RequestBody UpdateProfileRequest r) {
        return auth.updateProfile((String) a.getPrincipal(), r);
    }

    @PutMapping("/profile")
    public AuthResponse updateProfile(Authentication a, @Valid @RequestBody UpdateProfileRequest r) {
        return auth.updateProfile((String) a.getPrincipal(), r);
    }
}
