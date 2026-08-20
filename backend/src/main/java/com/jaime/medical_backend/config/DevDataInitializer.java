package com.jaime.medical_backend.config;

import com.jaime.medical_backend.doctor.Doctor;
import com.jaime.medical_backend.doctor.DoctorRepository;
import com.jaime.medical_backend.practice.Practice;
import com.jaime.medical_backend.practice.PracticeRepository;
import com.jaime.medical_backend.specialization.Specialization;
import com.jaime.medical_backend.specialization.SpecializationRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@Profile("dev")
public class DevDataInitializer {

    @Bean
    CommandLineRunner seedDevelopmentData(
            PracticeRepository practiceRepository,
            SpecializationRepository specializationRepository,
            DoctorRepository doctorRepository,
            PasswordEncoder passwordEncoder) {

        return args -> {
            // seed logic
            Specialization bariatrics = specializationRepository
                    .findByCode("BARIATRICS")
                    .orElseThrow();
            Practice practice = practiceRepository.findAll().stream()
                    .findFirst()
                    .orElseGet(() ->
                            practiceRepository.save(
                                    new Practice("Development Practice")
                            )
                    );
            if (doctorRepository.findByEmail("dev.doctor@example.com").isEmpty()) {

                Doctor doctor = new Doctor(
                        practice,
                        bariatrics,
                        "Dev",
                        "Doctor",
                        null,
                        "dev.doctor@example.com",
                        passwordEncoder.encode("dev-password-123")
                );

                doctorRepository.save(doctor);
            }
        };

    }
}