package org.example.springboot;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

@Service
public class EmployeeService {

	private final EmployeeRepository employeeRepository;

	public EmployeeService(EmployeeRepository employeeRepository) {
		this.employeeRepository = employeeRepository;
	}

	public List<Employee> getAllEmployees() {
		return employeeRepository.findAll();
	}

	public Optional<Employee> getEmployeeById(Long id) {
		return employeeRepository.findById(id);
	}

	public Employee createEmployee(CreateEmployeeRequest request) {
		return employeeRepository.save(
				request.firstName(),
				request.lastName(),
				request.email(),
				request.jobTitle(),
				request.department(),
				request.hireDate()
		);
	}

	public Optional<Employee> updateEmployee(Long id, UpdateEmployeeRequest request) {
		return employeeRepository.update(
				id,
				request.firstName(),
				request.lastName(),
				request.email(),
				request.jobTitle(),
				request.department(),
				request.hireDate()
		);
	}

	public boolean deleteEmployee(Long id) {
		return employeeRepository.delete(id);
	}
}
