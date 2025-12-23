package com.vaadin.starter.bakery.backend.service;

import com.vaadin.starter.bakery.backend.data.entity.AbstractEntity;
import com.vaadin.starter.bakery.backend.data.entity.User;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;

public interface CrudService<T extends AbstractEntity> {

    PanacheRepository<T> getRepository();

    @SuppressWarnings("unchecked")
    default T save(User currentUser, T entity) {
        // merge handles both insert and update
        T merged = (T) getRepository().getEntityManager().merge(entity);
        getRepository().flush();
        return merged;
    }

    default void delete(User currentUser, T entity) {
        if (entity == null) {
            throw new EntityNotFoundException();
        }
        getRepository().delete(entity);
    }

    default void delete(User currentUser, long id) {
        delete(currentUser, load(id));
    }

    @Transactional(Transactional.TxType.SUPPORTS)
    default long count() {
        return getRepository().count();
    }

    @Transactional(Transactional.TxType.SUPPORTS)
    default T load(long id) {
        T entity = getRepository().findByIdOptional(id).orElse(null);
        if (entity == null) {
            throw new EntityNotFoundException();
        }
        return entity;
    }

    T createNew(User currentUser);
}
