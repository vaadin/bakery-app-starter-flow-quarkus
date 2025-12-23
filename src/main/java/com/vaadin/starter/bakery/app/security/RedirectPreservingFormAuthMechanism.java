package com.vaadin.starter.bakery.app.security;

import io.quarkus.security.identity.IdentityProviderManager;
import io.quarkus.security.identity.SecurityIdentity;
import io.quarkus.security.identity.request.AuthenticationRequest;
import io.quarkus.vertx.http.runtime.security.ChallengeData;
import io.quarkus.vertx.http.runtime.security.FormAuthenticationMechanism;
import io.quarkus.vertx.http.runtime.security.HttpAuthenticationMechanism;
import io.quarkus.vertx.http.runtime.security.HttpCredentialTransport;
import io.smallrye.mutiny.Uni;
import io.vertx.core.http.Cookie;
import io.vertx.ext.web.RoutingContext;

import java.util.Objects;
import java.util.Set;

/**
 * A wrapper around {@link FormAuthenticationMechanism} that preserves the
 * redirect location cookie.
 * <p>
 * The default Quarkus {@code FormAuthenticationMechanism} overwrites the
 * {@code quarkus-redirect-location} cookie on every unauthenticated request.
 * This causes issues when a service worker makes additional requests (e.g., to
 * '/') after the initial redirect to login, overwriting the original
 * destination URL.
 * <p>
 * This wrapper delegates all calls to the underlying mechanism but intercepts
 * {@link #getChallenge(RoutingContext)} to restore the original cookie value
 * if it was changed by the delegate.
 */
public class RedirectPreservingFormAuthMechanism implements HttpAuthenticationMechanism {

    static final String LOCATION_COOKIE = "quarkus-redirect-location";

    private final FormAuthenticationMechanism delegate;

    public RedirectPreservingFormAuthMechanism(FormAuthenticationMechanism delegate) {
        this.delegate = delegate;
    }

    @Override
    public Uni<SecurityIdentity> authenticate(RoutingContext context,
                                              IdentityProviderManager identityProviderManager) {
        return delegate.authenticate(context, identityProviderManager);
    }

    @Override
    public Uni<ChallengeData> getChallenge(RoutingContext context) {
        Cookie redirectBefore = context.request().getCookie(LOCATION_COOKIE);
        return delegate.getChallenge(context).map(data -> {
            Cookie redirect = context.request().getCookie(LOCATION_COOKIE);
            if (redirect != null && redirectBefore != null &&
                !Objects.equals(redirectBefore.getValue(), redirect.getValue())) {
                redirect.setValue(redirectBefore.getValue());
            }
            return data;
        });
    }

    @Override
    public Uni<Boolean> sendChallenge(RoutingContext context) {
        return HttpAuthenticationMechanism.super.sendChallenge(context);
    }

    @Override
    public Set<Class<? extends AuthenticationRequest>> getCredentialTypes() {
        return delegate.getCredentialTypes();
    }

    @Override
    public Uni<HttpCredentialTransport> getCredentialTransport(RoutingContext context) {
        return delegate.getCredentialTransport(context);
    }

    @Override
    public int getPriority() {
        return 2500; // run before basic auth
    }
}
