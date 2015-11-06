package com.elastisys.scale.commons.security.jwt;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.joda.time.DateTime;
import org.jose4j.jwk.RsaJsonWebKey;
import org.jose4j.jwk.RsaJwkGenerator;
import org.jose4j.jws.AlgorithmIdentifiers;
import org.jose4j.jws.JsonWebSignature;
import org.jose4j.jwt.JwtClaims;
import org.jose4j.jwt.NumericDate;
import org.jose4j.lang.JoseException;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.elastisys.scale.commons.util.time.FrozenTime;
import com.elastisys.scale.commons.util.time.UtcTime;
import com.google.common.base.Charsets;
import com.google.common.base.Joiner;
import com.google.common.io.BaseEncoding;

/**
 * Verifies the behavior of the {@link AuthTokenHeaderValidator}.
 */
public class TestAuthTokenHeaderValidator {
	private static final String TOKEN_ISSUER = "Elastisys";

	private static final Logger LOG = LoggerFactory
			.getLogger(TestAuthTokenHeaderValidator.class);

	/** The signature key pair used to sign and verify auth tokens. */
	private RsaJsonWebKey signatureKeyPair;

	/** Object under test. */
	private static AuthTokenHeaderValidator validator;

	@Before
	public void beforeTestMethod() throws Exception {
		FrozenTime.setFixed(UtcTime.parse("2015-01-01T12:00:00.000Z"));
		this.signatureKeyPair = RsaJwkGenerator.generateJwk(2048);
		this.signatureKeyPair.setKeyId(TOKEN_ISSUER + "-signkey");

		AsymmetricKeyAuthTokenValidator tokenValidator = new AsymmetricKeyAuthTokenValidator(
				this.signatureKeyPair).withExpectedIssuer(TOKEN_ISSUER);
		validator = new AuthTokenHeaderValidator(tokenValidator);
	}

	/**
	 * The {@link AuthTokenHeaderValidator} needs a {@link AuthTokenValidator}
	 * to delegate work to.
	 */
	@Test(expected = IllegalArgumentException.class)
	public void createWithoutTokenValidator() {
		new AuthTokenHeaderValidator(null);
	}

	@Test(expected = IllegalArgumentException.class)
	public void validateNullHeader() throws AuthTokenValidationException {
		validator.validate(null);
	}

	@Test
	public void validateMalformedHeader() {
		// should not contain an equals sign
		String malformedHeader = "Bearer = eyJhbGciOiJIUzI1";
		try {
			validator.validate(malformedHeader);
			fail("validation should have failed");
		} catch (AuthTokenValidationException e) {
			assertValidationException(e,
					"failed to validate Authorization token",
					"malformed Authorization Bearer token");
		}
	}

	@Test
	public void validateMalformedToken() {
		String malformedToken = "eyJhbGciOiJIUzI1";
		String authzHeader = "Bearer " + malformedToken;
		try {
			validator.validate(authzHeader);
			fail("validation should have failed");
		} catch (AuthTokenValidationException e) {
			assertValidationException(e,
					"failed to validate Authorization token",
					"malformed Authorization Bearer token");
		}
	}

	@Test
	public void validateValidAuthToken() throws Exception {
		DateTime expirationTime = UtcTime.now().plusMinutes(10);
		String signedToken = signToken(TOKEN_ISSUER, this.signatureKeyPair,
				expirationTime);
		String authzHeader = "Bearer " + signedToken;
		JwtClaims claims = validator.validate(authzHeader);
		// TODO: check claims
	}

	/**
	 * Validation of an authentication token signed with the wrong key (or some
	 * other party) should not be validated.
	 */
	@Test
	public void validateForgedAuthToken() throws Exception {
		DateTime expirationTime = UtcTime.now().plusMinutes(10);
		RsaJsonWebKey wrongKeyPair = RsaJwkGenerator.generateJwk(2048);
		String forgedToken = signToken(TOKEN_ISSUER, wrongKeyPair,
				expirationTime);
		String authzHeader = "Bearer " + forgedToken;
		try {
			validator.validate(authzHeader);
			fail("validation should have failed");
		} catch (AuthTokenValidationException e) {
			assertValidationException(e,
					"failed to validate Authorization token",
					"invalid auth token signature");
		}
	}

	/**
	 * Validation of an auth token that has been tampered with (modify the
	 * claims part of the token) should fail.
	 */
	@Test
	public void validateTamperedAuthToken() throws Exception {

		DateTime expirationTime = UtcTime.now().plusMinutes(10);
		String legitToken = signToken(TOKEN_ISSUER, this.signatureKeyPair,
				expirationTime);
		// modify the claims part in an attempt to try and reuse a token but
		// issue it for a different client subject
		LOG.debug("legitimate token: {}", legitToken);
		// try to modify the signature part of the token
		// <B64-encoded header>.<B64-encoded claims>.<B64-encoded signature>
		String[] parts = legitToken.split("\\.");
		String claims = new String(BaseEncoding.base64().decode(parts[1]),
				Charsets.UTF_8);
		JwtClaims legitClaims = JwtClaims.parse(claims);
		LOG.debug("legit claims: {}", legitClaims);
		legitClaims.setSubject("malicious@elastisys.com");
		LOG.debug("tampered claims: {}", legitClaims);
		parts[1] = BaseEncoding.base64()
				.encode(legitClaims.toJson().getBytes());
		String tamperedToken = Joiner.on(".").join(parts);
		LOG.debug("tampered token: {}", tamperedToken);

		String authzHeader = "Bearer " + tamperedToken;
		try {
			validator.validate(authzHeader);
			fail("validation should have failed");
		} catch (AuthTokenValidationException e) {
			assertValidationException(e,
					"failed to validate Authorization token",
					"invalid auth token signature");
		}
	}

	/**
	 * Validation of an expired auth token should fail.
	 */
	@Test
	public void validateExpiredAuthToken() throws Exception {
		DateTime expirationTime = UtcTime.now().plusMinutes(10);
		String tokenWithExpiration = signToken(TOKEN_ISSUER,
				this.signatureKeyPair, expirationTime);

		// validate before expiry should be okay
		String authzHeader = "Bearer " + tokenWithExpiration;
		validator.validate(authzHeader);

		// wait for token to expire
		FrozenTime.setFixed(expirationTime);
		try {
			validator.validate(authzHeader);
			fail("validation should have failed");
		} catch (AuthTokenValidationException e) {
			assertValidationException(e,
					"failed to validate Authorization token",
					"access token has expired");
		}
	}

	/**
	 * The {@link AuthTokenHeaderValidator} will only check for token expiration
	 * if an {@code exp} claim is present. It does not require an expiration
	 * claim, it is up to the {@link AuthTokenValidator} to enforce any such
	 * requirements.
	 */
	@Test
	public void validateWithAuthTokenWithoutExpirationClaim() throws Exception {
		String tokenWithoutExpiration = signToken(TOKEN_ISSUER,
				this.signatureKeyPair, null);

		validator.validate("Bearer " + tokenWithoutExpiration);
	}

	/**
	 * A failing auth token validator implementation should cause an
	 * {@link AuthTokenValidationException} that includes the exception message.
	 */
	@Test
	public void failingAuthTokenValidator() throws Exception {
		AuthTokenValidator failingValidator = mock(AuthTokenValidator.class);

		when(failingValidator.validate(Mockito.anyString()))
				.thenThrow(new RuntimeException("internal error"));

		validator = new AuthTokenHeaderValidator(failingValidator);

		String signedToken = signToken(TOKEN_ISSUER, this.signatureKeyPair,
				UtcTime.now().plusMinutes(10));

		try {
			validator.validate("Bearer " + signedToken);
			fail("validation should have failed");
		} catch (AuthTokenValidationException e) {
			assertValidationException(e,
					"failed to validate Authorization token", "internal error");
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
		claims.setSubject("client@elastisys.com");
		// additional claims
		claims.setClaim("role", "user");

		JsonWebSignature jws = new JsonWebSignature();
		jws.setPayload(claims.toJson());
		jws.setKey(signatureKeyPair.getPrivateKey());
		jws.setKeyIdHeaderValue(signatureKeyPair.getKeyId());
		jws.setAlgorithmHeaderValue(AlgorithmIdentifiers.RSA_USING_SHA256);
		return jws.getCompactSerialization();
	}

	private void assertValidationException(AuthTokenValidationException e,
			String expectedMessage, String expectedDetail) {
		assertThat(e.getMessage(), is(expectedMessage));
		assertThat(e.getDetail(), is(expectedDetail));
	}

}
