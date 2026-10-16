# Medical Practice Platform

**Status: backend under development**

A multi-specialty medical practice management platform built as a hands-on backend engineering project using **Java 21, Spring Boot, and PostgreSQL**. The goal is to support doctors managing their patients within a practice, with room for specialty-specific clinical workflows in later phases.

> This is an in-development portfolio project, not a deployed clinical system. It is not intended for real patient data in its current state.

## Implemented backend work

- **Authentication:** session-based doctor login using Spring Security.
- **Authorization:** restricts patient operations by the authenticated doctor and their practice; access checks include doctor-patient associations.
- **Patient workflows:** create and retrieve patients; list and search patients using JPA specifications.
- **Persistence:** PostgreSQL with Spring Data JPA/Hibernate and Flyway database migrations.
- **Tests:** service, controller, security, and database integration tests for patient workflows and access isolation.

The patient search specifications include practice scoping so that search results cannot cross practice boundaries.

## Architecture and technologies

- Java 21 / Spring Boot 4.1
- Spring MVC, Spring Security, Spring Data JPA
- PostgreSQL 17, Flyway, Hibernate
- JUnit and Spring's testing support
- Docker Compose for local PostgreSQL

The initial schema models **specializations, practices, doctors, patients, and doctor-patient associations**. Patients belong to practices and are associated with doctors through a join table.

## Repository structure

- `backend/` — Spring Boot application, database migrations and tests
- `frontend/` — planned web interface (not yet presented as a completed application)
- `docs/` — requirements and database/architecture notes
- `docker-compose.yml` — local PostgreSQL development setup

## Running locally (development only)

Prerequisites: Java 21, Maven (or the provided Maven wrapper), and Docker Compose.

1. From the repository root, run `docker compose up -d` to start PostgreSQL.
2. From `backend/`, run `./mvnw spring-boot:run` (or `mvnw.cmd spring-boot:run` on Windows).
3. Run `./mvnw test` from `backend/` for the test suite.

The current configuration includes **development-only database credentials** checked into local configuration files. Do not reuse these credentials elsewhere, expose the database to untrusted networks, or load real patient information. Configuration hardening and a sensitive-data/history audit remain prerequisites for deployment or use with real patient data.

## Current scope and next steps

The project currently focuses on backend identity, access control, patient workflows, and testing. Specialty-specific intake forms, consultations, longitudinal clinical records, the frontend, production-grade security/configuration, and deployment are **future work**, not completed capabilities.

## Engineering focus

This project is intended to demonstrate backend API design, relational data modeling, authorization boundaries, schema migrations, and automated testing. Its security model is especially important because doctors and practices must not have access to unrelated patient records.
