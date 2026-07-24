package org.example.springboot;

import java.time.LocalDate;

public record CreateEmployeeRequest(
		String firstName,
		String lastName,
		String email,
		String jobTitle,
		String department,
		LocalDate hireDate
) {
}
