package com.vaadin.starter.bakery.backend.service;

import com.vaadin.starter.bakery.backend.data.entity.Product;
import com.vaadin.starter.bakery.backend.data.entity.User;
import com.vaadin.starter.bakery.backend.repositories.ProductRepository;
import io.quarkus.panache.common.Page;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.PersistenceException;
import jakarta.transaction.Transactional;

import java.util.List;
import java.util.Optional;

@ApplicationScoped
@Transactional
public class ProductService implements FilterableCrudService<Product> {

    private final ProductRepository productRepository;

    @Inject
    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Override
    public List<Product> findAnyMatching(Optional<String> filter, Page page) {
        if (filter.isPresent()) {
            String repositoryFilter = "%" + filter.get() + "%";
            return productRepository.findByNameLikeIgnoreCase(repositoryFilter, page);
        } else {
            return find(page);
        }
    }

    @Override
    public long countAnyMatching(Optional<String> filter) {
        if (filter.isPresent()) {
            String repositoryFilter = "%" + filter.get() + "%";
            return productRepository.countByNameLikeIgnoreCase(repositoryFilter);
        } else {
            return count();
        }
    }

    @Transactional(Transactional.TxType.SUPPORTS)
    public List<Product> find(Page page) {
        return productRepository.findBy(page);
    }

    @Override
    public ProductRepository getRepository() {
        return productRepository;
    }

    @Override
    public Product createNew(User currentUser) {
        return new Product();
    }

    @Override
    @Transactional
    public Product save(User currentUser, Product entity) {
        try {
            return FilterableCrudService.super.save(currentUser, entity);
        } catch (PersistenceException e) {
            throw new UserFriendlyDataException(
                    "There is already a product with that name. Please select a unique name for the product.");
        }
    }
}
