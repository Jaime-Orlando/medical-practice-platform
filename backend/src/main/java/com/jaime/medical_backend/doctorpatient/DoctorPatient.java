package com.jaime.medical_backend.doctorpatient;

import com.jaime.medical_backend.doctor.Doctor;
import com.jaime.medical_backend.patient.Patient;
import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "doctor_patients")
public class DoctorPatient {
    @EmbeddedId
    private DoctorPatientId id;

    @MapsId("doctorId")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "doctor_id", nullable = false)
    private Doctor doctor;

    @MapsId("patientId")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id", nullable = false)
    private Patient patient;

    @Column(name = "created_at", nullable = false, updatable = false, insertable = false)
    private Instant createdAt;

    @Column(name = "active", nullable = false)
    private boolean active;

    protected DoctorPatient() {}

    public DoctorPatient(Doctor doctor, Patient patient) {
        this.id = new DoctorPatientId(doctor.getId(), patient.getId());
        this.doctor = doctor;
        this.patient = patient;
        this.active = true;
    }

    public DoctorPatientId getId() {
        return id;
    }

    public Doctor getDoctor() {
        return doctor;
    }

    public Patient getPatient() {
        return patient;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public boolean isActive() {
        return active;
    }

    public void deactivate() {
        this.active = false;
    }

    public void activate() {
        this.active = true;
    }


}
