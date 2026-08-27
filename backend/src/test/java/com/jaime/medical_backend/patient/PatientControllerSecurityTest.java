package com.jaime.medical_backend.patient;

import com.jaime.medical_backend.practice.Practice;
import com.jaime.medical_backend.security.DoctorPrincipal;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.UUID;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@SpringBootTest
@AutoConfigureMockMvc
public class PatientControllerSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PatientService patientService;


    @Test
    void unauthenticatedRequestCannotAccessPatient() throws Exception {
        UUID patientId = UUID.randomUUID();
        mockMvc.perform(get("/api/patients/{patientId}", patientId))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void authenticatedDoctorCanAccessPatient() throws Exception {
        UUID doctorId = UUID.randomUUID();
        UUID practiceId = UUID.randomUUID();
        UUID patientId = UUID.randomUUID();

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

        Practice practice = new Practice("Test Practice");

        Patient patient = new Patient(
                practice,
                "MR-001",
                "Juan",
                "Perez",
                "Perez",
                LocalDate.of(1990, 1, 1),
                "MALE",
                null,
                null,
                null
        );

        when(patientService.getPatientForCurrentDoctor(patientId))
                .thenReturn(patient);

        mockMvc.perform(
                        get("/api/patients/{patientId}", patientId)
                                .with(authentication(authentication))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.medicalRecordNumber").value("MR-001"))
                .andExpect(jsonPath("$.firstName").value("Juan"))
                .andExpect(jsonPath("$.paternalSurname").value("Perez"))
                .andExpect(jsonPath("$.active").value(true));

    }

    @Test
    void returnsNotFoundWhenPatientDoesNotExist() throws Exception {
        UUID doctorId = UUID.randomUUID();
        UUID practiceId = UUID.randomUUID();
        UUID patientId = UUID.randomUUID();
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
        when(patientService.getPatientForCurrentDoctor(patientId))
                .thenThrow(new PatientNotFoundException(patientId));

        mockMvc.perform(
                        get("/api/patients/{patientId}", patientId)
                                .with(authentication(authentication))
                )
                .andExpect(status().isNotFound());
    }

    @Test
    void returnsForbiddenWhenDoctorCannotAccessPatient() throws Exception {
        UUID doctorId = UUID.randomUUID();
        UUID practiceId = UUID.randomUUID();
        UUID patientId = UUID.randomUUID();
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

        when(patientService.getPatientForCurrentDoctor(patientId))
                .thenThrow(new PatientAccessDeniedException());

        mockMvc.perform(
                        get("/api/patients/{patientId}", patientId)
                                .with(authentication(authentication))
                )
                .andExpect(status().isForbidden());
    }
}
