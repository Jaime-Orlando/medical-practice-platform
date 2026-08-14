# Database Architecture

## Overview

The Medical Practice Platform uses PostgreSQL.

Database schema changes are managed exclusively through Flyway migrations
located in:

`backend/src/main/resources/db/migration`

Hibernate is configured with `ddl-auto=validate` and must not modify the
database schema.

## Identity Model

The initial identity model consists of:

- `specializations` — medical specialization reference data
- `practices` — medical practices using the platform
- `doctors` — doctors belonging to a practice and specialization
- `patients` — patients scoped to a practice
- `doctor_patients` — controls doctor access to patients

## Relationships

Practice
├── Doctors
└── Patients

Specialization
└── Doctors

Doctor
└── DoctorPatient
    └── Patient

## Patient Identity

Patients are not globally shared across the platform.

Each patient belongs to a practice and is identified internally by a UUID.

Medical record numbers are unique within a practice:

UNIQUE (practice_id, medical_record_number)

CURP, when available, is also unique within a practice:

UNIQUE (practice_id, curp)

Names, email addresses, and phone numbers are not identity keys.

## Doctor Access

A doctor may access a patient only when an active `doctor_patients`
relationship exists.

Doctors and patients must belong to the same practice. This invariant is
currently enforced by the application layer.

## Schema Management

Flyway migrations are immutable once applied.

Current migrations:

- V1 — created temporary infrastructure smoke-test table
- V2 — created identity model
- V3 — removed temporary smoke-test table