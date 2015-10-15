package com.elastisys.scale.commons.rest.auth;

import org.jose4j.jwt.JwtClaims;
import org.jose4j.jwt.consumer.InvalidJwtException;

/**
 * A validator of JSON Web Token (JWT) authentication tokens used by an
 * {@link AuthTokenRequestFilter}.
 *
 * @see AuthTokenRequestFilter
 */
public interface AuthTokenValidator {

	/**
	 * Deserializes and validates the signature of a JSON Web Token.
	 *
	 * @param signedToken
	 *            A signed and base 64-encoded JSON Web Token.
	 * @return The token claims, if the token could be validated.
	 * @throws InvalidJwtException
	 */
	JwtClaims validate(String signedToken) throws InvalidJwtException;

}
