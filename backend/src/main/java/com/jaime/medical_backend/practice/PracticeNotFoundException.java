package com.jaime.medical_backend.practice;

import java.util.UUID;

public class PracticeNotFoundException extends RuntimeException{
    public PracticeNotFoundException(UUID practiceId) {
        super("Practice not found: " + practiceId);
    }
}
