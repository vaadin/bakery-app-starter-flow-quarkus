package com.vaadin.starter.bakery.app.security;

import com.vaadin.flow.router.Location;
import com.vaadin.flow.router.QueryParameters;
import com.vaadin.flow.router.internal.NavigationRouteTarget;
import com.vaadin.flow.router.internal.RouteTarget;
import com.vaadin.flow.server.HandlerHelper;
import com.vaadin.flow.server.RouteRegistry;
import com.vaadin.flow.server.ServiceInitEvent;
import com.vaadin.flow.server.VaadinService;
import com.vaadin.flow.server.auth.AccessCheckDecision;
import com.vaadin.flow.server.auth.AccessCheckResult;
import com.vaadin.flow.server.auth.NavigationAccessControl;
import com.vaadin.flow.server.auth.NavigationContext;
import com.vaadin.starter.bakery.ui.views.login.LoginView;
import io.quarkus.arc.Arc;
import io.quarkus.runtime.Startup;
import io.quarkus.security.identity.SecurityIdentity;
import io.quarkus.vertx.http.runtime.security.AuthenticatedHttpSecurityPolicy;
import io.quarkus.vertx.http.runtime.security.FormAuthenticationMechanism;
import io.quarkus.vertx.http.runtime.security.HttpSecurityPolicy;
import io.quarkus.vertx.http.runtime.security.ImmutablePathMatcher;
import io.smallrye.mutiny.Uni;
import io.vertx.ext.web.RoutingContext;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import org.jboss.logging.Logger;

import java.security.Principal;
import java.util.Arrays;
import java.util.stream.Stream;

/**
 * Quarkus HttpSecurityPolicy that integrates with Vaadin's NavigationAccessControl.
 * Based on quarkus-hilla's HillaSecurityPolicy pattern.
 */
@Startup
@ApplicationScoped
public class FlowSecurityPolicy implements HttpSecurityPolicy {

    private static final Logger LOGGER = Logger.getLogger(FlowSecurityPolicy.class);

    private final AuthenticatedHttpSecurityPolicy authenticatedPolicy;
    private ImmutablePathMatcher<Boolean> permitAllMatcher;
    private NavigationAccessControl accessControl;
    private VaadinService vaadinService;


    public FlowSecurityPolicy() {
        this.authenticatedPolicy = new AuthenticatedHttpSecurityPolicy();
        buildPathMatcher();
    }

    private void buildPathMatcher() {
        ImmutablePathMatcher.ImmutablePathMatcherBuilder<Boolean> builder = ImmutablePathMatcher.builder();

        // Add all Vaadin public resources from HandlerHelper
        Stream.of(
                HandlerHelper.getPublicResources(),
                HandlerHelper.getPublicResourcesRoot(),
                HandlerHelper.getPublicResourcesRequiringSecurityContext()
        ).flatMap(Arrays::stream)
         .map(FlowSecurityPolicy::normalizeWildcard)
         .forEach(p -> builder.addPath(p, true));

        this.permitAllMatcher = builder.build();
    }

    /**
     * Convert Ant-style wildcards to Quarkus path matching format.
     */
    private static String normalizeWildcard(String path) {
        // Replace ** with * for Quarkus ImmutablePathMatcher
        return path.replace("**", "*");
    }

    void onVaadinServiceInit(@Observes ServiceInitEvent event) {
        this.vaadinService = event.getSource();

        // Create NavigationAccessControl with custom principal/role suppliers from Quarkus SecurityIdentity
        accessControl = new NavigationAccessControl();
        accessControl.setLoginView(LoginView.class);

        event.getSource().addUIInitListener(uiEvent -> {
            uiEvent.getUI().addBeforeEnterListener(accessControl);
        });
    }

    @Override
    public Uni<CheckResult> checkPermission(RoutingContext request,
                                             Uni<SecurityIdentity> identity,
                                             AuthorizationRequestContext requestContext) {
        String path = request.normalizedPath();

        // 1. Check permit-all paths via ImmutablePathMatcher
        ImmutablePathMatcher.PathMatch<Boolean> match = permitAllMatcher.match(path);
        if (match.getValue() != null && match.getValue()) {
            LOGGER.tracef("Path %s matches permit-all pattern", path);
            return Uni.createFrom().item(CheckResult.PERMIT);
        }

        // 2. Check framework internal requests (UIDL, heartbeat, push, uploads)
        if (QuarkusHandlerHelper.isFrameworkInternalRequest("/*", request)) {
            LOGGER.tracef("Path %s is a framework internal request", path);
            return Uni.createFrom().item(CheckResult.PERMIT);
        }

        // 3. Check anonymous routes (views with @AnonymousAllowed)
        NavigationContext navContext = tryCreateNavigationContext(path);
        if (isAnonymousRoute(navContext, path)) {
            return Uni.createFrom().item(CheckResult.PERMIT);
        }

        // 4. Require authentication for everything else
        return authenticatedPolicy.checkPermission(request, identity, requestContext);
    }

    /**
     * Try to create a NavigationContext for the given path to check route-level access.
     */
    private NavigationContext tryCreateNavigationContext(String path) {
        if (vaadinService == null) {
            return null;
        }

        RouteRegistry routeRegistry = vaadinService.getRouter().getRegistry();
        String normalizedPath = path.startsWith("/") ? path.substring(1) : path;

        NavigationRouteTarget target = routeRegistry.getNavigationRouteTarget(normalizedPath);
        if (target == null) {
            return null;
        }

        RouteTarget routeTarget = target.getRouteTarget();
        if (routeTarget == null) {
            return null;
        }

        Class<?> targetClass = routeTarget.getTarget();
        if (targetClass == null) {
            return null;
        }

        return new NavigationContext(
                vaadinService.getRouter(),
                targetClass,
                new Location(normalizedPath, QueryParameters.empty()),
                target.getRouteParameters(),
                null,
                role -> false,
                false
        );
    }

    /**
     * Check if the route is anonymous (publicly accessible).
     */
    private boolean isAnonymousRoute(NavigationContext navigationContext, String path) {
        if (vaadinService == null) {
            LOGGER.warn("VaadinService not set. Cannot determine server route for " + path);
            return true;  // Allow if service not ready
        }
        if (navigationContext == null) {
            LOGGER.tracef("No route defined for %s", path);
            return true;  // No route = allow (might be static resource)
        }
        if (!accessControl.isEnabled()) {
            boolean productionMode = vaadinService.getDeploymentConfiguration().isProductionMode();
            if (productionMode) {
                LOGGER.debugf("Navigation Access Control is disabled for %s", path);
            } else {
                LOGGER.infof("Navigation Access Control is disabled for %s", path);
            }
            return true;
        }

        boolean productionMode = vaadinService.getDeploymentConfiguration().isProductionMode();
        AccessCheckResult result = accessControl.checkAccess(navigationContext, productionMode);
        boolean isAllowed = result.decision() == AccessCheckDecision.ALLOW;

        if (isAllowed) {
            LOGGER.debugf("%s refers to a public view", path);
        } else {
            LOGGER.debugf("Access to %s denied by Flow navigation access control", path);
        }

        return isAllowed;
    }
}
