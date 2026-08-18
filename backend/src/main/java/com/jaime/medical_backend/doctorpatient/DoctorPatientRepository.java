package com.jaime.medical_backend.doctorpatient;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface DoctorPatientRepository extends JpaRepository<DoctorPatient, UUID> {
    boolean existsByDoctor_IdAndPatient_IdAndActiveTrue(
            UUID doctorId,
            UUID patientId
    );
}
