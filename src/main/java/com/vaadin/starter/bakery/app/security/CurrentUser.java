package com.vaadin.starter.bakery.app.security;

import com.vaadin.starter.bakery.backend.data.entity.User;
import com.vaadin.starter.bakery.backend.repositories.UserRepository;
import io.quarkus.security.identity.SecurityIdentity;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.NoResultException;

/**
 * Provides access to the currently authenticated user entity.
 * Uses Quarkus SecurityIdentity to get the current principal.
 */
@ApplicationScoped
public class CurrentUser {

    @Inject
    SecurityIdentity identity;

    @Inject
    UserRepository userRepository;

    /**
     * Gets the User entity for the currently authenticated user.
     *
     * @return the User entity, or null if no user is logged in
     */
    public User getUser() {
        if (identity == null || identity.isAnonymous()) {
            return null;
        }
        String email = identity.getPrincipal().getName();
        try {
            return userRepository.findByEmailIgnoreCase(email);
        } catch (NoResultException e) {
            return null;
        }
    }
}
