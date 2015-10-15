package com.elastisys.scale.commons.rest.auth;

import java.security.Principal;

import javax.ws.rs.core.SecurityContext;

import org.jose4j.jwt.JwtClaims;
import org.jose4j.jwt.MalformedClaimException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A {@link SecurityContext} for a request with a valid JSON Web Token
 * authentication. The context holds the {@link JwtClaims} carried in the token
 * that was used to authenticate the client.
 *
 * @see AuthTokenRequestFilter
 */
public class AuthTokenSecurityContext implements SecurityContext {
	static Logger LOG = LoggerFactory.getLogger(AuthTokenSecurityContext.class);

	/**
	 * The name of the authentication scheme used to authenticate the request.
	 */
	public static final String JWT_AUTH_SCHEME = "JSON Web Token";

	/** The client claims extracted from the authentication token. */
	private final JwtClaims tokenClaims;

	/**
	 * Indicates whether this request was made using a secure channel, such as
	 * HTTPS.
	 */
	private final boolean secure;

	/**
	 * Creates an {@link AuthTokenSecurityContext} for a given account.
	 *
	 * @param tokenClaims
	 *            The authentication token client claims.
	 * @param secure
	 *            Indicates whether this request was made using a secure
	 *            channel, such as HTTPS.
	 */
	public AuthTokenSecurityContext(JwtClaims tokenClaims, boolean secure) {
		this.tokenClaims = tokenClaims;
		this.secure = secure;
	}

	@Override
	public Principal getUserPrincipal() {
		return new Principal() {
			@Override
			public String getName() {
				try {
					return AuthTokenSecurityContext.this.tokenClaims
							.getSubject();
				} catch (MalformedClaimException e) {
					throw new RuntimeException(String.format(
							"failed to extract subject from auth token",
							e.getMessage()), e);
				}
			}
		};
	}

	@Override
	public boolean isUserInRole(String role) {
		// at the moment, we don't have any roles stored in the JWT
		return false;
	}

	@Override
	public boolean isSecure() {
		return this.secure;
	}

	@Override
	public String getAuthenticationScheme() {
		return JWT_AUTH_SCHEME;
	}

}
