package com.elastisys.scale.commons.rest.auth;

import static java.lang.String.format;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.jose4j.jwt.JwtClaims;
import org.jose4j.jwt.MalformedClaimException;
import org.jose4j.jwt.NumericDate;
import org.jose4j.jwt.ReservedClaimNames;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.elastisys.scale.commons.rest.types.ErrorType;
import com.elastisys.scale.commons.util.time.UtcTime;

/**
 * A server request filter that only lets requests through with a authentication
 * token header of form {@code Authorization: Bearer <token>} that can be
 * validated. Any requests missing a successfully validated token are denied
 * with a {@code 401 (Unauthorized)} response.
 * <p/>
 * This filter is only applied for requests targeted to a
 * {@link RequireJwtAuthentication}-annotated resource class/method.
 * <p>
 * The filter only makes sure that the authentication token is present on a
 * request and delegates token deserialization and signature validation to a
 * {@link AuthTokenValidator}, which must be provided at construction time.
 * </p>
 * The JSON Web Token specification can be found
 * <a href="http://tools.ietf.org/html/rfc7519">here</a>.
 * <p/>
 * The bearer token specification is found
 * <a href="http://tools.ietf.org/html/rfc6750">here</a>.
 *
 * @see RequireJwtAuthentication
 */
@RequireJwtAuthentication
public class AuthTokenRequestFilter implements ContainerRequestFilter {
	static Logger LOG = LoggerFactory.getLogger(AuthTokenRequestFilter.class);

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
	 * Creates an {@link AuthTokenRequestFilter} with a given token validator.
	 *
	 * @param tokenValidator
	 *            The {@link AuthTokenValidator} which will be called to
	 *            deserialize and validate an auth token for requests that have
	 *            them.
	 */
	public AuthTokenRequestFilter(AuthTokenValidator tokenValidator) {
		this.tokenValidator = tokenValidator;
	}

	@Override
	public void filter(ContainerRequestContext request) throws IOException {
		try {
			validateAuthToken(request);
		} catch (Exception e) {
			LOG.debug("authentication token validation failed: {}",
					e.getMessage(), e);
			throw e;
		}
	}

	private void validateAuthToken(ContainerRequestContext request) {
		String authorizationHeader = request
				.getHeaderString(AUTHORIZATION_HEADER);
		if (authorizationHeader == null) {
			String errorMessage = format(
					"request missing %s Bearer token header",
					AUTHORIZATION_HEADER);
			request.abortWith(errorResponse(new ErrorType(errorMessage)));
			return;
		}
		if (LOG.isTraceEnabled()) {
			LOG.trace("received request with authorization header {}",
					authorizationHeader);
		}

		Matcher matcher = AUTH_TOKEN_PATTERN.matcher(authorizationHeader);
		if (!matcher.matches()) {
			String errorMessage = format("malformed %s Bearer token",
					AUTHORIZATION_HEADER);
			LOG.debug(errorMessage);
			request.abortWith(errorResponse(new ErrorType(errorMessage)));
			return;
		}
		String signedToken = matcher.group(1);

		JwtClaims tokenClaims = null;
		try {
			tokenClaims = this.tokenValidator.validate(signedToken);
		} catch (Exception e) {
			String errorMessage = format(
					"failed to validate Authorization token");
			LOG.debug("{}: {}", errorMessage, e.getMessage());
			request.abortWith(
					errorResponse(new ErrorType(errorMessage, e.getMessage())));
			return;
		}

		// make sure that token hasn't expired
		if (tokenClaims.hasClaim(ReservedClaimNames.EXPIRATION_TIME)) {
			checkForExpiration(request, tokenClaims);
		} else {
			if (LOG.isTraceEnabled()) {
				LOG.trace("token missing expiration time");
			}
		}

		boolean secure = request.getSecurityContext().isSecure();
		// set up a security context for the logged in principal
		request.setSecurityContext(
				new AuthTokenSecurityContext(tokenClaims, secure));
		LOG.debug("validated auth token for client '{}'",
				request.getSecurityContext().getUserPrincipal().getName());
	}

	private void checkForExpiration(ContainerRequestContext request,
			JwtClaims tokenClaims) {
		NumericDate now = NumericDate
				.fromMilliseconds(UtcTime.now().getMillis());
		try {
			if (now.isAfter(tokenClaims.getExpirationTime())) {
				String errorMessage = format("access token has expired");
				request.abortWith(errorResponse(new ErrorType(errorMessage)));
				return;
			}
		} catch (MalformedClaimException e) {
			String errorMessage = format(
					"failed to check access token expiration time: %s",
					e.getMessage());
			request.abortWith(errorResponse(new ErrorType(errorMessage)));
		}
	}

	private Response errorResponse(ErrorType error) {
		String errorHeader = "WWW-Authenticate";
		String errorHeaderValue = String.format(
				"Bearer, error=\"%s\", error_description=\"%s\"",
				"invalid_token", error.getMessage());
		return Response.status(Response.Status.UNAUTHORIZED).entity(error)
				.type(MediaType.APPLICATION_JSON)
				.header(errorHeader, errorHeaderValue).build();
	}

}
