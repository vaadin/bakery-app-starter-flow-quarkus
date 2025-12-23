package com.vaadin.starter.bakery.app.security;

import com.vaadin.flow.server.communication.StreamRequestHandler;
import com.vaadin.flow.shared.ApplicationConstants;
import io.vertx.ext.web.RoutingContext;

import java.io.Serializable;
import java.util.Optional;

import static com.vaadin.flow.server.HandlerHelper.getPathIfInsideServlet;

/**
 * Helper class for handling Vaadin framework requests in Quarkus environment.
 * Based on quarkus-hilla's QuarkusHandlerHelper.
 */
public class QuarkusHandlerHelper implements Serializable {

    /**
     * Checks whether the request is an internal request.
     * <p>
     * Internal requests include UIDL, heartbeat, push, and upload requests
     * that are needed for applications to work.
     * <p>
     * Requests for routes and static resources are not considered internal.
     *
     * @param servletMappingPath the path the Vaadin servlet is mapped to
     * @param request the routing context
     * @return {@code true} if the request is Vaadin internal, {@code false} otherwise
     */
    public static boolean isFrameworkInternalRequest(String servletMappingPath, RoutingContext request) {
        return isFrameworkInternalRequest(
                servletMappingPath,
                getRequestPathInsideContext(request),
                request.request().getParam(ApplicationConstants.REQUEST_TYPE_PARAMETER));
    }

    private static boolean isFrameworkInternalRequest(
            String servletMappingPath, String requestedPath, String requestTypeParameter) {
        Optional<String> requestedPathWithoutServletMapping = getPathIfInsideServlet(servletMappingPath, requestedPath);
        if (requestedPathWithoutServletMapping.isEmpty()) {
            return false;
        } else if (isInternalRequestInsideServlet(requestedPathWithoutServletMapping.get(), requestTypeParameter)) {
            return true;
        } else if (isUploadRequest(requestedPathWithoutServletMapping.get())) {
            return true;
        }
        return false;
    }

    /**
     * Returns the requested path inside the context root.
     *
     * @param request the routing context
     * @return the path inside the context root
     */
    public static String getRequestPathInsideContext(RoutingContext request) {
        String servletPath = request.mountPoint();
        String pathInfo = request.request().path();
        String url = "";
        if (servletPath != null) {
            url += servletPath.startsWith("/") ? servletPath.substring(1) : servletPath;
        }
        if (pathInfo != null) {
            url += pathInfo;
        }
        return url;
    }

    static boolean isInternalRequestInsideServlet(
            String requestedPathWithoutServletMapping, String requestTypeParameter) {
        if (requestedPathWithoutServletMapping == null
                || requestedPathWithoutServletMapping.isEmpty()
                || "/".equals(requestedPathWithoutServletMapping)) {
            return requestTypeParameter != null;
        }
        return false;
    }

    private static boolean isUploadRequest(String requestedPathWithoutServletMapping) {
        return requestedPathWithoutServletMapping.matches(
                StreamRequestHandler.DYN_RES_PREFIX + "(\\d+)/([0-9a-z-]*)/upload");
    }
}
