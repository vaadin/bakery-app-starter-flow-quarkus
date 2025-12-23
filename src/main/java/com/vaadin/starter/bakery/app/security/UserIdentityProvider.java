package com.vaadin.starter.bakery.app.security;

import com.vaadin.starter.bakery.backend.data.entity.User;
import com.vaadin.starter.bakery.backend.repositories.UserRepository;
import io.quarkus.security.AuthenticationFailedException;
import io.quarkus.security.identity.AuthenticationRequestContext;
import io.quarkus.security.identity.IdentityProvider;
import io.quarkus.security.identity.SecurityIdentity;
import io.quarkus.security.identity.request.UsernamePasswordAuthenticationRequest;
import io.quarkus.security.runtime.QuarkusSecurityIdentity;
import io.smallrye.mutiny.Uni;
import io.quarkus.arc.Arc;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.NoResultException;

/**
 * Quarkus IdentityProvider that authenticates users against the database.
 * Replaces Spring Security's UserDetailsService.
 */
@ApplicationScoped
public class UserIdentityProvider implements IdentityProvider<UsernamePasswordAuthenticationRequest> {

    @Inject
    UserRepository userRepository;

    @Inject
    PasswordEncoderService passwordEncoder;

    @Override
    public Class<UsernamePasswordAuthenticationRequest> getRequestType() {
        return UsernamePasswordAuthenticationRequest.class;
    }

    @Override
    public Uni<SecurityIdentity> authenticate(UsernamePasswordAuthenticationRequest request,
                                               AuthenticationRequestContext context) {
        return context.runBlocking(() -> {
            // Activate request context for database access
            var requestContext = Arc.container().requestContext();
            if (!requestContext.isActive()) {
                requestContext.activate();
                try {
                    return doAuthenticate(request);
                } finally {
                    requestContext.terminate();
                }
            }
            return doAuthenticate(request);
        });
    }

    private SecurityIdentity doAuthenticate(UsernamePasswordAuthenticationRequest request) {
        String username = request.getUsername();
        String password = new String(request.getPassword().getPassword());

        User user;
        try {
            user = userRepository.findByEmailIgnoreCase(username);
        } catch (NoResultException e) {
            throw new AuthenticationFailedException("Invalid username or password");
        }

        if (user == null) {
            throw new AuthenticationFailedException("Invalid username or password");
        }

        if (!passwordEncoder.matches(password, user.getPasswordHash())) {
            throw new AuthenticationFailedException("Invalid username or password");
        }

        // Build SecurityIdentity with user's role
        return QuarkusSecurityIdentity.builder()
                .setPrincipal(() -> user.getEmail())
                .addRole(user.getRole())
                .build();
    }
}
