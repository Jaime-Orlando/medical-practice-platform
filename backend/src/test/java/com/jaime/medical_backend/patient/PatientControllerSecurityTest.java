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
import org.springframework.http.MediaType;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

@SpringBootTest
@AutoConfigureMockMvc
public class PatientControllerSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PatientService patientService;

    private Authentication authenticatedDoctor(UUID doctorId, UUID practiceId) {
        DoctorPrincipal principal = new DoctorPrincipal(
                doctorId,
                practiceId,
                "doctor@example.com",
                "unused",
                true
        );

        return UsernamePasswordAuthenticationToken.authenticated(
                principal,
                null,
                principal.getAuthorities()
        );
    }
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

        Authentication authentication = authenticatedDoctor(doctorId, practiceId);
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
        Authentication authentication = authenticatedDoctor(doctorId, practiceId);
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

        Authentication authentication = authenticatedDoctor(doctorId, practiceId);

        when(patientService.getPatientForCurrentDoctor(patientId))
                .thenThrow(new PatientAccessDeniedException());

        mockMvc.perform(
                        get("/api/patients/{patientId}", patientId)
                                .with(authentication(authentication))
                )
                .andExpect(status().isForbidden());
    }

    @Test
    void unauthenticatedRequestCannotCreatePatient() throws Exception {
        mockMvc.perform(
                        post("/api/patients")
                                .with(csrf())
                )
                .andExpect(status().isUnauthorized());
    }

    @Test
    void authenticatedDoctorCanCreatePatient() throws Exception {
        UUID doctorId = UUID.randomUUID();
        UUID practiceId = UUID.randomUUID();

        Authentication authentication = authenticatedDoctor(doctorId, practiceId);

        Practice practice = new Practice("Test Practice");

        Patient patient = new Patient(
                practice,
                "MR-002",
                "Maria",
                "Lopez",
                "Garcia",
                LocalDate.of(1988, 5, 10),
                "FEMALE",
                "TESTCURP123456789",
                "maria@example.com",
                "5551234567"
        );
        when(patientService.createPatient(any(CreatePatientRequest.class)))
                .thenReturn(patient);

        mockMvc.perform(
                        post("/api/patients")
                                .with(authentication(authentication))
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                        {
                          "medicalRecordNumber": "MR-002",
                          "firstName": "Maria",
                          "paternalSurname": "Lopez",
                          "maternalSurname": "Garcia",
                          "birthDate": "1988-05-10",
                          "sex": "FEMALE",
                          "curp": "TESTCURP123456789",
                          "email": "maria@example.com",
                          "phoneNumber": "5551234567"
                        }
                        """)
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.medicalRecordNumber").value("MR-002"))
                .andExpect(jsonPath("$.firstName").value("Maria"))
                .andExpect(jsonPath("$.paternalSurname").value("Lopez"))
                .andExpect(jsonPath("$.active").value(true));
    }
    @Test
    void returnsConflictWhenMedicalRecordNumberAlreadyExists() throws Exception {
        UUID doctorId = UUID.randomUUID();
        UUID practiceId = UUID.randomUUID();


        Authentication authentication = authenticatedDoctor(doctorId, practiceId);

        when(patientService.createPatient(any(CreatePatientRequest.class)))
                .thenThrow(new DuplicateMedicalRecordNumberException("MR-002"));

        mockMvc.perform(
                        post("/api/patients")
                                .with(authentication(authentication))
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                            {
                              "medicalRecordNumber": "MR-002",
                              "firstName": "Maria",
                              "paternalSurname": "Lopez",
                              "maternalSurname": "Garcia",
                              "birthDate": "1988-05-10",
                              "sex": "FEMALE",
                              "curp": "TESTCURP123456789",
                              "email": "maria@example.com",
                              "phoneNumber": "5551234567"
                            }
                            """)
                )
                .andExpect(status().isConflict());
    }

    @Test
    void returnsConflictWhenCurpAlreadyExists() throws Exception {
        UUID doctorId = UUID.randomUUID();
        UUID practiceId = UUID.randomUUID();
        Authentication authentication = authenticatedDoctor(doctorId, practiceId);

        when(patientService.createPatient(any(CreatePatientRequest.class)))
                .thenThrow(new DuplicateCurpException("TESTCURP123456789"));

        mockMvc.perform(
                        post("/api/patients")
                                .with(authentication(authentication))
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                            {
                              "medicalRecordNumber": "MR-003",
                              "firstName": "Maria",
                              "paternalSurname": "Lopez",
                              "maternalSurname": "Garcia",
                              "birthDate": "1988-05-10",
                              "sex": "FEMALE",
                              "curp": "TESTCURP123456789",
                              "email": "maria@example.com",
                              "phoneNumber": "5551234567"
                            }
                            """)
                )
                .andExpect(status().isConflict());
    }

    @Test
    void returnsBadRequestWhenCreatePatientRequestIsInvalid() throws Exception {
        UUID doctorId = UUID.randomUUID();
        UUID practiceId = UUID.randomUUID();
        Authentication authentication = authenticatedDoctor(doctorId, practiceId);


        mockMvc.perform(
                        post("/api/patients")
                                .with(authentication(authentication))
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                            {
                              "medicalRecordNumber": "",
                              "firstName": "",
                              "paternalSurname": "",
                              "birthDate": null,
                              "sex": ""
                            }
                            """)
                )
                .andExpect(status().isBadRequest());
        verifyNoInteractions(patientService);
    }

    @Test
    void authenticatedDoctorCanListPatients() throws Exception {
        UUID doctorId = UUID.randomUUID();
        UUID practiceId = UUID.randomUUID();

        Authentication authentication =
                authenticatedDoctor(doctorId, practiceId);

        Practice practice = new Practice("Test Practice");

        Patient maria = new Patient(
                practice,
                "MR-100",
                "Maria",
                "Lopez",
                "Garcia",
                LocalDate.of(1988, 5, 10),
                "FEMALE",
                null,
                null,
                "5551234567"
        );

        Patient juan = new Patient(
                practice,
                "MR-101",
                "Juan",
                "Perez",
                null,
                LocalDate.of(1990, 1, 1),
                "MALE",
                null,
                null,
                "5559876543"
        );

        when(patientService.getPatientsForCurrentDoctor(null))
                .thenReturn(List.of(maria, juan));

        mockMvc.perform(
                        get("/api/patients")
                                .with(authentication(authentication))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].medicalRecordNumber").value("MR-100"))
                .andExpect(jsonPath("$[0].firstName").value("Maria"))
                .andExpect(jsonPath("$[1].medicalRecordNumber").value("MR-101"))
                .andExpect(jsonPath("$[1].firstName").value("Juan"));
    }

    @Test
    void unauthenticatedRequestCannotListPatients() throws Exception {
        mockMvc.perform(get("/api/patients"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void authenticatedDoctorCanSearchPatients() throws Exception {
        UUID doctorId = UUID.randomUUID();
        UUID practiceId = UUID.randomUUID();

        Authentication authentication =
                authenticatedDoctor(doctorId, practiceId);

        Practice practice = new Practice("Test Practice");

        Patient maria = new Patient(
                practice,
                "MR-100",
                "Maria",
                "Lopez",
                "Garcia",
                LocalDate.of(1988, 5, 10),
                "FEMALE",
                null,
                null,
                "5551234567"
        );

        when(patientService.getPatientsForCurrentDoctor("Maria"))
                .thenReturn(List.of(maria));

        mockMvc.perform(
                        get("/api/patients")
                                .param("query", "Maria")
                                .with(authentication(authentication))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].medicalRecordNumber").value("MR-100"))
                .andExpect(jsonPath("$[0].firstName").value("Maria"));

        verify(patientService)
                .getPatientsForCurrentDoctor("Maria");
    }
}
