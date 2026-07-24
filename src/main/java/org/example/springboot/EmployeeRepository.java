package org.example.springboot;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.time.LocalDate;

import org.springframework.stereotype.Repository;

@Repository
public class EmployeeRepository {

	private final List<Employee> employees = new ArrayList<>();

	public List<Employee> findAll() {
		return List.copyOf(employees);
	}

	public Optional<Employee> findById(Long id) {
		return employees.stream()
				.filter(e -> e.id().equals(id))
				.findFirst();
	}

	public void save(Employee employee) {
		employees.add(employee);
	}

	public Employee save(String firstName, String lastName, String email, String jobTitle, String department, LocalDate hireDate) {
		long nextId = employees.stream()
				.mapToLong(Employee::id)
				.max()
				.orElse(0L) + 1;
		Employee employee = new Employee(nextId, firstName, lastName, email, jobTitle, department, hireDate);
		employees.add(employee);
		return employee;
	}

	public Optional<Employee> update(Long id, String firstName, String lastName, String email, String jobTitle, String department, LocalDate hireDate) {
		for (int i = 0; i < employees.size(); i++) {
			if (employees.get(i).id().equals(id)) {
				Employee updated = new Employee(id, firstName, lastName, email, jobTitle, department, hireDate);
				employees.set(i, updated);
				return Optional.of(updated);
			}
		}
		return Optional.empty();
	}

	public boolean delete(Long id) {
		return employees.removeIf(e -> e.id().equals(id));
	}

	public void clear() {
		employees.clear();
	}
}
