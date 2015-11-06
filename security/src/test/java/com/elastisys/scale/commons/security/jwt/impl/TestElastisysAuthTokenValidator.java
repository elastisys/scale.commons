package com.elastisys.scale.commons.security.jwt.impl;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.joda.time.DateTime;
import org.jose4j.jwk.RsaJsonWebKey;
import org.jose4j.jwk.RsaJwkGenerator;
import org.jose4j.jws.AlgorithmIdentifiers;
import org.jose4j.jws.JsonWebSignature;
import org.jose4j.jwt.JwtClaims;
import org.jose4j.jwt.NumericDate;
import org.jose4j.jwt.consumer.InvalidJwtException;
import org.jose4j.lang.JoseException;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.elastisys.scale.commons.security.jwt.AuthTokenValidator;
import com.elastisys.scale.commons.security.jwt.TestAuthTokenHeaderValidator;
import com.elastisys.scale.commons.util.time.FrozenTime;
import com.elastisys.scale.commons.util.time.UtcTime;

/**
 * Exercises the {@link ElastisysAuthTokenValidator}.
 */
public class TestElastisysAuthTokenValidator {

	private static final String TOKEN_ROLE = "user";
	private static final String TOKEN_SUBJECT = "client@elastisys.com";
	private static final String TOKEN_ISSUER = "Elastisys AB";

	private static final Logger LOG = LoggerFactory
			.getLogger(TestAuthTokenHeaderValidator.class);

	/** The signature key pair used to sign and verify auth tokens. */
	private static RsaJsonWebKey signatureKeyPair;

	/** Object under test. */
	private static AuthTokenValidator validator;

	@BeforeClass
	public static void beforeClass() throws JoseException {
		signatureKeyPair = RsaJwkGenerator.generateJwk(2048);
		signatureKeyPair.setKeyId(TOKEN_ISSUER + "-signkey");
	}

	@Before
	public void beforeTestMethod() throws Exception {
		FrozenTime.setFixed(UtcTime.parse("2015-01-01T12:00:00.000Z"));
		validator = new ElastisysAuthTokenValidator(
				() -> signatureKeyPair.getPublicKey());
	}

	@Test(expected = IllegalArgumentException.class)
	public void validateNullToken() throws InvalidJwtException {
		validator.validate(null);
	}

	@Test(expected = IllegalArgumentException.class)
	public void validateEmptyToken() throws InvalidJwtException {
		validator.validate("");
	}

	@Test
	public void validateValidToken() throws Exception {
		DateTime expiration = UtcTime.now().plusHours(1);
		String signedToken = signToken(TOKEN_ISSUER, signatureKeyPair,
				expiration);
		JwtClaims claims = validator.validate(signedToken);

		assertThat(claims.getIssuer(), is(TOKEN_ISSUER));
		assertThat(claims.getSubject(), is(TOKEN_SUBJECT));
		assertThat(claims.getClaimValue("role"), is(TOKEN_ROLE));
		assertThat(claims.getIssuedAt(),
				is(NumericDate.fromMilliseconds(UtcTime.now().getMillis())));
	}

	@Test
	public void validateExpiredToken() throws Exception {
		DateTime expiration = UtcTime.now().plusHours(1);
		String signedToken = signToken(TOKEN_ISSUER, signatureKeyPair,
				expiration);

		// should work now
		validator.validate(signedToken);

		// expiration time has passed, shoule no longer validate
		FrozenTime.tick(3600);
		try {
			validator.validate(signedToken);
			fail("should not be able to validate an expired token");
		} catch (InvalidJwtException e) {
			// expected
			assertTrue(e.getMessage().contains("no longer valid"));
		}
	}

	@Test
	public void validateTokenWithWrongIssuer() throws Exception {
		String wrongIssuer = "BADGUY Inc.";
		DateTime expiration = UtcTime.now().plusHours(1);
		String signedToken = signToken(wrongIssuer, signatureKeyPair,
				expiration);

		try {
			validator.validate(signedToken);
			fail("should not be able to validate a token with wrong issuer");
		} catch (InvalidJwtException e) {
			// expected
			System.err.println(e.getMessage());
			assertTrue(e.getMessage().contains("Issuer"));
		}
	}

	/**
	 * Signs an JWT authentication token, acting as simulated authentication
	 * endpoint that issues auth tokens.
	 *
	 * @param tokenIssuer
	 * @param signatureKeyPair
	 * @param expirationTime
	 *            Expiration time in minutes to set for {@code exp} claim. Can
	 *            be <code>null</code>, in which case the header is left out.
	 * @return
	 * @throws JoseException
	 */
	private String signToken(String tokenIssuer, RsaJsonWebKey signatureKeyPair,
			DateTime expirationTime) throws JoseException {
		// Create the Claims, which will be the content of the JWT
		JwtClaims claims = new JwtClaims();
		claims.setIssuer(tokenIssuer);
		if (expirationTime != null) {
			claims.setExpirationTime(
					NumericDate.fromMilliseconds(expirationTime.getMillis()));
		}
		claims.setGeneratedJwtId();
		NumericDate now = NumericDate
				.fromMilliseconds(UtcTime.now().getMillis());
		claims.setIssuedAt(now);
		// the subject/principal is whom the token is about
		claims.setSubject(TOKEN_SUBJECT);
		// additional claims
		claims.setClaim("role", TOKEN_ROLE);

		JsonWebSignature jws = new JsonWebSignature();
		jws.setPayload(claims.toJson());
		jws.setKey(signatureKeyPair.getPrivateKey());
		jws.setKeyIdHeaderValue(signatureKeyPair.getKeyId());
		jws.setAlgorithmHeaderValue(AlgorithmIdentifiers.RSA_USING_SHA256);
		return jws.getCompactSerialization();
	}
}
