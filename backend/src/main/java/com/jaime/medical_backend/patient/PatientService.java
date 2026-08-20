package com.jaime.medical_backend.patient;

import com.jaime.medical_backend.doctorpatient.DoctorPatientRepository;
import com.jaime.medical_backend.security.CurrentDoctor;
import com.jaime.medical_backend.security.DoctorPrincipal;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class PatientService {
    private final PatientRepository patientRepository;
    private final DoctorPatientRepository doctorPatientRepository;
    private final CurrentDoctor currentDoctor;

    public PatientService(PatientRepository patientRepository, DoctorPatientRepository doctorPatientRepository, CurrentDoctor currentDoctor) {
        this.patientRepository = patientRepository;
        this.doctorPatientRepository = doctorPatientRepository;
        this.currentDoctor = currentDoctor;
    }

    public Patient getPatientForCurrentDoctor(UUID patientId) {
        DoctorPrincipal principal = currentDoctor.principal();
        Patient patient = patientRepository.findByIdAndPracticeId(patientId, principal.getPracticeId())
                .orElseThrow(() -> new PatientNotFoundException(patientId));

        boolean hasAccess = doctorPatientRepository.existsByDoctor_IdAndPatient_IdAndActiveTrue(principal.getDoctorId(), patientId);
        if (!hasAccess) {
            throw new PatientAccessDeniedException();
        }

        return patient;
    }
}
