package com.jaime.medical_backend.patient;

import com.jaime.medical_backend.practice.Practice;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

public class PatientSummaryResponseTest {
    @Test
    void shouldIncludeInactiveStatusInSummary() {
        Practice practice = new Practice("Test Practice");

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
                null
        );

        patient.deactivate();

        PatientSummaryResponse response =
                PatientSummaryResponse.from(patient);

        assertFalse(response.active());
        assertEquals("MR-100", response.medicalRecordNumber());
    }
}
