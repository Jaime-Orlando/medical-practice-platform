package com.jaime.medical_backend.patient;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record CreatePatientRequest(
        @NotBlank
        @Size(max = 50)
        String medicalRecordNumber,

        @NotBlank
        String firstName,

        @NotBlank
        String paternalSurname,

        String maternalSurname,

        @NotNull
        LocalDate birthDate,

        @NotBlank
        @Size(max = 20)
        String sex,

        @Size(max = 18)
        String curp,

        String email,

        @Size(max = 20)
        String phoneNumber
) {
}
