package org.example.springboot;

import java.time.LocalDate;

public record Employee(
		Long id,
		String firstName,
		String lastName,
		String email,
		String jobTitle,
		String department,
		LocalDate hireDate
) {
}
