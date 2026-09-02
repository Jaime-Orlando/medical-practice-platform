package com.jaime.medical_backend.patient;

import java.time.LocalDate;
import java.util.UUID;

public record PatientSummaryResponse(
        UUID id,
        String medicalRecordNumber,
        String firstName,
        String paternalSurname,
        String maternalSurname,
        LocalDate birthDate,
        String phoneNumber
) {
    public static PatientSummaryResponse from(Patient patient) {
        return new PatientSummaryResponse(
                patient.getId(),
                patient.getMedicalRecordNumber(),
                patient.getFirstName(),
                patient.getPaternalSurname(),
                patient.getMaternalSurname(),
                patient.getBirthDate(),
                patient.getPhoneNumber()
        );
    }
}