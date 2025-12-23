package com.vaadin.starter.bakery.backend.repositories;

import com.vaadin.starter.bakery.backend.data.entity.Customer;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class CustomerRepository implements PanacheRepository<Customer> {
}
