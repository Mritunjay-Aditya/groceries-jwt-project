package com.example.groceries_jwt_project.service;

import com.example.groceries_jwt_project.entity.Groceries;
import com.example.groceries_jwt_project.repository.GroceriesRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GroceriesService {

    private final GroceriesRepository groceriesRepository;

    // Save new grocery item
    public Groceries save(Groceries grocery) {
        return groceriesRepository.save(grocery);
    }

    // Get all groceries
    public List<Groceries> getAll() {
        return groceriesRepository.findAll();
    }

    // Find grocery by ID
    public Groceries findById(Long id) {
        return groceriesRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Grocery item not found with ID: " + id));
    }

    // Update grocery item
    public Groceries update(Long id, Groceries updatedGrocery) {
        Groceries existing = findById(id);
        existing.setName(updatedGrocery.getName());
        existing.setDescription(updatedGrocery.getDescription());
        existing.setPrice(updatedGrocery.getPrice());
        existing.setQuantity(updatedGrocery.getQuantity());
        return groceriesRepository.save(existing);
    }

    // Delete grocery item
    public void delete(Long id) {
        if (!groceriesRepository.existsById(id)) {
            throw new RuntimeException("Grocery item not found with ID: " + id);
        }
        groceriesRepository.deleteById(id);
    }

    // Reduce stock after purchase
    public void reduceStock(Long id, int quantity) {
        Groceries grocery = findById(id);
        if (grocery.getQuantity() < quantity) {
            throw new RuntimeException("Insufficient stock for product: " + grocery.getName());
        }
        grocery.setQuantity(grocery.getQuantity() - quantity);
        groceriesRepository.save(grocery);
    }
}