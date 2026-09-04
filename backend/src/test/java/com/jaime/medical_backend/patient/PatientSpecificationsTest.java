package com.jaime.medical_backend.patient;

import com.jaime.medical_backend.doctor.Doctor;
import com.jaime.medical_backend.doctor.DoctorRepository;
import com.jaime.medical_backend.doctorpatient.DoctorPatient;
import com.jaime.medical_backend.doctorpatient.DoctorPatientRepository;
import com.jaime.medical_backend.practice.Practice;
import com.jaime.medical_backend.practice.PracticeRepository;
import com.jaime.medical_backend.specialization.Specialization;
import com.jaime.medical_backend.specialization.SpecializationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class PatientSpecificationsTest {

    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    private PracticeRepository practiceRepository;

    @Autowired
    private DoctorRepository doctorRepository;

    @Autowired
    private DoctorPatientRepository doctorPatientRepository;

    @Autowired
    private SpecializationRepository specializationRepository;

    private Practice practice;
    private Specialization specialization;

    @BeforeEach
    void setUp() {
        practice = practiceRepository.save(
                new Practice("Test Practice")
        );

        specialization = specializationRepository.save(
                new Specialization(
                        "BARIATRICS_TEST",
                        "Bariatric Medicine"
                )
        );

    }

    private Doctor saveDoctor(
            String firstName,
            String paternalSurname,
            String email
    ) {
        return doctorRepository.save(
                new Doctor(
                        practice,
                        specialization,
                        firstName,
                        paternalSurname,
                        null,
                        email,
                        "unused"
                )
        );
    }

    private Patient savePatient(
            String medicalRecordNumber,
            String firstName,
            String paternalSurname,
            String maternalSurname
    ) {
        return patientRepository.save(
                new Patient(
                        practice,
                        medicalRecordNumber,
                        firstName,
                        paternalSurname,
                        maternalSurname,
                        LocalDate.of(1988, 5, 10),
                        "FEMALE",
                        null,
                        null,
                        null
                )
        );
    }

    @Test
    void findActivePatientAssignedToDoctor() {

        Doctor doctor = saveDoctor(
                "Ana",
                "Lopez",
                "ana@example.com"
        );

        Patient patient = savePatient(
                "MR-100",
                "Maria",
                "Lopez",
                "Garcia"
        );

        doctorPatientRepository.save(
                new DoctorPatient(doctor, patient)
        );
        List<Patient> result =
                patientRepository.findAll(
                        PatientSpecifications.activeForDoctor(doctor.getId())
                );

        assertEquals(1, result.size());
        assertEquals("MR-100", result.getFirst().getMedicalRecordNumber());


    }

    @Test
    void shouldExcludePatientAssignedToAnotherDoctor(){

        Doctor doctor1 = saveDoctor(
                "Ana",
                "Lopez",
                "ana@example.com"
        );
        Doctor doctor2 = saveDoctor(
                "John",
                "Doe",
                "john@example.com"
        );


        Patient patient = savePatient(
                "MR-100",
                "Maria",
                "Lopez",
                "Garcia"
        );

        doctorPatientRepository.save(
                new DoctorPatient(doctor1, patient)
        );

        List<Patient> result =
                patientRepository.findAll(
                        PatientSpecifications.activeForDoctor(doctor2.getId())
                );

        assertTrue(result.isEmpty());
    }

    @Test
    void shouldExcludePatientWhenDoctorPatientIsInactive() {

        Doctor doctor = saveDoctor(
                "Ana",
                "Lopez",
                "ana@example.com"
        );

        Patient patient = savePatient(
                "MR-100",
                "Maria",
                "Lopez",
                "Garcia"
        );


        DoctorPatient doctorPatient = new DoctorPatient(doctor, patient);
        doctorPatient.deactivate();

        doctorPatientRepository.save(doctorPatient);

        List<Patient> result =
                patientRepository.findAll(
                        PatientSpecifications.activeForDoctor(doctor.getId())
                );

        assertTrue(result.isEmpty());
    }

    @Test
    void shouldExcludePatientWhenPatientIsInactive() {

        Doctor doctor = saveDoctor(
                "Ana",
                "Lopez",
                "ana@example.com"
        );


        Patient patient = savePatient(
                "MR-100",
                "Maria",
                "Lopez",
                "Garcia"
        );

        patient.deactivate();
        patientRepository.save(patient);

        doctorPatientRepository.save(
                new DoctorPatient(doctor, patient)
        );

        List<Patient> result =
                patientRepository.findAll(
                        PatientSpecifications.activeForDoctor(doctor.getId())
                );

        assertTrue(result.isEmpty());

    }

    @Test
    void shouldMatchPatientByFirstName(){

        Doctor doctor = saveDoctor(
                "Ana",
                "Lopez",
                "ana@example.com"
        );

        Patient patient = savePatient(
                "MR-100",
                "Maria",
                "Lopez",
                "Garcia"
        );

        doctorPatientRepository.save(
                new DoctorPatient(doctor, patient)
        );

        List<Patient> result =
                patientRepository.findAll(
                        PatientSpecifications.activeForDoctor(doctor.getId())
                                .and(PatientSpecifications.matchesSearchTerms("Maria"))
                );

        assertEquals(1, result.size());
        assertEquals("MR-100", result.getFirst().getMedicalRecordNumber());
    }

    @Test
    void shouldMatchPatientByMultipleTerms(){
        Doctor doctor = saveDoctor(
                "Ana",
                "Lopez",
                "ana@example.com"
        );

        Patient patient = savePatient(
                "MR-100",
                "Maria",
                "Lopez",
                "Garcia"
        );

        doctorPatientRepository.save(
                new DoctorPatient(doctor, patient)
        );

        List<Patient> result =
                patientRepository.findAll(
                        PatientSpecifications.activeForDoctor(doctor.getId())
                                .and(PatientSpecifications.matchesSearchTerms("Maria Lopez"))
                );
        assertEquals(1, result.size());
        assertEquals("MR-100", result.getFirst().getMedicalRecordNumber());
    }

    @Test
    void shouldMatchPatientRegardlessOfTermOrder(){
        Doctor doctor = saveDoctor(
                "Ana",
                "Lopez",
                "ana@example.com"
        );

        Patient patient = savePatient(
                "MR-100",
                "Maria",
                "Lopez",
                "Garcia"
        );

        doctorPatientRepository.save(
                new DoctorPatient(doctor, patient)
        );

        List<Patient> result =
                patientRepository.findAll(
                        PatientSpecifications.activeForDoctor(doctor.getId())
                                .and(PatientSpecifications.matchesSearchTerms("Lopez Maria"))
                );
        assertEquals(1, result.size());
        assertEquals("MR-100", result.getFirst().getMedicalRecordNumber());
    }

    @Test
    void shouldMatchPatientByMedicalRecordNumber(){

        Doctor doctor = saveDoctor(
                "Ana",
                "Lopez",
                "ana@example.com"
        );

        Patient patient = savePatient(
                "MR-100",
                "Maria",
                "Lopez",
                "Garcia"
        );

        doctorPatientRepository.save(
                new DoctorPatient(doctor, patient)
        );

        List<Patient> result =
                patientRepository.findAll(
                        PatientSpecifications.activeForDoctor(doctor.getId())
                                .and(PatientSpecifications.matchesSearchTerms("MR-100"))
                );
        assertEquals(1, result.size());
        assertEquals("MR-100", result.getFirst().getMedicalRecordNumber());
    }

    @Test
    void shouldNotCombineSearchTermsAcrossDifferentPatients(){

        Doctor doctor = saveDoctor(
                "Ana",
                "Lopez",
                "ana@example.com"
        );

        Patient patient1 = savePatient(
                "MR-100",
                "Maria",
                "Sanchez",
                "Garcia"
        );
        Patient patient2 = savePatient(
                "MR-101",
                "Juan",
                "Perez",
                "Lopez"
        );

        doctorPatientRepository.save(
                new DoctorPatient(doctor, patient1)
        );
        doctorPatientRepository.save(
                new DoctorPatient(doctor, patient2)
        );

        List<Patient> result =
                patientRepository.findAll(
                        PatientSpecifications.activeForDoctor(doctor.getId())
                                .and(PatientSpecifications.matchesSearchTerms("Maria Perez"))
                );
        assertTrue(result.isEmpty());
    }

    @Test
    void shouldMatchSearchCaseInsensitively(){

        Doctor doctor = saveDoctor(
                "Ana",
                "Lopez",
                "ana@example.com"
        );

        Patient patient = savePatient(
                "MR-100",
                "Maria",
                "Lopez",
                "Garcia"
        );

        doctorPatientRepository.save(
                new DoctorPatient(doctor, patient)
        );

        List<Patient> result =
                patientRepository.findAll(
                        PatientSpecifications.activeForDoctor(doctor.getId())
                                .and(PatientSpecifications.matchesSearchTerms("mArIa lOpEz"))
                );
        assertEquals(1, result.size());
        assertEquals("MR-100", result.getFirst().getMedicalRecordNumber());
    }

    @Test
    void shouldExcludeMatchingPatientAssignedToAnotherDoctor(){

        Doctor doctor1 = saveDoctor(
                "Ana",
                "Lopez",
                "ana@example.com"
        );

        Doctor doctor2 = saveDoctor(
                "John",
                "Doe",
                "john@example.com"
        );

        Patient patient = savePatient(
                "MR-100",
                "Maria",
                "Lopez",
                "Garcia"
        );

        doctorPatientRepository.save(
                new DoctorPatient(doctor1, patient)
        );

        List<Patient> result =
                patientRepository.findAll(
                        PatientSpecifications.activeForDoctor(doctor2.getId())
                                .and(PatientSpecifications.matchesSearchTerms("Maria Lopez"))
                );
        assertTrue(result.isEmpty());
    }
}