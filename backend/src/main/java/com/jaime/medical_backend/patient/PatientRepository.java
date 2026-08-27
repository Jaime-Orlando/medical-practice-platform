package com.jaime.medical_backend.patient;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface PatientRepository extends JpaRepository<Patient, UUID> {
    Optional<Patient> findByIdAndPracticeId(UUID id, UUID practiceId);

    Optional<Patient> findByPracticeIdAndMedicalRecordNumber(UUID practiceId, String medicalRecordNumber);

    Optional<Patient> findByPracticeIdAndCurp(UUID practiceId, String curp);

    boolean existsByPracticeIdAndMedicalRecordNumber(
            UUID practiceId,
            String medicalRecordNumber
    );

    boolean existsByPracticeIdAndCurp(
            UUID practiceId,
            String curp
    );
}
