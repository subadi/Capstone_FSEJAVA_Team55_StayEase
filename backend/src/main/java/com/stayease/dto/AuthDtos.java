package com.stayease.dto;

import jakarta.validation.constraints.*;

public final class AuthDtos {
    private AuthDtos() {
    }

    public record RegisterRequest(@NotBlank @Email String email, @NotBlank @Size(min = 6, max = 100) String password,
                                  @NotBlank @Size(max = 100) String name) {
    }

    public record LoginRequest(@NotBlank @Email String email, @NotBlank String password) {
    }

    public record LoginResponse(String token) {
    }

    public record UserResponse(java.util.UUID id, String email, String name, String role) {
    }
}