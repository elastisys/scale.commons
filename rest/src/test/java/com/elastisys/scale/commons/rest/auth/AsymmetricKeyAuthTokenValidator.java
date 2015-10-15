package com.elastisys.scale.commons.rest.auth;

import org.jose4j.jwk.RsaJsonWebKey;
import org.jose4j.jwt.JwtClaims;
import org.jose4j.jwt.NumericDate;
import org.jose4j.jwt.consumer.InvalidJwtException;
import org.jose4j.jwt.consumer.JwtConsumer;
import org.jose4j.jwt.consumer.JwtConsumerBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.elastisys.scale.commons.util.time.UtcTime;

/**
 * A simple {@link AuthTokenRequestFilter} that uses the public key of a
 * public/private key pair to validate the signature of an authentication token.
 *
 */
public class AsymmetricKeyAuthTokenValidator implements AuthTokenValidator {
	private static final Logger LOG = LoggerFactory
			.getLogger(AsymmetricKeyAuthTokenValidator.class);
	/**
	 * The public/private key pair used to sign and validate a signature.
	 */
	private final RsaJsonWebKey signatureKeyPair;
	/**
	 * an expected issuer that must be present (in a token's {@code iss} claim)
	 * in order for validation to succeed. Can be <code>null</code>.
	 */
	private String expectedIssuer = null;

	/**
	 * Creates an {@link AsymmetricKeyAuthTokenValidator} with a given signature
	 * key pair.
	 *
	 * @param signatureKeyPair
	 */
	public AsymmetricKeyAuthTokenValidator(RsaJsonWebKey signatureKeyPair) {
		this.signatureKeyPair = signatureKeyPair;
	}

	/**
	 * Sets an expected issuer that must be present (in a token's {@code iss}
	 * claim) in order for validation to succeed.
	 *
	 * @param expectedIssuer
	 * @return
	 */
	public AsymmetricKeyAuthTokenValidator withExpectedIssuer(
			String expectedIssuer) {
		this.expectedIssuer = expectedIssuer;
		return this;
	}

	@Override
	public JwtClaims validate(String signedToken) throws InvalidJwtException {
		JwtConsumerBuilder jwtConsumerBuilder = new JwtConsumerBuilder()
				// verify the signature with the public key
				.setVerificationKey(this.signatureKeyPair.getKey());
		if (this.expectedIssuer != null) {
			jwtConsumerBuilder.setExpectedIssuer(this.expectedIssuer);
		}
		// set time of token expiry evaluation to now
		NumericDate now = NumericDate
				.fromMilliseconds(UtcTime.now().getMillis());
		jwtConsumerBuilder.setEvaluationTime(now);
		JwtConsumer jwtConsumer = jwtConsumerBuilder.build();

		// Deserialize and validate the JWT and process it to the Claims
		JwtClaims authTokenClaims = jwtConsumer.processToClaims(signedToken);
		return authTokenClaims;
	}

}
