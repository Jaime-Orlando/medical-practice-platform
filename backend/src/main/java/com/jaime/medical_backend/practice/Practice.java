package com.jaime.medical_backend.practice;

import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "practices")
public class Practice {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id")
    private UUID id;
    @Column(name = "name", nullable = false)
    private String name;
    @Column(name = "created_at", nullable = false, updatable = false, insertable = false)
    private Instant createdAt;
    @Column(name = "updated_at", nullable = false, insertable = false)
    private Instant updatedAt;

    protected Practice() {
    }

    public Practice(String name) {
        this.name = name;
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
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

    public void changeName(String name) {
        this.name = name;
    }
}
