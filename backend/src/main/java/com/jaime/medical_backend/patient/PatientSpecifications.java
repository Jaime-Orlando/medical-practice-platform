package com.jaime.medical_backend.patient;

import com.jaime.medical_backend.doctorpatient.DoctorPatient;

import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;
import org.springframework.data.jpa.domain.Specification;

import jakarta.persistence.criteria.Predicate;

import java.util.ArrayList;
import java.util.UUID;

public final class PatientSpecifications {

    private PatientSpecifications() {
    }

    public static Specification<Patient> activeForDoctor(UUID doctorId, UUID practiceId) {
        return (root, query, criteriaBuilder) -> {

            Subquery<Integer> subquery =
                    query.subquery(Integer.class);

            Root<DoctorPatient> doctorPatient =
                    subquery.from(DoctorPatient.class);

            subquery.select(criteriaBuilder.literal(1));

            subquery.where(
                    criteriaBuilder.equal(
                            doctorPatient.get("patient"),
                            root
                    ),
                    criteriaBuilder.equal(
                            doctorPatient.get("doctor").get("id"),
                            doctorId
                    ),
                    criteriaBuilder.isTrue(
                            doctorPatient.get("active")
                    )
            );

            return criteriaBuilder.and(
                    criteriaBuilder.isTrue(root.get("active")),
                    criteriaBuilder.equal(root.get("practice").get("id"), practiceId),
                    criteriaBuilder.exists(subquery)
            );
        };
    }

    public static Specification<Patient> matchesSearchTerms(String queryText) {
        return (root, query, criteriaBuilder) -> {
            String[] terms = queryText.trim().toLowerCase().split("\\s+");

            var predicates = new ArrayList<Predicate>();

            for (String term : terms) {
                String pattern = "%" + term + "%";

                predicates.add(
                        criteriaBuilder.or(
                                criteriaBuilder.like(
                                        criteriaBuilder.lower(root.get("firstName")),
                                        pattern
                                ),
                                criteriaBuilder.like(
                                        criteriaBuilder.lower(root.get("paternalSurname")),
                                        pattern
                                ),
                                criteriaBuilder.like(
                                        criteriaBuilder.lower(root.get("maternalSurname")),
                                        pattern
                                ),
                                criteriaBuilder.like(
                                        criteriaBuilder.lower(root.get("medicalRecordNumber")),
                                        pattern
                                )
                        )
                );
            }

            return criteriaBuilder.and(
                    predicates.toArray(new Predicate[0])
            );
        };
    }
}