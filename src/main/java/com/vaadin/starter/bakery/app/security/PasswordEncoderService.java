package com.vaadin.starter.bakery.app.security;

import io.quarkus.elytron.security.common.BcryptUtil;
import jakarta.enterprise.context.ApplicationScoped;

/**
 * Service for encoding and verifying passwords using BCrypt.
 * Replaces Spring Security's PasswordEncoder.
 */
@ApplicationScoped
public class PasswordEncoderService {

    /**
     * Encodes a raw password using BCrypt.
     *
     * @param rawPassword the raw password to encode
     * @return the BCrypt-hashed password
     */
    public String encode(String rawPassword) {
        return BcryptUtil.bcryptHash(rawPassword);
    }

    /**
     * Verifies that a raw password matches a BCrypt-hashed password.
     *
     * @param rawPassword the raw password to check
     * @param encodedPassword the BCrypt-hashed password to check against
     * @return true if the passwords match, false otherwise
     */
    public boolean matches(String rawPassword, String encodedPassword) {
        return BcryptUtil.matches(rawPassword, encodedPassword);
    }
}
