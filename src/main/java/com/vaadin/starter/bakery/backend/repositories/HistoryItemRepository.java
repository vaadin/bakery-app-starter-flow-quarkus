package com.vaadin.starter.bakery.backend.repositories;

import com.vaadin.starter.bakery.backend.data.entity.HistoryItem;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class HistoryItemRepository implements PanacheRepository<HistoryItem> {
}
