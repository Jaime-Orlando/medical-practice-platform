package com.jaime.medical_backend.patient;

public class DuplicateCurpException extends  RuntimeException {
    public DuplicateCurpException(String curp) {
        super("Duplicate CURP: " + curp);
    }
}
