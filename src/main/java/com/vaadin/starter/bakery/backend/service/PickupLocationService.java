package com.vaadin.starter.bakery.backend.service;

import com.vaadin.starter.bakery.backend.data.entity.PickupLocation;
import com.vaadin.starter.bakery.backend.data.entity.User;
import com.vaadin.starter.bakery.backend.repositories.PickupLocationRepository;
import io.quarkus.panache.common.Page;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class PickupLocationService implements FilterableCrudService<PickupLocation> {

    private final PickupLocationRepository pickupLocationRepository;

    @Inject
    public PickupLocationService(PickupLocationRepository pickupLocationRepository) {
        this.pickupLocationRepository = pickupLocationRepository;
    }

    @Override
    public List<PickupLocation> findAnyMatching(Optional<String> filter, Page page) {
        if (filter.isPresent()) {
            String repositoryFilter = "%" + filter.get() + "%";
            return pickupLocationRepository.findByNameLikeIgnoreCase(repositoryFilter, page);
        } else {
            return pickupLocationRepository.findAll().page(page).list();
        }
    }

    @Override
    public long countAnyMatching(Optional<String> filter) {
        if (filter.isPresent()) {
            String repositoryFilter = "%" + filter.get() + "%";
            return pickupLocationRepository.countByNameLikeIgnoreCase(repositoryFilter);
        } else {
            return pickupLocationRepository.count();
        }
    }

    public PickupLocation getDefault() {
        return findAnyMatching(Optional.empty(), Page.of(0, 1)).iterator().next();
    }

    @Override
    public PickupLocationRepository getRepository() {
        return pickupLocationRepository;
    }

    @Override
    public PickupLocation createNew(User currentUser) {
        return new PickupLocation();
    }
}
