//Package declaration: adjust to your project structure
package com.example.groceries_jwt_project;

//Swagger & OpenAPI imports
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.security.SecurityScheme.In;
import io.swagger.v3.oas.models.security.SecurityScheme.Type;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * SwaggerConfig sets up OpenAPI (Swagger UI) documentation.
 *
 * ✅ Explains the API project ✅ Adds server URLs (local/dev/prod) ✅ Enables JWT
 * Bearer authorization button ✅ Shows title, description, version, contact, and
 * license info
 */
@Configuration // Marks this as a configuration class for Spring Boot
public class SwaggerConfig {

	/**
	 * Defines the OpenAPI specification for Swagger UI.
	 */
	@Bean
	public OpenAPI customOpenAPI() {

		// 1️ Define SecurityScheme for JWT Bearer Token
		SecurityScheme bearerAuth = new SecurityScheme().type(Type.HTTP) // Use HTTP auth
				.scheme("bearer") // Bearer authentication
				.bearerFormat("JWT") // Format hint for Swagger UI
				.in(In.HEADER) // Token will be sent via Authorization header
				.name("Authorization"); // Header name

		// 2️ Add Security Requirement so Swagger knows to apply Bearer to endpoints
		SecurityRequirement securityRequirement = new SecurityRequirement().addList("bearerAuth");

		// 3️ Add Info section (project metadata)
		Info apiInfo = new Info().title("JWT Authentication & Authorization API") // Swagger UI main title
				.version("1.0.0") // API version
				.description("""
						This project is a microservices-based e-commerce system 
						built with Spring Boot, featuring two independent services: 
						User Service for user authentication, product management, 
						and order handling, and Payment Service for processing payments. 
						Both services use PostgreSQL for persistence and expose REST APIs 
						documented with Swagger/RestAPI. The User Service implements Spring 
						Security with JWT for secure authentication and leverages Spring 
						Cloud OpenFeign to communicate with the Payment Service during 
						order processing. The architecture ensures modularity, scalability, 
						and security, making it suitable for modern distributed applications

						**Features:**
						- Secure REST APIs with JWT tokens.
						- Role-based access control (ROLE_ADMIN and ROLE_USER).
						- Integrated Swagger UI for API documentation and testing.

						**Authentication Flow:**
						1. Register a new user via `/auth/register` (Public endpoint).
						2. Login via `/auth/login` to receive a JWT token.
						3. Use the "Authorize" button in Swagger UI to provide the token (without quotes).
						4. Access secured endpoints:
						   - **Admin**: Can create, update, and delete products.
						   - **User**: Can view products and manage cart.

						**Endpoints Overview:**
						- `/auth/register` → Public, creates a new user.
						- `/auth/login` → Public, authenticates user and returns JWT token.
						- `/api/groceries` → CRUD operations (Admin only for create/update/delete).
						- `/api/cart` → Add, view, remove items, and checkout (User only).

						**Usage Notes:**
						- All secured endpoints require a Bearer token in the `Authorization` header.
						- Swagger UI helps test APIs easily after authentication.
						""")
				.contact(new Contact().name("HCLTech").url("https://groceries.com").email("contact@groceries.com"))
				.license(new License().name("Apache 2.0").url("https://www.apache.org/licenses/LICENSE-2.0.html"));

		// 4️ Define Server URLs for Swagger UI dropdown
		Server localServer = new Server().url("http://localhost:8090").description("Local Development Server");

		Server prodServer = new Server().url("https://api.yourdomain.com").description("Production Server");

		// 5️ Return the OpenAPI object with all components combined
		return new OpenAPI().info(apiInfo).addServersItem(localServer).addServersItem(prodServer)
				.addSecurityItem(securityRequirement).schemaRequirement("bearerAuth", bearerAuth);
	}
}