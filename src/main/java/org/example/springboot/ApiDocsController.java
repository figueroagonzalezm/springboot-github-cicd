package org.example.springboot;

import java.util.List;
import java.util.Map;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ApiDocsController {

	@GetMapping(value = "/v3/api-docs", produces = MediaType.APPLICATION_JSON_VALUE)
	public Map<String, Object> openApiDocument() {
		return Map.of(
				"openapi", "3.0.3",
				"info", Map.of(
						"title", "Spring Boot AWS CI/CD Employee API",
						"version", "1.0.0",
						"description", "Interactive documentation for the employee CRUD API."
				),
				"paths", Map.of(
						"/employees", Map.of(
								"get", operation("List employees", "Returns the current employee collection.", List.of(
										response("200", "Employee collection returned successfully")
								)),
								"post", operation("Create employee", "Creates a new employee.", List.of(
										response("201", "Employee created"),
										response("400", "Validation failed")
								))
						),
						"/employees/{id}", Map.of(
								"get", operation("Read employee", "Returns one employee by ID.", List.of(
										response("200", "Employee found"),
										response("404", "Employee not found")
								)),
								"put", operation("Replace employee", "Replaces an employee by ID.", List.of(
										response("200", "Employee updated"),
										response("400", "Validation failed"),
										response("404", "Employee not found")
								)),
								"delete", operation("Delete employee", "Deletes an employee by ID.", List.of(
										response("204", "Employee deleted"),
										response("404", "Employee not found")
								))
						)
				),
				"components", Map.of(
						"schemas", Map.of(
								"Employee", Map.of(
										"type", "object",
										"properties", Map.of(
												"id", Map.of("type", "integer", "format", "int64"),
												"firstName", Map.of("type", "string"),
												"lastName", Map.of("type", "string"),
												"email", Map.of("type", "string"),
												"jobTitle", Map.of("type", "string"),
												"department", Map.of("type", "string"),
												"hireDate", Map.of("type", "string", "format", "date")
										),
										"required", List.of("id", "firstName", "lastName", "email", "jobTitle", "department", "hireDate")
								),
								"CreateEmployeeRequest", employeeInputSchema(),
								"UpdateEmployeeRequest", employeeInputSchema(),
								"ErrorResponse", Map.of(
										"type", "object",
										"properties", Map.of(
												"message", Map.of("type", "string")
										)
								)
						)
				)
		);
	}

	@GetMapping(value = {"/swagger-ui.html", "/swagger-ui/index.html"}, produces = MediaType.TEXT_HTML_VALUE)
	public ResponseEntity<String> swaggerUi() {
		return ResponseEntity.ok()
				.contentType(MediaType.TEXT_HTML)
				.body("""
						<!doctype html>
						<html lang="en">
						<head>
						  <meta charset="utf-8">
						  <meta name="viewport" content="width=device-width, initial-scale=1">
						  <title>Employee API Docs</title>
						  <style>
						    body { font-family: Arial, sans-serif; margin: 2rem; line-height: 1.5; }
						    .card { border: 1px solid #ddd; border-radius: 8px; padding: 1rem; margin-bottom: 1rem; }
						    code, pre { background: #f6f8fa; padding: 0.25rem 0.4rem; border-radius: 4px; }
						    input, button, textarea { font: inherit; margin: 0.25rem 0; }
						    textarea { width: 100%; min-height: 9rem; }
						  </style>
						</head>
						<body>
						  <h1>Employee API Docs</h1>
						  <p>OpenAPI document: <a href="/v3/api-docs">/v3/api-docs</a></p>
						  <div class="card">
						    <h2>Endpoints</h2>
						    <ul>
						      <li><code>GET /employees</code></li>
						      <li><code>GET /employees/{id}</code></li>
						      <li><code>POST /employees</code></li>
						      <li><code>PUT /employees/{id}</code></li>
						      <li><code>DELETE /employees/{id}</code></li>
						    </ul>
						  </div>
						  <div class="card">
						    <h2>Try the API</h2>
						    <p>Use the form below to call an endpoint directly from the running application.</p>
						    <label for="method">Method</label><br>
						    <select id="method">
						      <option>GET</option>
						      <option>POST</option>
						      <option>PUT</option>
						      <option>DELETE</option>
						    </select><br>
						    <label for="path">Path</label><br>
						    <input id="path" value="/employees" size="40"><br>
						    <label for="payload">Request body</label><br>
						    <textarea id="payload">{}</textarea><br>
						    <button onclick="sendRequest()">Send</button>
						    <pre id="output"></pre>
						  </div>
						  <script>
						    async function sendRequest() {
						      const method = document.getElementById('method').value;
						      const path = document.getElementById('path').value;
						      const payload = document.getElementById('payload').value;
						      const options = { method, headers: { 'Content-Type': 'application/json' } };
						      if (method !== 'GET' && method !== 'DELETE') {
						        options.body = payload;
						      }
						      const response = await fetch(path, options);
						      const text = await response.text();
						      document.getElementById('output').textContent = `Status: ${response.status}\n${text}`;
						    }
						  </script>
						</body>
						</html>
						""");
	}

	private Map<String, Object> operation(String summary, String description, List<Map<String, Object>> responses) {
		return Map.of(
				"summary", summary,
				"description", description,
				"responses", responseMap(responses)
		);
	}

	private Map<String, Object> response(String status, String description) {
		return Map.of(
				"status", status,
				"description", description
		);
	}

	private Map<String, Object> responseMap(List<Map<String, Object>> responses) {
		return responses.stream().collect(java.util.stream.Collectors.toMap(
				response -> (String) response.get("status"),
				response -> Map.of("description", response.get("description"))
		));
	}

	private Map<String, Object> employeeInputSchema() {
		return Map.of(
				"type", "object",
				"properties", Map.of(
						"firstName", Map.of("type", "string"),
						"lastName", Map.of("type", "string"),
						"email", Map.of("type", "string"),
						"jobTitle", Map.of("type", "string"),
						"department", Map.of("type", "string"),
						"hireDate", Map.of("type", "string", "format", "date")
				),
				"required", List.of("firstName", "lastName", "email", "jobTitle", "department", "hireDate")
		);
	}
}
