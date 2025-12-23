package com.vaadin.starter.bakery.app;

import com.vaadin.flow.server.auth.AccessAnnotationChecker;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;

/**
 * CDI producers for Vaadin classes that don't have CDI annotations.
 */
@ApplicationScoped
public class VaadinProducers {

    @Produces
    @ApplicationScoped
    public AccessAnnotationChecker accessAnnotationChecker() {
        return new AccessAnnotationChecker();
    }
}
