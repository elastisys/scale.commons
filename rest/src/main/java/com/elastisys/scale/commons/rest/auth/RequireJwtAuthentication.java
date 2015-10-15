package com.elastisys.scale.commons.rest.auth;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import javax.ws.rs.NameBinding;

/**
 * Annotation to be placed on resource classes/methods that require the user to
 * supply a valid JWT authentication token.
 * <p/>
 * Requests targeted towards a {@link RequireJwtAuthentication}-annotated
 * resource class/method will first pass through the
 * {@link AuthTokenRequestFilter}, which will ensure that the user has supplied
 * a valid authentication token, before the request is passed through to the
 * target resource method.
 *
 * @see AuthTokenRequestFilter
 */
// creates a name-binding between request filters and resource providers
// annotated by this annotation
@NameBinding
@Retention(RetentionPolicy.RUNTIME)
public @interface RequireJwtAuthentication {
}
