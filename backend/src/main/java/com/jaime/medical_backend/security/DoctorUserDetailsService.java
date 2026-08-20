package com.jaime.medical_backend.security;

import com.jaime.medical_backend.doctor.Doctor;
import com.jaime.medical_backend.doctor.DoctorRepository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;


@Service
public class DoctorUserDetailsService implements UserDetailsService {
    private final DoctorRepository doctorRepository;
    public DoctorUserDetailsService(DoctorRepository doctorRepository) {
        this.doctorRepository = doctorRepository;
    }



    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Doctor doctor = doctorRepository.findByEmail(username)
                .orElseThrow(() -> new UsernameNotFoundException("Doctor not found with email: " + username));
        return new DoctorPrincipal(
                doctor.getId(),
                doctor.getPractice().getId(),
                doctor.getEmail(),
                doctor.getPasswordHash(),
                doctor.isActive()
        );
    }
}
