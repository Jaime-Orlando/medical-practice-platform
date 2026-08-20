package com.jaime.medical_backend.patient;

import java.time.LocalDate;
import java.util.UUID;

public record PatientResponse(
        UUID id,
        String medicalRecordNumber,
        String firstName,
        String paternalSurname,
        String maternalSurname,
        LocalDate birthDate,
        String sex,
        String curp,
        String email,
        String phoneNumber,
        boolean active
) {
    public static PatientResponse from(Patient patient) {
        return new PatientResponse(
                patient.getId(),
                patient.getMedicalRecordNumber(),
                patient.getFirstName(),
                patient.getPaternalSurname(),
                patient.getMaternalSurname(),
                patient.getBirthDate(),
                patient.getSex(),
                patient.getCurp(),
                patient.getEmail(),
                patient.getPhoneNumber(),
                patient.isActive()
        );
    }
}