package com.jaime.medical_backend.patient;

import java.util.UUID;

public class PatientNotFoundException extends RuntimeException {
    public PatientNotFoundException(UUID patientId) {
        super("Patient with ID " + patientId + " not found");
    }
}
