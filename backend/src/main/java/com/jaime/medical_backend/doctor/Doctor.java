package com.jaime.medical_backend.doctor;

import com.jaime.medical_backend.practice.Practice;
import com.jaime.medical_backend.specialization.Specialization;
import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "doctors")
public class Doctor {
    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "practice_id", nullable = false)
    private Practice practice;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "specialization_id", nullable = false)
    private Specialization specialization;
    @Column(name = "first_name", nullable = false)
    private String firstName;
    @Column(name = "paternal_surname", nullable = false)
    private String paternalSurname;
    @Column(name = "maternal_surname")
    private String maternalSurname;
    @Column(name = "email", nullable = false, unique = true)
    private String email;
    @Column(name = "password_hash", nullable = false)
    private String passwordHash;
    @Column(name = "active", nullable = false)
    private boolean active;
    @Column(name = "created_at", nullable = false, updatable = false, insertable = false)
    private Instant createdAt;
    @Column(name = "updated_at", nullable = false, insertable = false)
    private Instant updatedAt;

    protected Doctor() {
    }

    public Doctor(Practice practice, Specialization specialization, String firstName, String paternalSurname, String maternalSurname, String email, String passwordHash) {
        this.practice = practice;
        this.specialization = specialization;
        this.firstName = firstName;
        this.paternalSurname = paternalSurname;
        this.maternalSurname = maternalSurname;
        this.email = email;
        this.active = true; // default value
        this.passwordHash = passwordHash;
    }

    public UUID getId() {
        return id;
    }

    public Practice getPractice() {
        return practice;
    }

    public Specialization getSpecialization() {
        return specialization;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getPaternalSurname() {
        return paternalSurname;
    }

    public String getMaternalSurname() {
        return maternalSurname;
    }

    public String getEmail() {
        return email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public boolean isActive() {
        return active;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    @PreUpdate
    private void onUpdate() {
        this.updatedAt = Instant.now();
    }

    public void deactivate() {
        this.active = false;
    }

    public void activate() {
        this.active = true;
    }

}
