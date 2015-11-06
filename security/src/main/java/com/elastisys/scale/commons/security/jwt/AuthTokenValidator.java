package com.elastisys.scale.commons.security.jwt;

import org.jose4j.jwt.JwtClaims;

/**
 * A validator of JSON Web Token (JWT) authentication tokens. Validation
 * includes deserializing the base 64-encoded token, validating its signature,
 * and verifying that the token claims look legitimate.
 * <p/>
 * Note that the {@link AuthTokenValidator} implementation typically needs to be
 * seeded with a signature validation key (for example, a public key from the
 * key pair whose private key was used to sign the token).
 */
public interface AuthTokenValidator {

	/**
	 * Deserializes and validates the signature of a JSON Web Token and checks
	 * the validity of the token claims.
	 *
	 * @param signedToken
	 *            A signed and base 64-encoded JSON Web Token.
	 * @return The token claims, if the token could be validated.
	 * @throws AuthTokenValidationException
	 *             on failure to validate the authentication token
	 */
	JwtClaims validate(String signedToken) throws AuthTokenValidationException;
}
