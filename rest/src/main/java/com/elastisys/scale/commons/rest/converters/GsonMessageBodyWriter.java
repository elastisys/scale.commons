package com.elastisys.scale.commons.rest.converters;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import javax.inject.Singleton;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.client.Client;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.elastisys.scale.commons.json.JsonUtils;
import com.google.common.base.Charsets;

/**
 * A {@link MessageBodyWriter} that can serialize arbitrary Java class instances
 * JSON-formatted bytes to be sent over the wire.
 * <p/>
 * On the server-side, it needs to be registered as a {@link Provider} class in
 * the {@link Application}.
 * <p/>
 * On the client-side, it needs to be registered as a {@link Provider} class on
 * the {@link Client} object.
 */
@Provider
@Produces(MediaType.APPLICATION_JSON)
@Singleton
public class GsonMessageBodyWriter<T> implements MessageBodyWriter<T> {
	private static final Logger LOG = LoggerFactory
			.getLogger(GsonMessageBodyWriter.class);

	@Override
	public boolean isWriteable(Class<?> type, Type genericType,
			Annotation[] annotations, MediaType mediaType) {
		// assume we can serialize instances for any class
		return true;
	}

	@Override
	public long getSize(T t, Class<?> type, Type genericType,
			Annotation[] annotations, MediaType mediaType) {
		// according to getSize javadoc, this method is ignored by the JAX-RS
		// runtime
		return -1;
	}

	@Override
	public void writeTo(T t, Class<?> type, Type genericType,
			Annotation[] annotations, MediaType mediaType,
			MultivaluedMap<String, Object> httpHeaders,
			OutputStream entityStream) throws IOException,
			WebApplicationException {
		entityStream.write(serialize(t));
	}

	private byte[] serialize(T t) {
		return JsonUtils.toPrettyString(JsonUtils.toJson(t)).getBytes(
				Charsets.UTF_8);
	}
}
