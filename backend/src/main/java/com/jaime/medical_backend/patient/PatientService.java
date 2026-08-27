package com.jaime.medical_backend.patient;

import com.jaime.medical_backend.doctor.Doctor;
import com.jaime.medical_backend.doctor.DoctorNotFoundException;
import com.jaime.medical_backend.doctor.DoctorRepository;
import com.jaime.medical_backend.doctorpatient.DoctorPatient;
import com.jaime.medical_backend.doctorpatient.DoctorPatientRepository;
import com.jaime.medical_backend.practice.Practice;
import com.jaime.medical_backend.practice.PracticeNotFoundException;
import com.jaime.medical_backend.practice.PracticeRepository;
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
    private final PracticeRepository practiceRepository;
    private final DoctorRepository doctorRepository;

    public PatientService(PatientRepository patientRepository, DoctorPatientRepository doctorPatientRepository, CurrentDoctor currentDoctor, PracticeRepository practiceRepository, DoctorRepository doctorRepository) {
        this.patientRepository = patientRepository;
        this.doctorPatientRepository = doctorPatientRepository;
        this.currentDoctor = currentDoctor;
        this.practiceRepository = practiceRepository;
        this.doctorRepository = doctorRepository;
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

    @Transactional
    public Patient createPatient(CreatePatientRequest request){
        DoctorPrincipal principal = currentDoctor.principal();
        Practice practice = practiceRepository.findById(principal.getPracticeId())
                .orElseThrow(() -> new PracticeNotFoundException(principal.getPracticeId()));

        Doctor doctor = doctorRepository.findById(principal.getDoctorId())
                .orElseThrow(() -> new DoctorNotFoundException(principal.getDoctorId()));

        if (!doctor.getPractice().getId().equals(principal.getPracticeId())) {
            throw new IllegalStateException(
                    "Authenticated doctor's practice does not match session"
            );
        }

        if (patientRepository.existsByPracticeIdAndMedicalRecordNumber(
                principal.getPracticeId(),
                request.medicalRecordNumber())) {
            throw new DuplicateMedicalRecordNumberException(
                    request.medicalRecordNumber()
            );
        }

        if (request.curp() != null && !request.curp().isBlank() && patientRepository.existsByPracticeIdAndCurp(principal.getPracticeId(), request.curp())) {
            throw new DuplicateCurpException(request.curp());
            }

        Patient patient = new Patient(
                practice,
                request.medicalRecordNumber(),
                request.firstName(),
                request.paternalSurname(),
                request.maternalSurname(),
                request.birthDate(),
                request.sex(),
                request.curp(),
                request.email(),
                request.phoneNumber()
        );
        Patient savedPatient = patientRepository.save(patient);

        DoctorPatient doctorPatient =
                new DoctorPatient(doctor, savedPatient);

        doctorPatientRepository.save(doctorPatient);
        return savedPatient;
    }
}
