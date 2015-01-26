package com.elastisys.scale.commons.rest.server;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;

import org.glassfish.jersey.jackson.JacksonFeature;

import com.elastisys.scale.commons.rest.converters.JsonObjectMessageBodyReader;
import com.elastisys.scale.commons.rest.converters.JsonObjectMessageBodyWriter;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.gson.JsonObject;

/**
 * A JAX-RS REST {@link Application} that combines a number of registered
 * response handlers (resources in JAX-RS terminology) to handle client
 * requests.
 * <p/>
 * The {@link Application} supports JSON serialization/deserialization of
 * JAXB-annotated Java classes via the {@link JacksonFeature} as well as native
 * support for {@link JsonObject} parameters/response types in resource methods.
 * <p/>
 * Registered handlers (resources in JAX-RS terminology) are added as
 * singletons, which means that the same handler instance will be used to
 * service all incoming requests. Handlers should therefore be thread-safe to
 * ensure proper operation even in the face of multiple concurrent requests
 *
 *
 *
 */
@ApplicationPath("/")
public class JaxRsApplication extends Application {

	/**
	 * The list of response handlers ("resources") to be published by this
	 * {@link Application}.
	 */
	private final List<Object> responseHandlers;

	/**
	 * Creates a {@link JaxRsApplication} from a number of response handler web
	 * resources.
	 *
	 * @param responseHandlers
	 *            The response handler web resources.
	 */
	public JaxRsApplication(Object... responseHandlers) {
		this.responseHandlers = Lists.newArrayList(responseHandlers);
	}

	/**
	 * Register an additional response handler web resource.
	 *
	 * @param handler
	 *            An instance of a web resource class.
	 */
	public void addHandler(Object handler) {
		this.responseHandlers.add(handler);
	}

	/**
	 * Registers the all response handler web resources as singleton root
	 * resources.
	 *
	 * @see javax.ws.rs.core.Application#getSingletons()
	 */
	@Override
	public Set<Object> getSingletons() {
		Set<Object> singletons = Sets.newHashSet();

		// registers all response handlers as singleton resources
		for (Object handler : this.responseHandlers) {
			singletons.add(handler);
		}
		return singletons;
	}

	/**
	 * Takes care of registering required provider classes and features.
	 *
	 * @see javax.ws.rs.core.Application#getClasses()
	 */
	@Override
	public Set<Class<?>> getClasses() {
		final Set<Class<?>> classes = new HashSet<Class<?>>();

		// add message body reader/writer for JsonObject conversion
		classes.add(JsonObjectMessageBodyReader.class);
		classes.add(JsonObjectMessageBodyWriter.class);

		// could add filters here as well
		// classes.add(LoggingFilter.class);

		// support Jackson JSON serialization/deserialization of JAXB-annotated
		// Java classes.
		classes.add(JacksonFeature.class);

		return classes;
	}
}
