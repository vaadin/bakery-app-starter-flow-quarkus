package com.vaadin.starter.bakery.backend.service;

import com.vaadin.starter.bakery.backend.data.entity.AbstractEntity;
import io.quarkus.panache.common.Page;
import jakarta.transaction.Transactional;

import java.util.List;
import java.util.Optional;

public interface FilterableCrudService<T extends AbstractEntity> extends CrudService<T> {

    @Transactional(Transactional.TxType.SUPPORTS)
    List<T> findAnyMatching(Optional<String> filter, Page page);

    @Transactional(Transactional.TxType.SUPPORTS)
    long countAnyMatching(Optional<String> filter);
}
