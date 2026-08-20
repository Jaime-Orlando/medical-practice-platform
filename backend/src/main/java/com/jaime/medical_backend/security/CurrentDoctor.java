package com.jaime.medical_backend.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class CurrentDoctor {
    public DoctorPrincipal principal(){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof DoctorPrincipal principal)) {
            throw new IllegalStateException("No authenticated doctor found");
        }
        return principal;
    }
}
