package com.jaime.medical_backend.doctor;

import java.util.UUID;

public class DoctorNotFoundException extends RuntimeException {
    public DoctorNotFoundException(UUID doctorId) {
        super("Doctor not found: " + doctorId);
    }
}
