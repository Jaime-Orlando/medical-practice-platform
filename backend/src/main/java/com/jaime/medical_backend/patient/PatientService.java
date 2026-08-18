package com.jaime.medical_backend.patient;

import com.jaime.medical_backend.doctorpatient.DoctorPatientRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class PatientService {
    private final PatientRepository patientRepository;
    private final DoctorPatientRepository doctorPatientRepository;

    public PatientService(PatientRepository patientRepository, DoctorPatientRepository doctorPatientRepository) {
        this.patientRepository = patientRepository;
        this.doctorPatientRepository = doctorPatientRepository;
    }

    public Patient getPatientForDoctor(UUID patientId, UUID doctorId, UUID practiceId) {
        Patient patient = patientRepository.findByIdAndPracticeId(patientId, practiceId)
                .orElseThrow(() -> new PatientNotFoundException(patientId));

        boolean hasAccess = doctorPatientRepository.existsByDoctor_IdAndPatient_IdAndActiveTrue(doctorId, patientId);
        if (!hasAccess) {
            throw new PatientAccessDeniedException();
        }

        return patient;
    }
}
