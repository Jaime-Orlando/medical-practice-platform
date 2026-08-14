package com.jaime.medical_backend.specialization;

import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "specializations")
public class Specialization {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id")
    private UUID id;
    @Column(name = "code", nullable = false, unique = true, length = 50)
    private String code;
    @Column(name = "display_name", nullable = false)
    private String displayName;
    @Column(name = "created_at", nullable = false, updatable = false, insertable = false)
    private Instant createdAt;

    protected Specialization() {}

    public Specialization(String code, String displayName) {
        this.code = code;
        this.displayName = displayName;
    }

    public UUID getId() {
        return id;
    }

    public String getCode() {
        return code;
    }

    public String getDisplayName() {
        return displayName;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

}
