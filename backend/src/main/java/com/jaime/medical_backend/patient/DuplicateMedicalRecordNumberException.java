package com.jaime.medical_backend.patient;

public class DuplicateMedicalRecordNumberException extends  RuntimeException {
    public DuplicateMedicalRecordNumberException(String medicalRecordNumber) {
        super("Duplicate Medical Record Number: " + medicalRecordNumber);
    }
}
