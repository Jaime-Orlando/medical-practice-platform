package com.jaime.medical_backend.patient;

import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/patients")
public class PatientController {

    private final PatientService patientService;

    public PatientController(PatientService patientService) {
        this.patientService = patientService;
    }

    @GetMapping("/{patientId}")
    public PatientResponse getPatient(@PathVariable UUID patientId) {
        Patient patient =
                patientService.getPatientForCurrentDoctor(patientId);

        return PatientResponse.from(patient);
    }
}