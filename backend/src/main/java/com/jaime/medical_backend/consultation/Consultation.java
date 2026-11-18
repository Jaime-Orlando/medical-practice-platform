package com.jaime.medical_backend;

import com.jaime.medical_backend.doctor.Doctor;
import com.jaime.medical_backend.patient.Patient;
import com.jaime.medical_backend.practice.Practice;
import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "consultations")
public class Consultation {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "practice_id", nullable = false)
    private Practice practice;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "patient_id", nullable = false)
    private Patient patient;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "doctor_id", nullable = false)
    private Doctor doctor;

    @Column(name = "consultation_date", nullable = false)
    private Instant consultationDate;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(name="created_at", insertable = false, updatable = false)
    private Instant createdAt;

    @Column(name="updated_at", insertable = false)
    private Instant updatedAt;

    protected Consultation() {}

    public Consultation(
            Practice practice,
            Patient patient,
            Doctor doctor,
            Instant consultationDate,
            String notes
    ) {
        this.practice = practice;
        this.patient = patient;
        this.doctor = doctor;
        this.consultationDate = consultationDate;
        this.notes = notes;
    }

    public UUID getId() {
        return id;
    }

    public Practice getPractice() {
        return practice;
    }

    public Patient getPatient() {
        return patient;
    }

    public Doctor getDoctor() {
        return doctor;
    }

    public Instant getConsultationDate() {
        return consultationDate;
    }

    public String getNotes() {
        return notes;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
