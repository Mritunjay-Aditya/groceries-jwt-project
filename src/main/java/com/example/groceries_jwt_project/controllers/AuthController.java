// Package declaration: groups this class under your project's controller layer
package com.example.groceries_jwt_project.controllers;

// Import: Spring's dependency injection annotation
import org.springframework.beans.factory.annotation.Autowired;
// Import: HTTP response wrapper utility (status + body)
import org.springframework.http.ResponseEntity;
// Import: Entry point for programmatic authentication (delegates to providers)
import org.springframework.security.authentication.AuthenticationManager;
// Import: Username/password token used by AuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
// Import: Represents the authenticated principal (user) and their authorities
import org.springframework.security.core.Authentication;
// Import: Hashing interface for passwords (BCrypt implementation via bean)
import org.springframework.security.crypto.password.PasswordEncoder;
// Import: Maps HTTP POST to a handler method
import org.springframework.web.bind.annotation.PostMapping;
// Import: Bind request JSON to a Java object
import org.springframework.web.bind.annotation.RequestBody;
// Import: Declares a base URL path for this controller
import org.springframework.web.bind.annotation.RequestMapping;
// Import: Marks class as REST controller (@ResponseBody by default)
import org.springframework.web.bind.annotation.RestController;

import com.example.groceries_jwt_project.entity.Groceries;
import com.example.groceries_jwt_project.entity.User;
import com.example.groceries_jwt_project.repository.UserRepository;
import com.example.groceries_jwt_project.security.JwtUtil;

// =====================
// SWAGGER / OPENAPI IMPORTS
// =====================
import io.swagger.v3.oas.annotations.Operation; // Describes individual endpoint
import io.swagger.v3.oas.annotations.Parameter; // Describes method parameters
import io.swagger.v3.oas.annotations.media.Content; // Describes media (JSON, etc.)
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema; // Describes structure of request/response
import io.swagger.v3.oas.annotations.responses.ApiResponse; // Describes possible responses
import io.swagger.v3.oas.annotations.tags.Tag; // Groups related endpoints under a tag

/**
 * Handles user registration and login to issue JWT token. Teaching notes: -
 * /auth/register is PUBLIC (no token) -> creates user with hashed password. -
 * /auth/login is PUBLIC (no token) -> returns JWT if credentials are valid. -
 * The returned JWT is then sent by the client in Authorization: Bearer <token>.
 */
@RestController // Tells Spring this class exposes REST endpoints (JSON)
@RequestMapping("/auth") // Base path for all endpoints in this controller

// =====================
// @Tag: Groups endpoints in Swagger UI
// Example: In Swagger UI, this will appear as "Authentication Controller"
// =====================
@Tag(name = "Authentication Controller", description = "Endpoints for user registration and login that issue JWT tokens.")
public class AuthController {

	@Autowired // Injects the JPA repository for user existence checks & saves
	private UserRepository repo;

	@Autowired // Injects the configured PasswordEncoder (e.g., BCrypt)
	private PasswordEncoder encoder;

	@Autowired // Injects AuthenticationManager built from your SecurityConfig
	private AuthenticationManager authManager;

	@Autowired // Injects JwtUtil to generate signed tokens
	private JwtUtil jwtUtil;

	// -------------------------------
	// USER REGISTRATION
	// -------------------------------
	@PostMapping("/register") // Maps POST /auth/register

	// =====================
	// @Operation: Describes this endpoint for Swagger UI
	// - summary → short one-line description
	// - description → detailed explanation shown in Swagger docs
	// =====================
	@Operation(summary = "Register a new user", description = "Registers a new user by saving their username and encrypted password. "
			+ "Accessible publicly without JWT. Returns success message if successful.",
			// @ApiResponse: Documents possible responses
			responses = { @ApiResponse(responseCode = "200", description = "User registered successfully"),
					@ApiResponse(responseCode = "400", description = "User already exists", content = @Content(mediaType = "application/json", schema = @Schema(example = "\"User already exists\""))),
					@ApiResponse(responseCode = "500", description = "Internal server error") }

	)
	public ResponseEntity<?> register(
			@io.swagger.v3.oas.annotations.parameters.
			RequestBody(description = "User object containing username, password, and optionally role.", 
			required = true, 
			content = @Content(schema = @Schema(implementation = Groceries.class), 
			examples = @ExampleObject(value = """
					                            {
					  "username":"Aditya",
					  "password":"12345",
					  "role":"ROLE_USER"
					}"""))) 
			@RequestBody User user // Binds JSON body to User
	) {
		// Check if username already exists to prevent duplicates
		if (repo.existsByUsername(user.getUsername())) {
			// 400 Bad Request with simple message if taken
			return ResponseEntity.badRequest().body("User already exists");
		}

		// Hash the raw password before saving (NEVER store plain text)
		user.setPassword(encoder.encode(user.getPassword()));

		// If no role supplied, default to basic application role
		if (user.getRole() == null) {
			user.setRole("ROLE_USER");
		}

		// Persist the new user (username unique constraint recommended)
		repo.save(user);
		// 200 OK with confirmation message
		return ResponseEntity.ok("User registered successfully");
	}

	// -------------------------------
	// USER LOGIN
	// -------------------------------
	@PostMapping("/login") // Maps POST /auth/login

	// =====================
	// @Operation: Describes login endpoint
	// - Shows request format and possible success/failure responses
	// =====================
	@Operation(summary = "Authenticate user and return JWT", description = "Authenticates user credentials. If valid, returns a JWT token to be used in the Authorization header for protected APIs.", responses = {
			@ApiResponse(responseCode = "200", description = "Successful login, JWT token returned", content = @Content(mediaType = "application/json", schema = @Schema(example = "\"eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...\""))),
			@ApiResponse(responseCode = "401", description = "Invalid username or password", content = @Content(mediaType = "application/json", schema = @Schema(example = "\"Invalid username or password\""))) })
	public ResponseEntity<?> login(
			@io.swagger.v3.oas.annotations.parameters.
			RequestBody(description = "User login credentials (username and password).", 
			required = true, 
			content = @Content(schema = @Schema(implementation = User.class), 
			examples = @ExampleObject(value = """
					                            {
					  "username":"Aditya",
					  "password":"12345"
					}""")))
			@RequestBody User loginRequest // Binds
																																														// JSON
																																														// to
																																														// a
																																														// simple
																																														// User
																																														// holder
	) {
		try {
			// Ask AuthenticationManager to authenticate using username & password
			Authentication authentication = authManager.authenticate(
					// Build a UsernamePasswordAuthenticationToken from request fields
					new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));

			// If authentication succeeds, build a JWT for this principal
			String token = jwtUtil.generateToken(authentication);

			// 200 OK with the token string (frontends often prefer a JSON wrapper)
			return ResponseEntity.ok(token);
		} catch (Exception e) {
			// If authentication fails (bad credentials, disabled user, etc.), return 401
			return ResponseEntity.status(401).body("Invalid username or password");
		}
	}
}
