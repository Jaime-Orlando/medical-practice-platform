-- PostgreSQL migration script to create the identity model tables
create TABLE specializations (
    id uuid PRIMARY KEY,
    code VARCHAR(50) NOT NULL UNIQUE,
    display_name VARCHAR(255) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

create TABLE practices(
    id uuid PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

create TABLE doctors(
    id uuid PRIMARY KEY,
    practice_id uuid NOT NULL references practices(id),
    specialization_id uuid NOT NULL references specializations(id),
    first_name VARCHAR(255) NOT NULL,
    paternal_surname VARCHAR(255) NOT NULL,
    maternal_surname VARCHAR(255),
    email VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

create TABLE patients(
    id uuid PRIMARY KEY,
    practice_id uuid not null references practices(id),
    medical_record_number VARCHAR(50) NOT NULL,
    first_name VARCHAR(255) NOT NULL,
    paternal_surname VARCHAR(255) NOT NULL,
    maternal_surname VARCHAR(255),
    birth_date DATE NOT NULL,
    sex VARCHAR(20) NOT NULL,
    curp VARCHAR(18),
    email VARCHAR(255),
    phone_number VARCHAR(20),
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT unique_practice_medical_record UNIQUE (practice_id, medical_record_number),
    CONSTRAINT unique_practice_curp UNIQUE (practice_id, curp)
);

create TABLE doctor_patients(
    doctor_id uuid NOT NULL references doctors(id),
    patient_id uuid NOT NULL references patients(id),
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    PRIMARY KEY (doctor_id, patient_id)
);

CREATE INDEX idx_patients_name
    ON patients (
                 practice_id,
                 paternal_surname,
                 maternal_surname,
                 first_name
        );

CREATE INDEX idx_doctor_patients_patient_id
    ON doctor_patients (patient_id);

CREATE INDEX idx_doctors_practice_id
    ON doctors (practice_id);

CREATE INDEX idx_doctors_specialization_id
    ON doctors (specialization_id);

INSERT INTO specializations (
    id,
    code,
    display_name
)
VALUES (
           '00000000-0000-0000-0000-000000000001',
           'BARIATRICS',
           'Bariatric Medicine'
       );