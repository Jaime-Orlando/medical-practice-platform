package com.jaime.medical_backend.patient;

import com.jaime.medical_backend.doctor.Doctor;
import com.jaime.medical_backend.doctor.DoctorRepository;
import com.jaime.medical_backend.doctorpatient.DoctorPatient;
import com.jaime.medical_backend.doctorpatient.DoctorPatientRepository;
import com.jaime.medical_backend.practice.Practice;
import com.jaime.medical_backend.practice.PracticeRepository;
import com.jaime.medical_backend.security.DoctorPrincipal;
import com.jaime.medical_backend.specialization.Specialization;
import com.jaime.medical_backend.specialization.SpecializationRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.support.TransactionTemplate;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

import java.time.LocalDate;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@Testcontainers
class PatientUpdateIntegrationTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer postgres =
            new PostgreSQLContainer("postgres:17-alpine");

    @Autowired
    private PatientService patientService;

    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    private DoctorRepository doctorRepository;

    @Autowired
    private DoctorPatientRepository doctorPatientRepository;

    @Autowired
    private PracticeRepository practiceRepository;

    @Autowired
    private SpecializationRepository specializationRepository;

    @Autowired
    private TransactionTemplate transactionTemplate;

    @Test
    void shouldPersistPatientUpdatesWithoutExplicitSave() {
        // Arrange

        TestPatientIds ids = transactionTemplate.execute(status -> {
            Practice practice = practiceRepository.save(
                    new Practice("Test Practice")
            );

            Specialization specialization = specializationRepository.save(
                    new Specialization(
                            "BARIATRICS_UPDATE_TEST",
                            "Bariatric Medicine"
                    )
            );

            Doctor doctor = doctorRepository.save(
                    new Doctor(
                            practice,
                            specialization,
                            "Ana",
                            "Lopez",
                            null,
                            "update.test@example.com",
                            "unused"
                    )
            );

            Patient patient = patientRepository.save(
                    new Patient(
                            practice,
                            "MR-100",
                            "Maria",
                            "Lopez",
                            "Garcia",
                            LocalDate.of(1988, 5, 10),
                            "FEMALE",
                            null,
                            "maria@example.com",
                            "5511111111"
                    )
            );

            doctorPatientRepository.save(
                    new DoctorPatient(doctor, patient)
            );

            return new TestPatientIds(
                    practice.getId(),
                    doctor.getId(),
                    patient.getId()
            );
        });

        UUID practiceId = ids.practiceId();
        UUID doctorId = ids.doctorId();
        UUID patientId = ids.patientId();

        DoctorPrincipal principal = new DoctorPrincipal(
                doctorId,
                practiceId,
                "doctor@example.com",
                "unused",
                true
        );

        Authentication authentication =
                UsernamePasswordAuthenticationToken.authenticated(
                        principal,
                        null,
                        principal.getAuthorities()
                );

        SecurityContext context =
                SecurityContextHolder.createEmptyContext();

        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);

        UpdatePatientRequest request = new UpdatePatientRequest(
                "Mariana",
                null,
                null,
                null,
                "5522222222"
        );


        // Act
        patientService.updatePatient(patientId, request);

// After the service transaction has committed:
        Patient reloaded = transactionTemplate.execute(status ->
                patientRepository.findById(patientId).orElseThrow()
        );

        assertEquals("Mariana", reloaded.getFirstName());
        assertEquals("Lopez", reloaded.getPaternalSurname());
        assertEquals("Garcia", reloaded.getMaternalSurname());
        assertEquals("maria@example.com", reloaded.getEmail());
        assertEquals("5522222222", reloaded.getPhoneNumber());

    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private record TestPatientIds(
            UUID practiceId,
            UUID doctorId,
            UUID patientId
    ) {}
}
