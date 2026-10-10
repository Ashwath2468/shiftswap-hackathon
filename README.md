# ShiftSwap — Clinic Staff Shift Exchange

## Project Overview

ShiftSwap is a web-based clinic staff shift exchange system. Staff members can post shifts for exchange, other eligible staff members can claim available shifts, and managers can approve or reject claims.

The application validates shift eligibility before allowing a claim and records actions in a database-backed Action History.

## Features

- Select an acting staff member or clinic manager for demo purposes.
- View clinic shifts, assigned staff, and required certifications.
- Post and withdraw shift listings.
- Claim eligible shifts and cancel pending claims.
- Validate certifications, overlapping shifts, minimum rest periods, and weekly working hours.
- Allow managers to approve or reject pending claims.
- Keep the original shift assignment until manager approval.
- Record successful and failed actions in Action History.
- Persist application data in MySQL.

## Technology Stack

- Java
- Spring Boot
- Spring MVC
- Spring Data JPA / Hibernate
- Thymeleaf
- MySQL
- Maven

## Prerequisites

- JDK 17 or a compatible version required by the project configuration.
- MySQL Server.
- Git, if cloning the repository.

## Database Setup

1. Start MySQL.
2. Create the database configured in `src/main/resources/application.properties`:

   ```sql
   CREATE DATABASE shiftswap_seed_test;
   ```

3. Configure the MySQL URL, username, and password in `src/main/resources/application.properties` to match your local setup.
4. If the password property uses `${DB_PASSWORD}`, configure the `DB_PASSWORD` environment variable before starting the application.
5. Run the application. On an empty database, the application loads the organizer-provided staff and shift data from `seed.json`.

Do not commit real database passwords or other credentials to GitHub.

## Run the Application

Open PowerShell in the project directory and run:

```powershell
.\mvnw.cmd compile
.\mvnw.cmd spring-boot:run
```

Open the application at:

http://localhost:8090/

If the database configuration uses environment variables, make sure they are configured in the same terminal before starting the application.

## Demo Workflow

1. Select a staff member from the Acting User dropdown.
2. Review the available clinic shifts.
3. Post a shift listing as its assigned staff member.
4. Select another staff member and review claim eligibility.
5. Claim an eligible listing.
6. Select the clinic manager to approve or reject the pending claim.
7. Review Action History to see recorded actions and outcomes.

## Shift Claim Rules

A claim is subject to the following checks:

1. Staff cannot claim their own shift.
2. Required certifications must be satisfied.
3. Assigned shifts must not overlap.
4. At least 11 hours of rest must separate non-overlapping assigned shifts.
5. Weekly assigned working hours must not exceed 40 hours.

A pending claim does not change the shift assignment. Reassignment occurs only after manager approval.

## AI Usage

See `AI_USAGE.md` for documentation of AI-assisted development and verification.

## Notes

This project is a hackathon demonstration. The Acting User selector simulates different roles; it is not a production authentication system.
