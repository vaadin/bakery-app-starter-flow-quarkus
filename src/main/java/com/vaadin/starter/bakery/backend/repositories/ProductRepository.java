package com.vaadin.starter.bakery.backend.repositories;

import com.vaadin.starter.bakery.backend.data.entity.Product;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import io.quarkus.panache.common.Page;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;

@ApplicationScoped
public class ProductRepository implements PanacheRepository<Product> {

    public List<Product> findBy(Page page) {
        return findAll().page(page).list();
    }

    public List<Product> findByNameLikeIgnoreCase(String name, Page page) {
        return find("where upper(name) like upper(?1)", name).page(page).list();
    }

    public long countByNameLikeIgnoreCase(String name) {
        return count("where upper(name) like upper(?1)", name);
    }
}
