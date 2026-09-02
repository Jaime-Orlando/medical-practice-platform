package com.jaime.medical_backend.patient;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PatientRepository extends JpaRepository<Patient, UUID>, JpaSpecificationExecutor<Patient> {
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
