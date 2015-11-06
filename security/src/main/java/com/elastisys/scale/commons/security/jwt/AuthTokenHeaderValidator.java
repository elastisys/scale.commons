package com.elastisys.scale.commons.security.jwt;

import static java.lang.String.format;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jose4j.jwt.JwtClaims;
import org.jose4j.jwt.MalformedClaimException;
import org.jose4j.jwt.NumericDate;
import org.jose4j.jwt.ReservedClaimNames;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.elastisys.scale.commons.util.time.UtcTime;

/**
 * Validates Authorization headers carrying JSON Web Token (JWT) authentication
 * tokens. These request headers are of the form {@code Authorization: Bearer
 * <token>} where the header name is {@code Authorization} and the {@code
 * <token>} part is a base 64-encoded JSON. For details on what a token may look
 * like, see <a href="http://jwt.io/">jwt.io</a>.
 * <p/>
 * The {@link AuthTokenHeaderValidator} gets passed the header value and
 * performs the following checks:
 * <ul>
 * <li>It verifies that the header value is well-formed. That is, it follows the
 * form: {@code Bearer <token>}.</li>
 * <li>It uses a wrapped {@link AuthTokenValidator} to deserialize the token,
 * validate its signature and also verify the claims of the token.</li>
 * <li>Verifies that the token hasn't expired, in case an expiration time is
 * set.</li>
 * </ul>
 */
public class AuthTokenHeaderValidator {

	private static final Logger LOG = LoggerFactory
			.getLogger(AuthTokenHeaderValidator.class);

	private static final String AUTHORIZATION_HEADER = "Authorization";

	/**
	 * A correct authentication token has three dot-separated segments of
	 * base64-encoded strings.
	 */
	private static final Pattern AUTH_TOKEN_PATTERN = Pattern.compile(
			"Bearer ([\\p{Alnum}\\-_+=/]+\\.[\\p{Alnum}\\-_+=/]+\\.[\\p{Alnum}\\-_+=/]+)");

	/**
	 * Will be called to deserialize and validate an auth token for requests
	 * that have them.
	 */
	private final AuthTokenValidator tokenValidator;

	/**
	 * Creates an {@link AuthTokenHeaderValidator} with a given token validator.
	 *
	 * @param tokenValidator
	 *            The {@link AuthTokenValidator} which will be called to
	 *            deserialize and validate auth token signature and claims.
	 */
	public AuthTokenHeaderValidator(AuthTokenValidator tokenValidator) {
		this.tokenValidator = tokenValidator;
	}

	/**
	 * Validates an {@code Authorization} header value by:
	 * <ul>
	 * <li>Verifying that the header value is well-formed. That is, it follows
	 * the form: {@code Bearer <token>}.</li>
	 * <li>Using a wrapped {@link AuthTokenValidator} to deserialize the token,
	 * validate its signature and also verify the claims of the token.</li>
	 * <li>Verifying that the token hasn't expired, in case an expiration time
	 * is set.</li>
	 * </ul>
	 *
	 * @param authorizationHeader
	 *            The value of an {@code Authorization} header. In order to be
	 *            well-formed, this value must be of the form
	 *            {@code Bearer <token>}, with {@code <token>} being a base
	 *            64-encoded JWT.
	 * @return
	 * @throws AuthTokenValidationException
	 * @throws MalformedClaimException
	 */
	public JwtClaims validate(String authorizationHeader)
			throws AuthTokenValidationException {
		Matcher matcher = AUTH_TOKEN_PATTERN.matcher(authorizationHeader);
		if (!matcher.matches()) {
			String message = "failed to validate Authorization token";
			String detail = format("malformed %s Bearer token",
					AUTHORIZATION_HEADER);
			LOG.debug("%s: %s", message, detail);
			throw new AuthTokenValidationException(message, detail);
		}
		String signedToken = matcher.group(1);

		JwtClaims tokenClaims = null;
		try {
			tokenClaims = this.tokenValidator.validate(signedToken);
		} catch (Exception e) {
			LOG.debug("failed to validate Authorization token: {}",
					e.getMessage());
			throw new AuthTokenValidationException(
					"failed to validate Authorization token", e.getMessage());
		}

		// make sure that token hasn't expired
		if (tokenClaims.hasClaim(ReservedClaimNames.EXPIRATION_TIME)) {
			checkForExpiration(tokenClaims);
		} else {
			if (LOG.isTraceEnabled()) {
				LOG.trace("token missing expiration time");
			}
		}

		try {
			LOG.debug("validated auth token for client '{}'",
					tokenClaims.getSubject());
		} catch (MalformedClaimException e) {
			String message = "failed to validate Authorization token";
			String detail = String.format(
					"auth token claims were malformed: %s", e.getMessage());
			throw new AuthTokenValidationException(message, detail, e);
		}
		return tokenClaims;
	}

	private void checkForExpiration(JwtClaims tokenClaims)
			throws AuthTokenValidationException {
		NumericDate now = NumericDate
				.fromMilliseconds(UtcTime.now().getMillis());
		try {
			if (now.isAfter(tokenClaims.getExpirationTime())) {
				String message = "failed to validate Authorization token";
				String detail = "access token has expired";
				throw new AuthTokenValidationException(message, detail);
			}
		} catch (MalformedClaimException e) {
			String message = "failed to validate Authorization token";
			String detail = format(
					"failed to check access token expiration time: %s",
					e.getMessage());
			throw new AuthTokenValidationException(message, detail);
		}
	}
}
