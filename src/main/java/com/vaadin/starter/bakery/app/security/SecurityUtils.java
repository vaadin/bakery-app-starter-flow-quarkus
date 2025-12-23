package com.vaadin.starter.bakery.app.security;

import com.vaadin.flow.server.VaadinSession;
import io.quarkus.security.identity.SecurityIdentity;
import io.quarkus.vertx.http.runtime.security.FormAuthenticationMechanism;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

/**
 * SecurityUtils provides methods to query the current user's security state.
 * Uses Quarkus SecurityIdentity instead of Spring Security's SecurityContextHolder.
 */
@ApplicationScoped
public class SecurityUtils {

    @Inject
    SecurityIdentity identity;

    /**
     * Gets the user name (email) of the currently signed in user.
     *
     * @return the user name of the current user or <code>null</code> if the user
     *         has not signed in
     */
    public String getUsername() {
        if (identity == null || identity.isAnonymous()) {
            return null;
        }
        return identity.getPrincipal().getName();
    }

    /**
     * Checks if the user is logged in.
     *
     * @return true if the user is logged in. False otherwise.
     */
    public boolean isUserLoggedIn() {
        return identity != null && !identity.isAnonymous();
    }

    /**
     * Logs out the currently authenticated user.
     *
     * This method uses the FormAuthenticationMechanism to log out the
     * user associated with the provided SecurityIdentity. It will
     * invalidate the session of the authenticated user.
     */
    public void logout() {
        VaadinSession.getCurrent().getSession().invalidate();
        FormAuthenticationMechanism.logout(identity);
    }
}
