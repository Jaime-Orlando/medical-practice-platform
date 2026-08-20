package com.jaime.medical_backend.security;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

// Identity class for the authenticated doctor
public class DoctorPrincipal implements UserDetails {
    private final UUID doctorId;
    private final UUID practiceId;
    private final String email;
    private final String passwordHash;
    private final boolean active;

    public DoctorPrincipal(
            UUID doctorId,
            UUID practiceId,
            String email,
            String passwordHash,
            boolean active) {
        this.doctorId = doctorId;
        this.practiceId = practiceId;
        this.email = email;
        this.passwordHash = passwordHash;
        this.active = active;
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public String getPassword() {
        return passwordHash;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_DOCTOR"));
    }

    @Override
    public boolean isEnabled() {
        return active;
    }

    public UUID getDoctorId() {
        return doctorId;
    }

    public UUID getPracticeId() {
        return practiceId;
    }
}
