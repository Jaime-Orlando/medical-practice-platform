package com.jaime.medical_backend.patient;
import com.jaime.medical_backend.doctorpatient.DoctorPatientRepository;
import com.jaime.medical_backend.practice.Practice;
import com.jaime.medical_backend.security.CurrentDoctor;
import com.jaime.medical_backend.security.DoctorPrincipal;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class PatientServiceTest {

    @Mock
    private PatientRepository patientRepository;

    @Mock
    private DoctorPatientRepository doctorPatientRepository;

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

}
