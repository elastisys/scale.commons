package com.elastisys.scale.commons.rest.converters;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.client.Client;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;

import com.elastisys.scale.commons.json.JsonUtils;
import com.google.gson.JsonObject;

/**
 * A {@link MessageBodyWriter} that can serialize {@link JsonObject}s into bytes
 * to send over the wire.
 * <p/>
 * On the server-side, it needs to be registered as a {@link Provider} class in
 * the {@link Application}.
 * <p/>
 * On the client-side, it needs to be registered as a {@link Provider} class on
 * the {@link Client} object.
 * 
 * 
 */
@Provider
@Produces(MediaType.APPLICATION_JSON)
public class JsonObjectMessageBodyWriter implements
		MessageBodyWriter<JsonObject> {

	@Override
	public boolean isWriteable(Class<?> type, Type genericType,
			Annotation[] annotations, MediaType mediaType) {
		return JsonObject.class.isAssignableFrom(type);
	}

	@Override
	public long getSize(JsonObject t, Class<?> type, Type genericType,
			Annotation[] annotations, MediaType mediaType) {
		return serialize(t).length;
	}

	@Override
	public void writeTo(JsonObject t, Class<?> type, Type genericType,
			Annotation[] annotations, MediaType mediaType,
			MultivaluedMap<String, Object> httpHeaders,
			OutputStream entityStream) throws IOException,
			WebApplicationException {
		entityStream.write(serialize(t));
	}

	private byte[] serialize(JsonObject t) {
		return JsonUtils.toPrettyString(t).getBytes();
	}
}
