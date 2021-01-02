package com.coderkan.controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.coderkan.models.Customer;
import com.coderkan.services.CustomerService;


@RestController
public class CustomerController {
	@Autowired
	private CustomerService customerService;

	@GetMapping(value = "/customers")
	public ResponseEntity<Object> getAllCustomers() {
		List<Customer> customers = this.customerService.getAll();
		return ResponseEntity.ok(customers);
	}

	@GetMapping(value = "/customers/{id}")
	public ResponseEntity<Object> getCustomerById(@PathVariable("id") String id) {
		Long _id = Long.valueOf(id);
		Customer customer = this.customerService.getCustomerById(_id);
		return ResponseEntity.ok(customer);
	}

	@PostMapping(value = "/customers")
	public ResponseEntity<Object> addCustomer(@RequestBody Customer customer) {
		Customer created = this.customerService.add(customer);
		return ResponseEntity.status(HttpStatus.CREATED).body(created);
	}

	@PutMapping(value = "/customers")
	public ResponseEntity<Object> updateCustomer(@RequestBody Customer customer) {
		Customer updated = this.customerService.update(customer);
		return ResponseEntity.ok(updated);
	}

	@DeleteMapping(value = "/customers/{id}")
	public ResponseEntity<Object> deleteCustomerById(@PathVariable("id") String id) {
		Long _id = Long.valueOf(id);
		this.customerService.delete(_id);
		return ResponseEntity.ok().build();
	}
}
