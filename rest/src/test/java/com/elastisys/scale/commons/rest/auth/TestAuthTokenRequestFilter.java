package com.elastisys.scale.commons.rest.auth;

import static com.elastisys.scale.commons.rest.auth.IsError.error;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.eclipse.jetty.server.Server;
import org.glassfish.jersey.servlet.ServletContainer;
import org.joda.time.DateTime;
import org.jose4j.jwk.RsaJsonWebKey;
import org.jose4j.jwk.RsaJwkGenerator;
import org.jose4j.jws.AlgorithmIdentifiers;
import org.jose4j.jws.JsonWebSignature;
import org.jose4j.jwt.JwtClaims;
import org.jose4j.jwt.NumericDate;
import org.jose4j.lang.JoseException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.elastisys.scale.commons.net.host.HostUtils;
import com.elastisys.scale.commons.rest.client.RestClients;
import com.elastisys.scale.commons.rest.types.ErrorType;
import com.elastisys.scale.commons.server.ServletDefinition;
import com.elastisys.scale.commons.server.ServletServerBuilder;
import com.elastisys.scale.commons.server.SslKeyStoreType;
import com.elastisys.scale.commons.util.time.FrozenTime;
import com.elastisys.scale.commons.util.time.UtcTime;
import com.google.common.base.Charsets;
import com.google.common.base.Joiner;
import com.google.common.io.BaseEncoding;
import com.google.common.io.Resources;

/**
 * Tests the {@link AuthTokenRequestFilter} by setting up a server with a dummy
 * JAX-RS application with some resources requiring JWT authentication
 * (annotated with {@link RequireJwtAuthentication}) and some not requiring
 * authentication, and access the server resources using a mix of authenticated
 * and non-authenticated client requests.
 */
public class TestAuthTokenRequestFilter {
	private static final String TOKEN_ISSUER = "Elastisys";

	private static final Logger LOG = LoggerFactory
			.getLogger(TestAuthTokenRequestFilter.class);

	private static final String SERVER_KEYSTORE_PATH = Resources
			.getResource("security/server_keystore.p12").toString();
	private static final String SERVER_KEYSTORE_PASSWORD = "serverpassword";

	private static int httpsPort = HostUtils.findFreePorts(1).get(0);
	private Server server;

	/** The signature key pair used to sign and verify auth tokens. */
	private RsaJsonWebKey signatureKeyPair;

	/** Object under test. */
	private static AuthTokenRequestFilter authTokenFilter;

	@Before
	public void beforeTestMethod() throws Exception {
		// test methods start need to start their own server
		this.server = null;
		FrozenTime.setFixed(UtcTime.parse("2015-01-01T12:00:00.000Z"));
		this.signatureKeyPair = RsaJwkGenerator.generateJwk(2048);
		this.signatureKeyPair.setKeyId(TOKEN_ISSUER + "-signkey");
	}

	@After
	public void afterTestMethod() throws Exception {
		// tear down the server is one was started by the test method
		if (this.server != null) {
			this.server.stop();
		}
	}

	/**
	 * Verify that a request without the {@code Authorization} header with an
	 * authentication token when requesting a protected resource fails.
	 */
	@Test
	public void accessProtectedResourceWithoutAuthToken() throws Exception {
		startServer(new AsymmetricKeyAuthTokenValidator(this.signatureKeyPair)
				.withExpectedIssuer(TOKEN_ISSUER));

		Response response = getWithoutToken("/api/protected");
		assertThat(response.getStatus(),
				is(Status.UNAUTHORIZED.getStatusCode()));
		assertThat(response.readEntity(ErrorType.class), is(new ErrorType(
				"request missing Authorization Bearer token header")));
		// verify that error header is present
		assertThat(response.getHeaderString("WWW-Authenticate"),
				is(notNullValue()));
	}

	/**
	 * A protected resource should be possible to access if the client supplies
	 * a valid auth token in the request.
	 */
	@Test
	public void accessProtectedResourceWithValidAuthToken() throws Exception {
		startServer(new AsymmetricKeyAuthTokenValidator(this.signatureKeyPair)
				.withExpectedIssuer(TOKEN_ISSUER));

		DateTime expirationTime = UtcTime.now().plusMinutes(10);
		String signedToken = signToken(TOKEN_ISSUER, this.signatureKeyPair,
				expirationTime);

		Response response = getWithToken("/api/protected", signedToken);
		assertThat(response.getStatus(), is(Status.OK.getStatusCode()));
	}

	/**
	 * Verify that a protected resource cannot be accessed when a malformed
	 * authentication token is used.
	 */
	@Test
	public void accessProtectedResourceWithMalformedAuthToken()
			throws Exception {
		startServer(new AsymmetricKeyAuthTokenValidator(this.signatureKeyPair)
				.withExpectedIssuer(TOKEN_ISSUER));

		String malformedToken = "eyJhbGciOiJIUzI1";
		// access protected resource (with authentication token)
		WebTarget resource = RestClients.httpsNoAuth()
				.target(httpsUrl("/api/protected"));

		String authzHeader = "Bearer " + malformedToken;
		Response response = resource.request()
				.header("Authorization", authzHeader).get();
		assertThat(response.getStatus(),
				is(Status.UNAUTHORIZED.getStatusCode()));
		assertThat(response.readEntity(ErrorType.class),
				is(new ErrorType("malformed Authorization Bearer token")));
		// verify that error header is present
		assertThat(response.getHeaderString("WWW-Authenticate"),
				is(notNullValue()));
	}

	/**
	 * Verify that a protected resource cannot be accessed when an
	 * authentication token signed with the wrong key (or some other party) is
	 * used.
	 */
	@Test
	public void accessProtectedResourceWithForgedAuthToken() throws Exception {
		startServer(new AsymmetricKeyAuthTokenValidator(this.signatureKeyPair)
				.withExpectedIssuer(TOKEN_ISSUER));

		DateTime expirationTime = UtcTime.now().plusMinutes(10);
		RsaJsonWebKey wrongKeyPair = RsaJwkGenerator.generateJwk(2048);
		String forgedToken = signToken(TOKEN_ISSUER, wrongKeyPair,
				expirationTime);
		Response response = getWithToken("/api/protected", forgedToken);
		assertThat(response.getStatus(),
				is(Status.UNAUTHORIZED.getStatusCode()));
		assertThat(response.readEntity(ErrorType.class),
				is(error("failed to validate Authorization token")));
		// verify that error header is present
		assertThat(response.getHeaderString("WWW-Authenticate"),
				is(notNullValue()));
	}

	/**
	 * Verify that it isn't possible to access a protected resource with an auth
	 * token that has been tampered with (modify the claims part of the token).
	 */
	@Test
	public void accessProtectedResourceWithTamperedAuthToken()
			throws Exception {
		startServer(new AsymmetricKeyAuthTokenValidator(this.signatureKeyPair)
				.withExpectedIssuer(TOKEN_ISSUER));

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

		Response response = getWithToken("/api/protected", tamperedToken);
		assertThat(response.getStatus(),
				is(Status.UNAUTHORIZED.getStatusCode()));
		assertThat(response.readEntity(ErrorType.class),
				is(error("failed to validate Authorization token")));
		// verify that error header is present
		assertThat(response.getHeaderString("WWW-Authenticate"),
				is(notNullValue()));
	}

	/**
	 * Verify that the {@link AuthTokenRequestFilter} only applies to resource
	 * classes/methods annotated with {@link RequireJwtAuthentication}. For
	 * other, unprotected, resources no auth token should be required.
	 */
	@Test
	public void accessUnprotectedResourceWithoutAuthToken() throws Exception {
		startServer(new AsymmetricKeyAuthTokenValidator(this.signatureKeyPair)
				.withExpectedIssuer(TOKEN_ISSUER));

		Response response = getWithoutToken("/api/unprotected");
		assertThat(response.getStatus(), is(Status.OK.getStatusCode()));

		// should also be possible to include whatever token in the request,
		// since the token is not validated (or even inspected).
		response = getWithToken("/api/unprotected", "bogus_token");
		assertThat(response.getStatus(), is(Status.OK.getStatusCode()));
	}

	/**
	 * It should not be possible to access a protected resource with an expired
	 * auth token.
	 */
	@Test
	public void accessProtectedResourceWithExpiredAuthToken() throws Exception {
		startServer(new AsymmetricKeyAuthTokenValidator(this.signatureKeyPair)
				.withExpectedIssuer(TOKEN_ISSUER));

		DateTime expirationTime = UtcTime.now().plusMinutes(10);
		String tokenWithExpiration = signToken(TOKEN_ISSUER,
				this.signatureKeyPair, expirationTime);
		Response response = getWithToken("/api/protected", tokenWithExpiration);
		assertThat(response.getStatus(), is(Status.OK.getStatusCode()));

		// wait for token to expire
		FrozenTime.setFixed(expirationTime);
		response = getWithToken("/api/protected", tokenWithExpiration);
		assertThat(response.getStatus(),
				is(Status.UNAUTHORIZED.getStatusCode()));
		assertThat(response.readEntity(ErrorType.class),
				is(error("failed to validate Authorization token")));
		// verify that error header is present
		assertThat(response.getHeaderString("WWW-Authenticate"),
				is(notNullValue()));
	}

	/**
	 * The {@link AuthTokenRequestFilter} will only check for token expiration
	 * if an {@code exp} claim is present. It does not require an expiration
	 * claim, it is up to the {@link AuthTokenValidator} to enforce any such
	 * requirements.
	 */
	@Test
	public void accessProtectedResourceWithAuthTokenWithoutExpirationClaim()
			throws Exception {
		startServer(new AsymmetricKeyAuthTokenValidator(this.signatureKeyPair)
				.withExpectedIssuer(TOKEN_ISSUER));

		String tokenWithoutExpiration = signToken(TOKEN_ISSUER,
				this.signatureKeyPair, null);
		Response response = getWithToken("/api/protected",
				tokenWithoutExpiration);
		assertThat(response.getStatus(), is(Status.OK.getStatusCode()));
	}

	/**
	 * A failing auth token validator implementation should cause a
	 * {@code 401 (Unauthorized)} response that includes the exception message
	 * in the response {@link ErrorType}.
	 */
	@Test
	public void failingAuthTokenValidator() throws Exception {
		AuthTokenValidator failingValidator = mock(AuthTokenValidator.class);

		when(failingValidator.validate(Mockito.anyString()))
				.thenThrow(new RuntimeException("internal error"));

		startServer(failingValidator);

		String signedToken = signToken(TOKEN_ISSUER, this.signatureKeyPair,
				UtcTime.now().plusMinutes(10));
		Response response = getWithToken("/api/protected", signedToken);
		assertThat(response.getStatus(),
				is(Status.UNAUTHORIZED.getStatusCode()));
		assertThat(response.readEntity(ErrorType.class),
				is(new ErrorType("failed to validate Authorization token",
						"internal error")));
		// verify that error header is present
		assertThat(response.getHeaderString("WWW-Authenticate"),
				is(notNullValue()));

	}

	/**
	 * Starts a test server with an {@link AuthTokenRequestFilter} that makes
	 * use of the given {@link AuthTokenValidator} used to protect a
	 * {@link SecuredApplication}.
	 *
	 * @param authTokenValidator
	 * @throws Exception
	 */
	private void startServer(AuthTokenValidator authTokenValidator)
			throws Exception {
		authTokenFilter = new AuthTokenRequestFilter(authTokenValidator);

		ServletContainer appServlet = new ServletContainer(
				new SecuredApplication(authTokenFilter));
		ServletDefinition apiServlet = new ServletDefinition.Builder()
				.servlet(appServlet).servletPath("/api").requireHttps(true)
				.requireBasicAuth(false).build();

		this.server = ServletServerBuilder.create().httpsPort(httpsPort)
				.sslKeyStoreType(SslKeyStoreType.PKCS12)
				.sslKeyStorePath(SERVER_KEYSTORE_PATH)
				.sslKeyStorePassword(SERVER_KEYSTORE_PASSWORD)
				.sslRequireClientCert(false).addServlet(apiServlet).build();

		this.server.start();
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

	/**
	 * Performs a {@code GET} for a given resource without supplying a
	 * {@code Authorization: Bearer <token>} header.
	 *
	 * @param resourcePath
	 * @return
	 */
	private Response getWithoutToken(String resourcePath) {
		WebTarget resource = RestClients.httpsNoAuth()
				.target(httpsUrl(resourcePath));
		return resource.request().get();
	}

	/**
	 * Performs a {@code GET} for a given resource, supplying a
	 * {@code Authorization: Bearer <token>} header.
	 *
	 * @param resourcePath
	 * @param authToken
	 * @return
	 */
	private Response getWithToken(String resourcePath, String authToken) {
		WebTarget resource = RestClients.httpsNoAuth()
				.target(httpsUrl(resourcePath));
		String authzHeader = "Bearer " + authToken;
		return resource.request().header("Authorization", authzHeader).get();
	}

	private static String httpsUrl(String resourcePath) {
		String absolutePath = makeAbsolute(resourcePath);
		return String.format("https://localhost:%d%s", httpsPort, absolutePath);
	}

	private static String makeAbsolute(String resourcePath) {
		if (!resourcePath.startsWith("/")) {
			resourcePath = "/" + resourcePath;
		}
		return resourcePath;
	}
}
