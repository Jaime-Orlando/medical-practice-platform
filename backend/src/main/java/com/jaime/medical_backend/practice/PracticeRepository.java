package com.jaime.medical_backend.practice;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface PracticeRepository extends JpaRepository<Practice, UUID> {
}
