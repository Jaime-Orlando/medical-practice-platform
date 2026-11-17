package com.jaime.medical_backend.patient;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Locale;
import java.util.UUID;

@RestController
@RequestMapping("/api/patients")
public class PatientController {

    private final PatientService patientService;

    public PatientController(PatientService patientService) {
        this.patientService = patientService;
    }

    @GetMapping
    public List<PatientSummaryResponse> getPatients(
            @RequestParam(required = false) String query,
            @RequestParam(defaultValue = "active") String status
    ) {
        PatientStatusFilter statusFilter;
        try{
            statusFilter  = PatientStatusFilter.valueOf(status.toUpperCase(Locale.ROOT));
        }
        catch(IllegalArgumentException exception){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid patient status: " + status, exception);
        }

        return patientService.getPatientsForCurrentDoctor(query, statusFilter)
                .stream()
                .map(PatientSummaryResponse::from)
                .toList();
    }

    @GetMapping("/{patientId}")
    public PatientResponse getPatient(@PathVariable UUID patientId) {
        Patient patient =
                patientService.getPatientForCurrentDoctor(patientId);

        return PatientResponse.from(patient);
    }

    @PostMapping
    public ResponseEntity<PatientResponse> createPatient(
            @Valid @RequestBody CreatePatientRequest request) {

        Patient patient = patientService.createPatient(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(PatientResponse.from(patient));
    }

    @PatchMapping("/{patientId}")
    public ResponseEntity<PatientResponse> updatePatient(
            @PathVariable UUID patientId,
            @Valid @RequestBody UpdatePatientRequest request
    ) {
        Patient patient = patientService.updatePatient(patientId, request);
        PatientResponse response = PatientResponse.from(patient);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{patientId}/deactivate")
    public PatientResponse deactivatePatient(@PathVariable UUID patientId) {
        Patient patient = patientService.deactivatePatient(patientId);
        return PatientResponse.from(patient);
    }

    @PatchMapping("/{patientId}/reactivate")
    public PatientResponse reactivatePatient(@PathVariable UUID patientId) {
        Patient patient = patientService.reactivatePatient(patientId);
        return PatientResponse.from(patient);
    }
}