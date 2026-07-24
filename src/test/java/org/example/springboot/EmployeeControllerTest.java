package org.example.springboot;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class EmployeeControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private EmployeeRepository employeeRepository;

	@BeforeEach
	void setUp() {
		employeeRepository.clear();
	}

	@Test
	void getAllEmployees_returnsEmptyArrayWhenNoEmployeesExist() throws Exception {
		mockMvc.perform(get("/employees"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$", hasSize(0)));
	}

	@Test
	void getAllEmployees_returnsEmployeeCollectionWithExpectedShape() throws Exception {
		employeeRepository.save(new Employee(
				1L,
				"Ana",
				"Lopez",
				"ana.lopez@example.com",
				"Engineer",
				"Platform",
				LocalDate.of(2024, 1, 15)
		));

		mockMvc.perform(get("/employees"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$", hasSize(1)))
				.andExpect(jsonPath("$[0].id").value(1))
				.andExpect(jsonPath("$[0].firstName").value("Ana"))
				.andExpect(jsonPath("$[0].lastName").value("Lopez"))
				.andExpect(jsonPath("$[0].email").value("ana.lopez@example.com"))
				.andExpect(jsonPath("$[0].jobTitle").value("Engineer"))
				.andExpect(jsonPath("$[0].department").value("Platform"))
				.andExpect(jsonPath("$[0].hireDate").value("2024-01-15"));
	}

	@Test
	void getEmployeeById_returnsEmployeeWhenFound() throws Exception {
		employeeRepository.save(new Employee(
				1L,
				"Ana",
				"Lopez",
				"ana.lopez@example.com",
				"Engineer",
				"Platform",
				LocalDate.of(2024, 1, 15)
		));

		mockMvc.perform(get("/employees/1"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value(1))
				.andExpect(jsonPath("$.firstName").value("Ana"))
				.andExpect(jsonPath("$.lastName").value("Lopez"))
				.andExpect(jsonPath("$.email").value("ana.lopez@example.com"))
				.andExpect(jsonPath("$.jobTitle").value("Engineer"))
				.andExpect(jsonPath("$.department").value("Platform"))
				.andExpect(jsonPath("$.hireDate").value("2024-01-15"));
	}

	@Test
	void getEmployeeById_returnsNotFoundWhenEmployeeDoesNotExist() throws Exception {
		mockMvc.perform(get("/employees/999"))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.message").value("Employee with ID 999 not found"));
	}

	@Test
	void createEmployee_returnsCreatedEmployeeWhenPayloadIsValid() throws Exception {
		String validPayload = """
				{
					"firstName": "David",
					"lastName": "Miller",
					"email": "david.miller@example.com",
					"jobTitle": "Analyst",
					"department": "Finance",
					"hireDate": "2024-03-01"
				}
				""";

		mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/employees")
				.contentType(org.springframework.http.MediaType.APPLICATION_JSON)
				.content(validPayload))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.id").exists())
				.andExpect(jsonPath("$.firstName").value("David"))
				.andExpect(jsonPath("$.lastName").value("Miller"))
				.andExpect(jsonPath("$.email").value("david.miller@example.com"))
				.andExpect(jsonPath("$.jobTitle").value("Analyst"))
				.andExpect(jsonPath("$.department").value("Finance"))
				.andExpect(jsonPath("$.hireDate").value("2024-03-01"));
	}

	@Test
	void createEmployee_returnsBadRequestWhenRequiredFieldIsMissing() throws Exception {
		String invalidPayload = """
				{
					"lastName": "Miller",
					"email": "david.miller@example.com",
					"jobTitle": "Analyst",
					"department": "Finance",
					"hireDate": "2024-03-01"
				}
				""";

		mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/employees")
				.contentType(org.springframework.http.MediaType.APPLICATION_JSON)
				.content(invalidPayload))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.message").value("firstName is required"));
	}

	@Test
	void createEmployee_returnsBadRequestWhenEmailFormatIsInvalid() throws Exception {
		String invalidPayload = """
				{
					"firstName": "David",
					"lastName": "Miller",
					"email": "invalidemail",
					"jobTitle": "Analyst",
					"department": "Finance",
					"hireDate": "2024-03-01"
				}
				""";

		mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/employees")
				.contentType(org.springframework.http.MediaType.APPLICATION_JSON)
				.content(invalidPayload))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.message").value("email format is invalid"));
	}

	@Test
	void updateEmployee_returnsUpdatedEmployeeWhenPayloadIsValidAndEmployeeExists() throws Exception {
		employeeRepository.save(new Employee(
				1L,
				"Ana",
				"Lopez",
				"ana.lopez@example.com",
				"Engineer",
				"Platform",
				LocalDate.of(2024, 1, 15)
		));

		String updatePayload = """
				{
					"firstName": "Ana Maria",
					"lastName": "Lopez-Gomez",
					"email": "ana.lopez@company.com",
					"jobTitle": "Lead Engineer",
					"department": "Infrastructure",
					"hireDate": "2024-01-15"
				}
				""";

		mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put("/employees/1")
				.contentType(org.springframework.http.MediaType.APPLICATION_JSON)
				.content(updatePayload))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value(1))
				.andExpect(jsonPath("$.firstName").value("Ana Maria"))
				.andExpect(jsonPath("$.lastName").value("Lopez-Gomez"))
				.andExpect(jsonPath("$.email").value("ana.lopez@company.com"))
				.andExpect(jsonPath("$.jobTitle").value("Lead Engineer"))
				.andExpect(jsonPath("$.department").value("Infrastructure"))
				.andExpect(jsonPath("$.hireDate").value("2024-01-15"));
	}

	@Test
	void updateEmployee_returnsBadRequestWhenRequiredFieldIsMissing() throws Exception {
		employeeRepository.save(new Employee(
				1L,
				"Ana",
				"Lopez",
				"ana.lopez@example.com",
				"Engineer",
				"Platform",
				LocalDate.of(2024, 1, 15)
		));

		String invalidPayload = """
				{
					"lastName": "Lopez-Gomez",
					"email": "ana.lopez@company.com",
					"jobTitle": "Lead Engineer",
					"department": "Infrastructure",
					"hireDate": "2024-01-15"
				}
				""";

		mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put("/employees/1")
				.contentType(org.springframework.http.MediaType.APPLICATION_JSON)
				.content(invalidPayload))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.message").value("firstName is required"));
	}

	@Test
	void updateEmployee_returnsNotFoundWhenEmployeeDoesNotExist() throws Exception {
		String updatePayload = """
				{
					"firstName": "Ana Maria",
					"lastName": "Lopez-Gomez",
					"email": "ana.lopez@company.com",
					"jobTitle": "Lead Engineer",
					"department": "Infrastructure",
					"hireDate": "2024-01-15"
				}
				""";

		mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put("/employees/999")
				.contentType(org.springframework.http.MediaType.APPLICATION_JSON)
				.content(updatePayload))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.message").value("Employee with ID 999 not found"));
	}

	@Test
	void deleteEmployee_returnsNoContentAndRemovesFromStoreWhenEmployeeExists() throws Exception {
		employeeRepository.save(new Employee(
				1L,
				"Ana",
				"Lopez",
				"ana.lopez@example.com",
				"Engineer",
				"Platform",
				LocalDate.of(2024, 1, 15)
		));

		mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete("/employees/1"))
				.andExpect(status().isNoContent());

		// Verify it no longer appears in GET /employees
		mockMvc.perform(get("/employees"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$", hasSize(0)));
	}

	@Test
	void deleteEmployee_returnsNotFoundWhenEmployeeDoesNotExist() throws Exception {
		mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete("/employees/999"))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.message").value("Employee with ID 999 not found"));
	}

	@Test
	void openApiDocs_exposesEmployeeRoutesAndSchemas() throws Exception {
		mockMvc.perform(get("/v3/api-docs"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.openapi").value("3.0.3"))
				.andExpect(jsonPath("$.info.title").value("Spring Boot AWS CI/CD Employee API"))
				.andExpect(jsonPath("$.paths['/employees'].get").exists())
				.andExpect(jsonPath("$.paths['/employees'].post").exists())
				.andExpect(jsonPath("$.paths['/employees/{id}'].get").exists())
				.andExpect(jsonPath("$.paths['/employees/{id}'].put").exists())
				.andExpect(jsonPath("$.paths['/employees/{id}'].delete").exists())
				.andExpect(jsonPath("$.components.schemas.Employee").exists())
				.andExpect(jsonPath("$.components.schemas.CreateEmployeeRequest").exists())
				.andExpect(jsonPath("$.components.schemas.UpdateEmployeeRequest").exists());
	}

	@Test
	void swaggerUi_isReachableFromTheRunningApplication() throws Exception {
		mockMvc.perform(get("/swagger-ui.html"))
				.andExpect(status().isOk())
				.andExpect(content().string(containsString("Employee API Docs")))
				.andExpect(content().string(containsString("/v3/api-docs")))
				.andExpect(content().string(containsString("GET /employees")))
				.andExpect(content().string(containsString("DELETE /employees/{id}")));
	}
}
