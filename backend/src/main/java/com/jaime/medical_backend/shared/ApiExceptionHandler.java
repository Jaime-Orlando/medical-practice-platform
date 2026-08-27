package com.jaime.medical_backend.shared;

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
}