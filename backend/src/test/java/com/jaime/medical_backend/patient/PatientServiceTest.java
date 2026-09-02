package com.jaime.medical_backend.patient;
import com.jaime.medical_backend.doctor.Doctor;
import com.jaime.medical_backend.doctor.DoctorRepository;
import com.jaime.medical_backend.doctorpatient.DoctorPatient;
import com.jaime.medical_backend.doctorpatient.DoctorPatientRepository;
import com.jaime.medical_backend.practice.Practice;
import com.jaime.medical_backend.practice.PracticeRepository;
import com.jaime.medical_backend.security.CurrentDoctor;
import com.jaime.medical_backend.security.DoctorPrincipal;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class PatientServiceTest {

    @Mock
    private PatientRepository patientRepository;

    @Mock
    private DoctorPatientRepository doctorPatientRepository;

    @Mock
    private PracticeRepository practiceRepository;

    @Mock
    private DoctorRepository doctorRepository;

    @Mock
    private CurrentDoctor currentDoctor;

    @InjectMocks
    private PatientService patientService;

    @Test
    void returnPatientWhenDoctorHasAccess() {
        UUID doctorId = UUID.randomUUID();
        UUID practiceId = UUID.randomUUID();
        UUID patientId = UUID.randomUUID();
        DoctorPrincipal principal = new DoctorPrincipal(
                doctorId,
                practiceId,
                "doctor@example.com",
                "unused",
                true
        );
        when(currentDoctor.principal()).thenReturn(principal);
        Practice practice = new Practice("Test Practice");

        Patient patient = new Patient(
                practice,
                "MR-001",
                "Juan",
                "Perez",
                "Perez",
                LocalDate.of(1990, 1, 1),
                "MALE",
                null,
                null,
                null
        );

        when(patientRepository.findByIdAndPracticeId(patientId, practiceId))
                .thenReturn(Optional.of(patient));

        when(doctorPatientRepository
                .existsByDoctor_IdAndPatient_IdAndActiveTrue(doctorId, patientId))
                .thenReturn(true);

        Patient result = patientService.getPatientForCurrentDoctor(patientId);

        assertSame(patient, result);
        verify(currentDoctor).principal();

        verify(patientRepository)
                .findByIdAndPracticeId(patientId, practiceId);

        verify(doctorPatientRepository)
                .existsByDoctor_IdAndPatient_IdAndActiveTrue(
                        doctorId,
                        patientId
                );
    }

    @Test
    void throwAccessDeniedWhenDoctorHasNoActiveRelationship() {
        UUID doctorId = UUID.randomUUID();
        UUID practiceId = UUID.randomUUID();
        UUID patientId = UUID.randomUUID();

        DoctorPrincipal principal = new DoctorPrincipal(
                doctorId,
                practiceId,
                "doctor@example.com",
                "unused",
                true
        );

        when(currentDoctor.principal()).thenReturn(principal);

        Practice practice = new Practice("Test Practice");
        Patient patient = new Patient(
                practice,
                "MR-001",
                "Juan",
                "Perez",
                "Perez",
                LocalDate.of(1990, 1, 1),
                "MALE",
                null,
                null,
                null
        );

        when(patientRepository.findByIdAndPracticeId(patientId, practiceId))
                .thenReturn(Optional.of(patient));

        when(doctorPatientRepository
                .existsByDoctor_IdAndPatient_IdAndActiveTrue(doctorId, patientId))
                .thenReturn(false);

        assertThrows(
                PatientAccessDeniedException.class,
                () -> patientService.getPatientForCurrentDoctor(patientId)
        );
        verify(currentDoctor).principal();

        verify(patientRepository)
                .findByIdAndPracticeId(patientId, practiceId);

        verify(doctorPatientRepository)
                .existsByDoctor_IdAndPatient_IdAndActiveTrue(
                        doctorId,
                        patientId
                );
    }

    @Test
    void throwNotFoundWhenPatientIsOutsideDoctorsPractice() {
        UUID doctorId = UUID.randomUUID();
        UUID practiceId = UUID.randomUUID();
        UUID patientId = UUID.randomUUID();

        DoctorPrincipal principal = new DoctorPrincipal(
                doctorId,
                practiceId,
                "doctor@example.com",
                "unused",
                true
        );

        when(currentDoctor.principal()).thenReturn(principal);

        when(patientRepository.findByIdAndPracticeId(patientId, practiceId))
                .thenReturn(Optional.empty());

        assertThrows(
                PatientNotFoundException.class,
                () -> patientService.getPatientForCurrentDoctor(patientId)
        );

        verify(currentDoctor).principal();

        verify(patientRepository)
                .findByIdAndPracticeId(patientId, practiceId);

        verify(
                doctorPatientRepository,
                never()
        ).existsByDoctor_IdAndPatient_IdAndActiveTrue(
                doctorId,
                patientId
        );
    }

    @Test
    void createPatientWhenRequestIsValid() {
        UUID doctorId = UUID.randomUUID();
        UUID practiceId = UUID.randomUUID();

        DoctorPrincipal principal = new DoctorPrincipal(
                doctorId,
                practiceId,
                "doctor@example.com",
                "unused",
                true
        );

        Practice practice = mock(Practice.class);
        Doctor doctor = mock(Doctor.class);

        when(practice.getId()).thenReturn(practiceId);
        when(doctor.getId()).thenReturn(doctorId);
        when(doctor.getPractice()).thenReturn(practice);

        when(currentDoctor.principal()).thenReturn(principal);

        when(practiceRepository.findById(practiceId))
                .thenReturn(Optional.of(practice));

        when(doctorRepository.findById(doctorId))
                .thenReturn(Optional.of(doctor));

        CreatePatientRequest request = new CreatePatientRequest(
                "MR-100",
                "Maria",
                "Lopez",
                "Garcia",
                LocalDate.of(1988, 5, 10),
                "FEMALE",
                "TESTCURP123456789",
                "maria@example.com",
                "5551234567"
        );

        when(patientRepository.existsByPracticeIdAndMedicalRecordNumber(
                practiceId,
                "MR-100"
        )).thenReturn(false);

        when(patientRepository.existsByPracticeIdAndCurp(
                practiceId,
                "TESTCURP123456789"
        )).thenReturn(false);

        when(patientRepository.save(any(Patient.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Patient result = patientService.createPatient(request);

        assertEquals("MR-100", result.getMedicalRecordNumber());
        assertEquals("Maria", result.getFirstName());
        assertEquals(practice, result.getPractice());

        ArgumentCaptor<DoctorPatient> captor =
                ArgumentCaptor.forClass(DoctorPatient.class);

        verify(doctorPatientRepository).save(captor.capture());

        DoctorPatient savedRelationship = captor.getValue();

        assertSame(doctor, savedRelationship.getDoctor());
        assertSame(result, savedRelationship.getPatient());
        assertTrue(savedRelationship.isActive());
    }

    @Test
    void throwDuplicateMedicalRecordNumberWhenCreatingPatient() {
        UUID doctorId = UUID.randomUUID();
        UUID practiceId = UUID.randomUUID();

        DoctorPrincipal principal = new DoctorPrincipal(
                doctorId,
                practiceId,
                "doctor@example.com",
                "unused",
                true
        );

        Practice practice = mock(Practice.class);
        Doctor doctor = mock(Doctor.class);

        when(practice.getId()).thenReturn(practiceId);
        when(doctor.getPractice()).thenReturn(practice);

        when(currentDoctor.principal()).thenReturn(principal);

        when(practiceRepository.findById(practiceId))
                .thenReturn(Optional.of(practice));

        when(doctorRepository.findById(doctorId))
                .thenReturn(Optional.of(doctor));

        CreatePatientRequest request = new CreatePatientRequest(
                "MR-100",
                "Maria",
                "Lopez",
                "Garcia",
                LocalDate.of(1988, 5, 10),
                "FEMALE",
                "TESTCURP123456789",
                "maria@example.com",
                "5551234567"
        );

        when(patientRepository.existsByPracticeIdAndMedicalRecordNumber(
                practiceId,
                "MR-100"
        )).thenReturn(true);

        assertThrows(
                DuplicateMedicalRecordNumberException.class,
                () -> patientService.createPatient(request)
        );

        verify(patientRepository, never()).save(any(Patient.class));
        verify(doctorPatientRepository, never()).save(any(DoctorPatient.class));

        verify(
                patientRepository,
                never()
        ).existsByPracticeIdAndCurp(
                any(UUID.class),
                anyString()
        );
    }

    @Test
    void throwDuplicateCurpWhenCreatingPatient() {
        UUID doctorId = UUID.randomUUID();
        UUID practiceId = UUID.randomUUID();

        DoctorPrincipal principal = new DoctorPrincipal(
                doctorId,
                practiceId,
                "doctor@example.com",
                "unused",
                true
        );

        Practice practice = mock(Practice.class);
        Doctor doctor = mock(Doctor.class);

        when(practice.getId()).thenReturn(practiceId);
        when(doctor.getPractice()).thenReturn(practice);

        when(currentDoctor.principal()).thenReturn(principal);

        when(practiceRepository.findById(practiceId))
                .thenReturn(Optional.of(practice));

        when(doctorRepository.findById(doctorId))
                .thenReturn(Optional.of(doctor));

        CreatePatientRequest request = new CreatePatientRequest(
                "MR-101",
                "Maria",
                "Lopez",
                "Garcia",
                LocalDate.of(1988, 5, 10),
                "FEMALE",
                "TESTCURP123456789",
                "maria@example.com",
                "5551234567"
        );

        when(patientRepository.existsByPracticeIdAndMedicalRecordNumber(
                practiceId,
                "MR-101"
        )).thenReturn(false);

        when(patientRepository.existsByPracticeIdAndCurp(
                practiceId,
                "TESTCURP123456789"
        )).thenReturn(true);

        assertThrows(
                DuplicateCurpException.class,
                () -> patientService.createPatient(request)
        );

        verify(patientRepository, never())
                .save(any(Patient.class));

        verify(doctorPatientRepository, never())
                .save(any(DoctorPatient.class));
        verify(patientRepository)
                .existsByPracticeIdAndMedicalRecordNumber(
                        practiceId,
                        "MR-101"
                );

        verify(patientRepository)
                .existsByPracticeIdAndCurp(
                        practiceId,
                        "TESTCURP123456789"
                );
    }

    @Test
    void throwWhenAuthenticatedDoctorPracticeDoesNotMatchSession() {
        UUID doctorId = UUID.randomUUID();
        UUID sessionPracticeId = UUID.randomUUID();
        UUID doctorPracticeId = UUID.randomUUID();

        DoctorPrincipal principal = new DoctorPrincipal(
                doctorId,
                sessionPracticeId,
                "doctor@example.com",
                "unused",
                true
        );

        Practice sessionPractice = mock(Practice.class);
        Practice doctorPractice = mock(Practice.class);
        Doctor doctor = mock(Doctor.class);

        when(doctorPractice.getId()).thenReturn(doctorPracticeId);
        when(doctor.getPractice()).thenReturn(doctorPractice);

        when(currentDoctor.principal()).thenReturn(principal);

        when(practiceRepository.findById(sessionPracticeId))
                .thenReturn(Optional.of(sessionPractice));

        when(doctorRepository.findById(doctorId))
                .thenReturn(Optional.of(doctor));

        CreatePatientRequest request = new CreatePatientRequest(
                "MR-102",
                "Maria",
                "Lopez",
                "Garcia",
                LocalDate.of(1988, 5, 10),
                "FEMALE",
                "TESTCURP123456789",
                "maria@example.com",
                "5551234567"
        );

        assertThrows(
                IllegalStateException.class,
                () -> patientService.createPatient(request)
        );

        verify(patientRepository, never())
                .existsByPracticeIdAndMedicalRecordNumber(
                        any(UUID.class),
                        anyString()
                );

        verify(patientRepository, never())
                .existsByPracticeIdAndCurp(
                        any(UUID.class),
                        anyString()
                );

        verify(patientRepository, never())
                .save(any(Patient.class));

        verify(doctorPatientRepository, never())
                .save(any(DoctorPatient.class));
    }

}
