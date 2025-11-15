package com.example.groceries_jwt_project.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.groceries_jwt_project.entity.Groceries;

@Repository
public interface GroceriesRepository extends JpaRepository<Groceries, Long> {

}
