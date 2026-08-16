package com.jaime.medical_backend.doctorpatient;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

@Embeddable
public class DoctorPatientId implements Serializable {

    @Column(name = "doctor_id", nullable = false)
    private UUID doctorId;
    @Column(name = "patient_id", nullable = false)
    private UUID patientId;


    protected DoctorPatientId() {}

    public DoctorPatientId(UUID doctorId, UUID patientId) {
        this.doctorId = doctorId;
        this.patientId = patientId;
    }

    public UUID getDoctorId() {
        return doctorId;
    }

    public UUID getPatientId() {
        return patientId;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }

        if (!(object instanceof DoctorPatientId that)) {
            return false;
        }

        return Objects.equals(doctorId, that.doctorId)
                && Objects.equals(patientId, that.patientId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(doctorId, patientId);
    }



}
