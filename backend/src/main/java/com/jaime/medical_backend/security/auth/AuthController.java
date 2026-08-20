package com.jaime.medical_backend.security.auth;

import com.jaime.medical_backend.security.DoctorPrincipal;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthenticationManager authenticationManager;
    private final SecurityContextRepository securityContextRepository;

    public AuthController(AuthenticationManager authenticationManager, SecurityContextRepository securityContextRepository) {
        this.authenticationManager = authenticationManager;
        this.securityContextRepository = securityContextRepository;
    }

    @PostMapping("/login")
    public LoginResponse login(@RequestBody LoginRequest loginRequest, HttpServletRequest request, HttpServletResponse response) {
        Authentication authentication = authenticationManager.authenticate(UsernamePasswordAuthenticationToken.unauthenticated(loginRequest.email(), loginRequest.password()));
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        securityContextRepository.saveContext(context, request, response);
        DoctorPrincipal principal = (DoctorPrincipal) authentication.getPrincipal();
        return new LoginResponse(principal.getDoctorId(), principal.getPracticeId(), principal.getUsername());
    }

    @GetMapping("/me")
    public LoginResponse me(@AuthenticationPrincipal DoctorPrincipal principal) {
        return new LoginResponse(principal.getDoctorId(), principal.getPracticeId(), principal.getUsername());
    }

    @GetMapping("/csrf")
    public CsrfToken csrf(CsrfToken token) {
        return token;
    }



}
