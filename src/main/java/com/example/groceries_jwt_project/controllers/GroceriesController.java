package com.example.groceries_jwt_project.controllers;

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

import com.example.groceries_jwt_project.entity.Groceries;
import com.example.groceries_jwt_project.service.GroceriesService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/groceries")
@Tag(name = "Groceries", description = "Groceries CRUD (create/update/delete require ADMIN)")
@RequiredArgsConstructor
public class GroceriesController {

    private final GroceriesService groceriesService;

    @Operation(summary = "Create product (ADMIN only)",
            description = "Requires JWT with ROLE_ADMIN",
            security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Created",
                    content = @Content(schema = @Schema(implementation = Groceries.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    @PostMapping
    public ResponseEntity<Groceries> create(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "New product payload",
                    required = true,
                    content = @Content(schema = @Schema(implementation = Groceries.class),
                            examples = @ExampleObject(value = """
                            {
                              "name": "apple",
                              "description": "Red apple",
                              "price": 600,
                              "quantity": 50
                            }"""))
            )
            @RequestBody Groceries product) {
        return new ResponseEntity<>(groceriesService.save(product), HttpStatus.CREATED);
    }

    @Operation(summary = "List products", description = "Public in this demo")
    @ApiResponses(@ApiResponse(responseCode = "200", description = "OK",
            content = @Content(array = @ArraySchema(schema = @Schema(implementation = Groceries.class)))))
    @GetMapping
    public ResponseEntity<List<Groceries>> findAllProducts() {
        return ResponseEntity.ok(groceriesService.getAll());
    }

    @Operation(summary = "Get product by id", description = "Public in this demo")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK",
                    content = @Content(schema = @Schema(implementation = Groceries.class))),
            @ApiResponse(responseCode = "404", description = "Not Found")
    })
    @GetMapping("/{id}")
    public ResponseEntity<Groceries> findProduct(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(groceriesService.findById(id));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @Operation(summary = "Update product (ADMIN only)",
            description = "Requires JWT with ROLE_ADMIN",
            security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Updated",
                    content = @Content(schema = @Schema(implementation = Groceries.class))),
            @ApiResponse(responseCode = "404", description = "Not Found")
    })
    @PutMapping("/{id}")
    public ResponseEntity<Groceries> updateProduct(@PathVariable Long id,
                                                   @RequestBody Groceries newProduct) {
        try {
            return ResponseEntity.ok(groceriesService.update(id, newProduct));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @Operation(summary = "Delete product (ADMIN only)",
            description = "Requires JWT with ROLE_ADMIN",
            security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "No Content"),
            @ApiResponse(responseCode = "404", description = "Not Found")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
        try {
            groceriesService.delete(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
}