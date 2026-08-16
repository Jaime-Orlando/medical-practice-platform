package com.jaime.medical_backend.patient;

import com.jaime.medical_backend.practice.Practice;
import jakarta.persistence.*;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "patients", uniqueConstraints = {
        @UniqueConstraint(name = "unique_practice_medical_record", columnNames = {"practice_id", "medical_record_number"}),
        @UniqueConstraint(name = "unique_practice_curp", columnNames = {"practice_id", "curp"})
})
public class Patient {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id")
    private UUID id;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "practice_id", nullable = false)
    private Practice practice;
    @Column(name = "medical_record_number", nullable = false, length = 50)
    private String medicalRecordNumber;
    @Column(name = "first_name", nullable = false)
    private String firstName;
    @Column(name = "paternal_surname", nullable = false)
    private String paternalSurname;
    @Column(name = "maternal_surname")
    private String maternalSurname;
    @Column(name = "birth_date", nullable = false)
    private LocalDate birthDate;
    @Column(name = "sex", nullable = false, length = 20)
    private String sex;
    @Column(name = "curp", length = 18)
    private String curp;
    @Column(name = "email")
    private String email;
    @Column(name = "phone_number", length = 20)
    private String phoneNumber;
    @Column(name = "active", nullable = false)
    private boolean active;
    @Column(name = "created_at", nullable = false, updatable = false, insertable = false)
    private Instant createdAt;
    @Column(name = "updated_at", nullable = false, insertable = false)
    private Instant updatedAt;

    protected Patient() {}

    public Patient(Practice practice, String medicalRecordNumber, String firstName, String paternalSurname, String maternalSurname, LocalDate birthDate, String sex, String curp, String email,
                     String phoneNumber) {
        this.practice = practice;
        this.medicalRecordNumber = medicalRecordNumber;
        this.firstName = firstName;
        this.paternalSurname = paternalSurname;
        this.maternalSurname = maternalSurname;
        this.birthDate = birthDate;
        this.sex = sex;
        this.curp = curp;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.active = true;
    }

    public UUID getId() {
        return id;
    }

    public Practice getPractice() {
        return practice;
    }

    public String getMedicalRecordNumber() {
        return medicalRecordNumber;
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

    public LocalDate getBirthDate() {
        return birthDate;
    }

    public String getSex() {
        return sex;
    }

    public String getCurp() {
        return curp;
    }

    public String getEmail() {
        return email;
    }

    public String getPhoneNumber() {
        return phoneNumber;
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

    public void updateContactInformation(String email, String phoneNumber) {
        this.email = email;
        this.phoneNumber = phoneNumber;
    }

    public void updateName(
            String firstName,
            String paternalSurname,
            String maternalSurname) {
        this.firstName = firstName;
        this.paternalSurname = paternalSurname;
        this.maternalSurname = maternalSurname;
    }




}

