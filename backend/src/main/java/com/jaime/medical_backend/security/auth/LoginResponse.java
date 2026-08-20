package com.jaime.medical_backend.security.auth;

import java.util.UUID;

public record LoginResponse(
        UUID doctorId,
        UUID practiceId,
        String email
) {
}