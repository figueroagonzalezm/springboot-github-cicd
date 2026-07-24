package org.example.springboot;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
@RestController
@RequestMapping("/employees")
public class EmployeeController {

	private final EmployeeService employeeService;

	public EmployeeController(EmployeeService employeeService) {
		this.employeeService = employeeService;
	}

	@GetMapping
	public List<Employee> getAllEmployees() {
		return employeeService.getAllEmployees();
	}

	@GetMapping("/{id}")
	public ResponseEntity<Object> getEmployeeById(@PathVariable Long id) {
		return employeeService.getEmployeeById(id)
				.<ResponseEntity<Object>>map(ResponseEntity::ok)
				.orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND)
						.body(new ErrorResponse("Employee with ID " + id + " not found")));
	}

	@PostMapping
	public ResponseEntity<Object> createEmployee(@RequestBody CreateEmployeeRequest request) {
		try {
			validateCreateRequest(request);
			Employee created = employeeService.createEmployee(request);
			return ResponseEntity.status(HttpStatus.CREATED).body(created);
		} catch (IllegalArgumentException e) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ErrorResponse(e.getMessage()));
		}
	}

	@PutMapping("/{id}")
	public ResponseEntity<Object> updateEmployee(@PathVariable Long id, @RequestBody UpdateEmployeeRequest request) {
		try {
			validateUpdateRequest(request);
			return employeeService.updateEmployee(id, request)
					.<ResponseEntity<Object>>map(ResponseEntity::ok)
					.orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND)
							.body(new ErrorResponse("Employee with ID " + id + " not found")));
		} catch (IllegalArgumentException e) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ErrorResponse(e.getMessage()));
		}
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<Object> deleteEmployee(@PathVariable Long id) {
		boolean deleted = employeeService.deleteEmployee(id);
		if (deleted) {
			return ResponseEntity.noContent().build();
		} else {
			return ResponseEntity.status(HttpStatus.NOT_FOUND)
					.body(new ErrorResponse("Employee with ID " + id + " not found"));
		}
	}

	private void validateCreateRequest(CreateEmployeeRequest request) {
		if (request == null) {
			throw new IllegalArgumentException("Payload is required");
		}
		if (request.firstName() == null || request.firstName().isBlank()) {
			throw new IllegalArgumentException("firstName is required");
		}
		if (request.lastName() == null || request.lastName().isBlank()) {
			throw new IllegalArgumentException("lastName is required");
		}
		if (request.email() == null || request.email().isBlank()) {
			throw new IllegalArgumentException("email is required");
		}
		if (!isValidEmail(request.email())) {
			throw new IllegalArgumentException("email format is invalid");
		}
		if (request.jobTitle() == null || request.jobTitle().isBlank()) {
			throw new IllegalArgumentException("jobTitle is required");
		}
		if (request.department() == null || request.department().isBlank()) {
			throw new IllegalArgumentException("department is required");
		}
		if (request.hireDate() == null) {
			throw new IllegalArgumentException("hireDate is required");
		}
	}

	private void validateUpdateRequest(UpdateEmployeeRequest request) {
		if (request == null) {
			throw new IllegalArgumentException("Payload is required");
		}
		if (request.firstName() == null || request.firstName().isBlank()) {
			throw new IllegalArgumentException("firstName is required");
		}
		if (request.lastName() == null || request.lastName().isBlank()) {
			throw new IllegalArgumentException("lastName is required");
		}
		if (request.email() == null || request.email().isBlank()) {
			throw new IllegalArgumentException("email is required");
		}
		if (!isValidEmail(request.email())) {
			throw new IllegalArgumentException("email format is invalid");
		}
		if (request.jobTitle() == null || request.jobTitle().isBlank()) {
			throw new IllegalArgumentException("jobTitle is required");
		}
		if (request.department() == null || request.department().isBlank()) {
			throw new IllegalArgumentException("department is required");
		}
		if (request.hireDate() == null) {
			throw new IllegalArgumentException("hireDate is required");
		}
	}

	private boolean isValidEmail(String email) {
		return email != null && email.contains("@") && email.indexOf("@") > 0 && email.indexOf("@") < email.length() - 1;
	}

	public record ErrorResponse(String message) {}
}
