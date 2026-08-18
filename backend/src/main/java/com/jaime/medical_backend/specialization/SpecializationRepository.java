package com.jaime.medical_backend.specialization;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface SpecializationRepository extends JpaRepository<Specialization, UUID> {
    Optional<Specialization> findByCode(String code);
}
