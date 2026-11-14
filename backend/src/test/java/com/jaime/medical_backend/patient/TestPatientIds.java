package com.jaime.medical_backend.patient;

import java.util.UUID;

public record TestPatientIds(
        UUID practiceId,
        UUID doctorId,
        UUID patientId
) {}