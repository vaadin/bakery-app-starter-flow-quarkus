package com.vaadin.starter.bakery.backend.repositories;

import com.vaadin.starter.bakery.backend.data.entity.PickupLocation;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import io.quarkus.panache.common.Page;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;

@ApplicationScoped
public class PickupLocationRepository implements PanacheRepository<PickupLocation> {

    public List<PickupLocation> findByNameLikeIgnoreCase(String nameFilter, Page page) {
        return find("where upper(name) like upper(?1)", nameFilter).page(page).list();
    }

    public long countByNameLikeIgnoreCase(String nameFilter) {
        return count("where upper(name) like upper(?1)", nameFilter);
    }
}
