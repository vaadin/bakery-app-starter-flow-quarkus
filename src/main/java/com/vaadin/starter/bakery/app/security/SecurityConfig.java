package com.vaadin.starter.bakery.app.security;

import io.quarkus.vertx.http.runtime.security.FormAuthenticationMechanism;
import io.quarkus.vertx.http.security.Form;
import io.quarkus.vertx.http.security.HttpSecurity;
import jakarta.enterprise.event.Observes;

public class SecurityConfig {

    void configure(@Observes HttpSecurity httpSecurity, FlowSecurityPolicy policy) {
        // Create standard form auth mechanism
        FormAuthenticationMechanism delegate = (FormAuthenticationMechanism) Form.builder()
                .httpOnlyCookie()
                .loginPage("/login")
                .postLocation("/login")
                .errorPage("/login?error=true")
                .landingPage("/")
                .usernameParameter("username")
                .passwordParameter("password")
                .locationCookie(RedirectPreservingFormAuthMechanism.LOCATION_COOKIE)
                .build();

        // Wrap with mechanism that preserves redirect cookie
        RedirectPreservingFormAuthMechanism formAuth = new RedirectPreservingFormAuthMechanism(delegate);
        httpSecurity.mechanism(formAuth);

        httpSecurity.path("/login").permit();

        // Static resources
        httpSecurity.get("/icons/*", "/images/*", "/styles.css").permit();

        httpSecurity.path("/*").policy(policy);

    }

}
