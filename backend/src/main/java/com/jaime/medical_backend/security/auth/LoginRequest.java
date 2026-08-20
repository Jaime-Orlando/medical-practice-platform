package com.jaime.medical_backend.security.auth;

public record LoginRequest(
        String email,
        String password
) {
}