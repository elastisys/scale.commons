package com.elastisys.scale.commons.rest.auth;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.UriInfo;

import org.glassfish.jersey.server.ResourceConfig;
import org.jose4j.jwt.MalformedClaimException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.elastisys.scale.commons.json.JsonUtils;
import com.elastisys.scale.commons.rest.converters.GsonMessageBodyReader;
import com.elastisys.scale.commons.rest.converters.GsonMessageBodyWriter;
import com.google.gson.JsonElement;

/**
 * A dummy JAX-RS application for testing purposes that one resources that
 * requires JWT authentication (annotated with {@link RequireJwtAuthentication}
 * ): {@code /api/protected} and one resource that does not require
 * authentication: {@code /api/unprotected}.
 */
@ApplicationPath("/")
class SecuredApplication extends ResourceConfig {

	private final static Logger LOG = LoggerFactory
			.getLogger(SecuredApplication.class);

	/**
	 * Creates a new {@link SecuredApplication}.
	 *
	 * @param authTokenRequestFilter
	 *            The {@link AuthTokenRequestFilter} that will be used to
	 *            protect the application.
	 */
	public SecuredApplication(AuthTokenRequestFilter authTokenRequestFilter) {
		super();

		// request/response filters
		register(authTokenRequestFilter);

		// POJO <-> JSON conversions
		register(GsonMessageBodyReader.class);
		register(GsonMessageBodyWriter.class);

		// add resource provider classes
		register(ResponseHandler.class);
	}

	@Path("")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public static class ResponseHandler {

		private static Logger LOG = LoggerFactory
				.getLogger(ResponseHandler.class);

		@Context
		private UriInfo requestUri;

		/**
		 * Authentication required for this resource.
		 *
		 * @return
		 */
		@GET
		@Path("/protected")
		@RequireJwtAuthentication
		public Response getProtected(@Context SecurityContext securityContext)
				throws MalformedClaimException {
			LOG.info("GET {}", this.requestUri.getAbsolutePath());

			AuthTokenPrincipal authToken = AuthTokenPrincipal.class
					.cast(securityContext.getUserPrincipal());
			String client = authToken.getName();
			String issuer = authToken.getTokenClaims().getIssuer();
			LOG.debug("serving request for client '{}' (token issued by '{}')",
					client, issuer);
			JsonElement response = JsonUtils
					.parseJsonString("{\"value\": \"protected\"}");
			return Response.ok(response).build();
		}

		/**
		 * No authentication required for this resource.
		 *
		 * @return
		 */
		@GET
		@Path("/unprotected")
		public Response getUnprotected() {
			LOG.info("GET {}", this.requestUri.getAbsolutePath());
			JsonElement response = JsonUtils
					.parseJsonString("{\"value\": \"unprotected\"}");
			return Response.ok(response).build();
		}

	}

}
