package com.elastisys.scale.commons.security.jwt;

import java.security.Principal;

import org.jose4j.jwt.JwtClaims;
import org.jose4j.jwt.MalformedClaimException;

/**
 * A {@link Principal} represented by a set of JSON Web Token claims. The name
 * of the principal is the subject {@code sub} claim of the token. All token
 * claims can be retrieved via {@link #getTokenClaims()}.
 */
public class AuthTokenPrincipal implements Principal {
    /** The authentication token client claims. */
    private final JwtClaims tokenClaims;

    /**
     * Creates an {@link AuthTokenPrincipal} from a given client auth token.
     *
     * @param tokenClaims
     *            The authentication token client claims.
     */
    public AuthTokenPrincipal(JwtClaims tokenClaims) {
        this.tokenClaims = tokenClaims;
    }

    @Override
    public String getName() {
        try {
            return this.tokenClaims.getSubject();
        } catch (MalformedClaimException e) {
            throw new RuntimeException(String.format("failed to extract subject from auth token", e.getMessage()), e);
        }
    }

    /**
     * Returns all authentication token client claims.
     *
     * @return
     */
    public JwtClaims getTokenClaims() {
        return this.tokenClaims;
    }

}
