package com.jaime.medical_backend.shared;

import com.jaime.medical_backend.patient.DuplicateCurpException;
import com.jaime.medical_backend.patient.DuplicateMedicalRecordNumberException;
import com.jaime.medical_backend.patient.PatientAccessDeniedException;
import com.jaime.medical_backend.patient.PatientNotFoundException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ApiExceptionHandler {
    @ExceptionHandler(PatientNotFoundException.class)
    public ResponseEntity<Void> handlePatientNotFound(
            PatientNotFoundException exception) {

        return ResponseEntity.notFound().build();
    }

    @ExceptionHandler(PatientAccessDeniedException.class)
    public ResponseEntity<Void> handlePatientAccessDenied(
            PatientAccessDeniedException exception) {

        return ResponseEntity.status(403).build();
    }
    @ExceptionHandler(DuplicateMedicalRecordNumberException.class)
    public ResponseEntity<Void> handleDuplicateMedicalRecordNumber(
            DuplicateMedicalRecordNumberException exception) {

        return ResponseEntity.status(409).build();
    }

    @ExceptionHandler(DuplicateCurpException.class)
    public ResponseEntity<Void> handleDuplicateCurp(
            DuplicateCurpException exception) {

        return ResponseEntity.status(409).build();
    }
}