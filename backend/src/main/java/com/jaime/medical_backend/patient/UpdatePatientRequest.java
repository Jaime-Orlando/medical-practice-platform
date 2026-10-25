package com.jaime.medical_backend.patient;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UpdatePatientRequest(

        @Pattern(regexp = ".*\\S.*", message = "First name must not be blank")
        String firstName,
        @Pattern(regexp = ".*\\S.*", message = "Paternal surname must not be blank")
        String paternalSurname,
        @Pattern(regexp = ".*\\S.*", message = "Maternal surname must not be blank")
        String maternalSurname,
        @Email
        String email,
        @Size(max = 20)
        String phoneNumber
) {
}