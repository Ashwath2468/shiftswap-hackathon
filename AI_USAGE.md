# AI Usage Disclosure

## Purpose

AI assistance was used during the development and debugging of ShiftSwap.

## Areas Where AI Assistance Was Used

- Planning the Spring Boot MVC application structure.
- Developing and reviewing controller, service, model, and repository code.
- Implementing shift claim validation and manager approval workflows.
- Debugging Thymeleaf template parsing and Hibernate lazy-loading errors.
- Implementing claim eligibility feedback in the user interface.
- Adding database-backed Action History recording.
- Preparing application testing steps and project documentation.

## Review and Verification

AI-generated suggestions were reviewed and incorporated into the project as appropriate. The application was compiled, launched locally, and tested through the browser.

The following behaviors were verified during development:

- The application starts and the main page loads.
- Claim validation blocks ineligible claims, including a missing required certification.
- Manager claim restrictions are displayed.
- Shift claims can be submitted for approval.
- Manager approval updates the shift assignment.
- Action History records a successful POST action and retains the entry after an application restart.

## Changes and Limitations

Code suggestions were adapted to the existing project structure and errors encountered during implementation.

The application was tested locally. These tests do not establish that every possible edge case, concurrent request, security scenario, or production deployment requirement has been covered.

## Developer Responsibility

The developer remains responsible for reviewing the implementation, understanding the code, validating behavior, protecting credentials, and ensuring that the final submission meets the hackathon requirements.
