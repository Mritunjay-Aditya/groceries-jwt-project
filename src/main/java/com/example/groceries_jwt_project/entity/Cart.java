package com.example.groceries_jwt_project.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "cart")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Cart {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId; // from JWT token
    private Long productId;
    private int quantity;
    private double totalPrice; // ✅ Added total price
}