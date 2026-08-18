package com.jaime.medical_backend.patient;

public class PatientAccessDeniedException extends RuntimeException {
    public PatientAccessDeniedException() {
        super("Doctor does not have access to this patient");
    }
}