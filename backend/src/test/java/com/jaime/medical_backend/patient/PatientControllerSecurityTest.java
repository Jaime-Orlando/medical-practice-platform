package com.jaime.medical_backend.patient;

import com.jaime.medical_backend.practice.Practice;
import com.jaime.medical_backend.security.DoctorPrincipal;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.http.MediaType;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

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

        when(patientService.getPatientsForCurrentDoctor(
                null,
                PatientStatusFilter.ACTIVE
        )).thenReturn(List.of(maria, juan));

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

        verify(patientService).getPatientsForCurrentDoctor(
                null,
                PatientStatusFilter.ACTIVE
        );
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

        when(patientService.getPatientsForCurrentDoctor(
                "Maria",
                PatientStatusFilter.ACTIVE
        )).thenReturn(List.of(maria));



        mockMvc.perform(
                        get("/api/patients")
                                .param("query", "Maria")
                                .with(authentication(authentication))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].medicalRecordNumber").value("MR-100"))
                .andExpect(jsonPath("$[0].firstName").value("Maria"));

        verify(patientService).getPatientsForCurrentDoctor(
                "Maria",
                PatientStatusFilter.ACTIVE
        );
    }

    @Test
    void shouldUpdatePatientWhenRequestIsValid() throws Exception {
        // Arrange
        UUID doctorId = UUID.randomUUID();
        UUID practiceId = UUID.randomUUID();
        UUID patientId = UUID.randomUUID();

        Authentication authentication =
                authenticatedDoctor(doctorId, practiceId);

        Practice practice = new Practice("Test Practice");

        Patient updatedPatient = new Patient(
                practice,
                "MR-100",
                "Mariana",
                "Lopez",
                "Garcia",
                LocalDate.of(1988, 5, 10),
                "FEMALE",
                null,
                null,
                "5522222222"
        );

        when(patientService.updatePatient(
                eq(patientId),
                any(UpdatePatientRequest.class)
        )).thenReturn(updatedPatient);

        // Act & Assert
        mockMvc.perform(
                patch("/api/patients/{patientId}", patientId)
                        .with(authentication(authentication))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(
                                """
                    {
                        "firstName": "Mariana",
                        "phoneNumber": "5522222222"
                    }
                    """)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("Mariana"))
                .andExpect(jsonPath("$.paternalSurname").value("Lopez"))
                .andExpect(jsonPath("$.phoneNumber").value("5522222222"));

        ArgumentCaptor<UpdatePatientRequest> captor =
                ArgumentCaptor.forClass(UpdatePatientRequest.class);

        verify(patientService).updatePatient(
                eq(patientId),
                captor.capture()
        );

        UpdatePatientRequest capturedRequest = captor.getValue();

        assertEquals("Mariana", capturedRequest.firstName());
        assertEquals("5522222222", capturedRequest.phoneNumber());
        assertNull(capturedRequest.paternalSurname());
        assertNull(capturedRequest.maternalSurname());
        assertNull(capturedRequest.email());
    }

    @Test
    void shouldRejectInvalidPatientUpdateRequest() throws Exception {
        // Arrange
        UUID doctorId = UUID.randomUUID();
        UUID practiceId = UUID.randomUUID();
        UUID patientId = UUID.randomUUID();

        Authentication authentication =
                authenticatedDoctor(doctorId, practiceId);


        // Act & Assert
        mockMvc.perform(
                patch("/api/patients/{patientId}", patientId)
                        .with(authentication(authentication))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(
                                """
                                        {
                                          "firstName": "   ",
                                          "phoneNumber": "1234567890123456789012345"
                                        }
                    """)
                )
                .andExpect(status().isBadRequest())
                .andExpect(result ->
                        assertInstanceOf(
                                MethodArgumentNotValidException.class,
                                result.getResolvedException()
                        )
                );

        verify(patientService, never())
                .updatePatient(
                        any(UUID.class),
                        any(UpdatePatientRequest.class)
                );
    }

    @Test
    void shouldRejectPatientUpdateWithoutCsrfToken() throws Exception {
        // Arrange
        UUID doctorId = UUID.randomUUID();
        UUID practiceId = UUID.randomUUID();
        UUID patientId = UUID.randomUUID();

        Authentication authentication =
                authenticatedDoctor(doctorId, practiceId);

        // Act & Assert
        mockMvc.perform(
                patch("/api/patients/{patientId}", patientId)
                        .with(authentication(authentication))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(
                                """
                                        {
                                          "firstName": "Mariana",
                                          "paternalSurname": "Lopez",
                                          "phoneNumber": "5522222222"
                                        }
                    """)
                )
                .andExpect(status().isForbidden());

        verify(patientService, never())
                .updatePatient(
                        any(UUID.class),
                        any(UpdatePatientRequest.class)
                );
    }

    @Test
    void shouldRejectUnauthenticatedPatientUpdate() throws Exception {
        UUID patientId = UUID.randomUUID();

        mockMvc.perform(
                        patch("/api/patients/{patientId}", patientId)
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                        {
                            "firstName": "Mariana"
                        }
                        """)
                )
                .andExpect(status().isUnauthorized());

        verify(patientService, never())
                .updatePatient(
                        any(UUID.class),
                        any(UpdatePatientRequest.class)
                );
    }

    @Test
    void shouldRejectPatientUpdateWhenDoctorHasNoAccess() throws Exception {
        // Arrange
        UUID doctorId = UUID.randomUUID();
        UUID practiceId = UUID.randomUUID();
        UUID patientId = UUID.randomUUID();

        Authentication authentication =
                authenticatedDoctor(doctorId, practiceId);

        when(patientService.updatePatient(
                eq(patientId),
                any(UpdatePatientRequest.class)
        )).thenThrow(new PatientAccessDeniedException());


        // Act & Assert

        mockMvc.perform(
                patch("/api/patients/{patientId}", patientId)
                        .with(authentication(authentication))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                        {
                            "firstName": "Mariana"
                        }
                        """)
        )
                .andExpect(status().isForbidden());

        verify(patientService).updatePatient(
                eq(patientId),
                any(UpdatePatientRequest.class)
        );
    }

    @Test
    void shouldRejectPatientUpdateWhenPatientBelongsToAnotherPractice() throws Exception {
        // Arrange
        UUID doctorId = UUID.randomUUID();
        UUID practiceId = UUID.randomUUID();
        UUID patientId = UUID.randomUUID();

        Authentication authentication =
                authenticatedDoctor(doctorId, practiceId);

        when(patientService.updatePatient(
                eq(patientId),
                any(UpdatePatientRequest.class)
        )).thenThrow(new PatientNotFoundException(patientId));

        // Act & Assert
        mockMvc.perform(
                patch("/api/patients/{patientId}", patientId)
                        .with(authentication(authentication))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                        {
                            "firstName": "Mariana"
                        }
                        """)
        )
                .andExpect(status().isNotFound());

        verify(patientService).updatePatient(
                eq(patientId),
                any(UpdatePatientRequest.class)
        );
    }

    @Test
    void shouldDeactivatePatientWhenDoctorHasAccess() throws Exception {
        // Arrange
        UUID doctorId = UUID.randomUUID();
        UUID practiceId = UUID.randomUUID();
        UUID patientId = UUID.randomUUID();

        Authentication authentication =
                authenticatedDoctor(doctorId, practiceId);

        Practice practice = mock(Practice.class);

        Patient patient = new Patient(
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

        assertTrue(patient.isActive());
        patient.deactivate();

        when(patientService.deactivatePatient(patientId))
                .thenReturn(patient);



        // Act & Assert
        mockMvc.perform(
                patch("/api/patients/{patientId}/deactivate", patientId)
                        .with(authentication(authentication))
                        .with(csrf())
        )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.active").value(false))
                .andExpect(jsonPath("$.medicalRecordNumber").value("MR-100"));

        verify(patientService).deactivatePatient(patientId);
    }

    @Test
    void shouldReactivatePatientWhenDoctorHasAccess() throws Exception {
        UUID doctorId = UUID.randomUUID();
        UUID practiceId = UUID.randomUUID();
        UUID patientId = UUID.randomUUID();

        Authentication authentication =
                authenticatedDoctor(doctorId, practiceId);

        Practice practice = mock(Practice.class);

        Patient patient = new Patient(
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

        patient.deactivate();
        assertFalse(patient.isActive());
        patient.activate();
        assertTrue(patient.isActive());

        when(patientService.reactivatePatient(patientId))
                .thenReturn(patient);

        mockMvc.perform(
                        patch("/api/patients/{patientId}/reactivate", patientId)
                                .with(authentication(authentication))
                                .with(csrf())
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.active").value(true))
                .andExpect(jsonPath("$.medicalRecordNumber").value("MR-100"));
        verify(patientService).reactivatePatient(patientId);
    }

    @Test
    void shouldRejectDeactivationWhenDoctorHasNoAccess() throws Exception {
        // Arrange
        UUID doctorId = UUID.randomUUID();
        UUID practiceId = UUID.randomUUID();
        UUID patientId = UUID.randomUUID();

        Authentication authentication =
                authenticatedDoctor(doctorId, practiceId);

        when(patientService.deactivatePatient(patientId))
                .thenThrow(new PatientAccessDeniedException());

        // Act & Assert
        mockMvc.perform(
                patch("/api/patients/{patientId}/deactivate", patientId)
                        .with(authentication(authentication))
                        .with(csrf())
        )
                .andExpect(status().isForbidden());
        verify(patientService).deactivatePatient(patientId);
    }

    @Test
    void shouldRejectReactivationWhenDoctorHasNoAccess() throws Exception {
        // Arrange
        UUID doctorId = UUID.randomUUID();
        UUID practiceId = UUID.randomUUID();
        UUID patientId = UUID.randomUUID();

        Authentication authentication =
                authenticatedDoctor(doctorId, practiceId);

        when(patientService.reactivatePatient(patientId))
                .thenThrow(new PatientAccessDeniedException());

        // Act & Assert
        mockMvc.perform(
                        patch("/api/patients/{patientId}/reactivate", patientId)
                                .with(authentication(authentication))
                                .with(csrf())
                )
                .andExpect(status().isForbidden());
        verify(patientService).reactivatePatient(patientId);
    }

    @Test
    void shouldRejectDeactivationWhenPatientBelongsToAnotherPractice()
            throws Exception {
        // Arrange
        UUID doctorId = UUID.randomUUID();
        UUID practiceId = UUID.randomUUID();
        UUID patientId = UUID.randomUUID();

        Authentication authentication =
                authenticatedDoctor(doctorId, practiceId);

        when(patientService.deactivatePatient(patientId))
                .thenThrow(new PatientNotFoundException(patientId));

        // Act & Assert
        mockMvc.perform(
                        patch("/api/patients/{patientId}/deactivate", patientId)
                                .with(authentication(authentication))
                                .with(csrf())
                )
                .andExpect(status().isNotFound());
        verify(patientService).deactivatePatient(patientId);
    }

    @Test
    void shouldRejectReactivationWhenPatientBelongsToAnotherPractice()
            throws Exception {
        // Arrange
        UUID doctorId = UUID.randomUUID();
        UUID practiceId = UUID.randomUUID();
        UUID patientId = UUID.randomUUID();

        Authentication authentication =
                authenticatedDoctor(doctorId, practiceId);

        when(patientService.reactivatePatient(patientId))
                .thenThrow(new PatientNotFoundException(patientId));

        // Act & Assert
        mockMvc.perform(
                        patch("/api/patients/{patientId}/reactivate", patientId)
                                .with(authentication(authentication))
                                .with(csrf())
                )
                .andExpect(status().isNotFound());
        verify(patientService).reactivatePatient(patientId);
    }

    @Test
    void shouldRejectUnauthenticatedDeactivation() throws Exception {
        // Arrange
        UUID patientId = UUID.randomUUID();

        // Act & Assert
        mockMvc.perform(
                        patch("/api/patients/{patientId}/deactivate", patientId)
                                .with(csrf())
                )
                .andExpect(status().isUnauthorized());

        verify(patientService, never()).deactivatePatient(any(UUID.class));
    }

    @Test
    void shouldRejectUnauthenticatedReactivation() throws Exception {
        // Arrange
        UUID patientId = UUID.randomUUID();

        // Act & Assert
        mockMvc.perform(
                        patch("/api/patients/{patientId}/reactivate", patientId)
                                .with(csrf())
                )
                .andExpect(status().isUnauthorized());

        verify(patientService, never()).reactivatePatient(any(UUID.class));
    }

    @Test
    void shouldRejectDeactivationWithoutCsrfToken() throws Exception {
        // Arrange
        UUID doctorId = UUID.randomUUID();
        UUID practiceId = UUID.randomUUID();
        UUID patientId = UUID.randomUUID();

        Authentication authentication =
                authenticatedDoctor(doctorId, practiceId);

        // Act & Assert
        mockMvc.perform(
                        patch("/api/patients/{patientId}/deactivate", patientId)
                                .with(authentication(authentication))
                )
                .andExpect(status().isForbidden());
        verify(patientService, never()).deactivatePatient(any(UUID.class));
    }

    @Test
    void shouldRejectReactivationWithoutCsrfToken() throws Exception {
        // Arrange
        UUID doctorId = UUID.randomUUID();
        UUID practiceId = UUID.randomUUID();
        UUID patientId = UUID.randomUUID();

        Authentication authentication =
                authenticatedDoctor(doctorId, practiceId);

        // Act & Assert
        mockMvc.perform(
                        patch("/api/patients/{patientId}/reactivate", patientId)
                                .with(authentication(authentication))
                )
                .andExpect(status().isForbidden());
        verify(patientService, never()).reactivatePatient(any(UUID.class));
    }

}
