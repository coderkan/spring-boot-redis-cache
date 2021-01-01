package com.coderkan.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import com.coderkan.models.Customer;

public interface CustomerRepository extends JpaRepository<Customer, Long>{

}

