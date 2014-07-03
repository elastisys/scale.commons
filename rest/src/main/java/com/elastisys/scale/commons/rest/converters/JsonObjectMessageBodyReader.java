package com.elastisys.scale.commons.rest.converters;

import static com.google.common.base.Preconditions.checkArgument;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import javax.ws.rs.Consumes;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.client.Client;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.Provider;

import com.elastisys.scale.commons.json.JsonUtils;
import com.elastisys.scale.commons.rest.types.ErrorType;
import com.google.common.base.Charsets;
import com.google.common.io.CharStreams;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

/**
 * A {@link MessageBodyReader} that can de-serialize JSON values from an entity
 * stream to a {@link JsonObject}.
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
@Consumes(MediaType.APPLICATION_JSON)
public class JsonObjectMessageBodyReader implements
		MessageBodyReader<JsonObject> {

	@Override
	public boolean isReadable(Class<?> type, Type genericType,
			Annotation[] annotations, MediaType mediaType) {
		return JsonObject.class.isAssignableFrom(type);
	}

	@Override
	public JsonObject readFrom(Class<JsonObject> type, Type genericType,
			Annotation[] annotations, MediaType mediaType,
			MultivaluedMap<String, String> httpHeaders, InputStream entityStream)
			throws IOException, WebApplicationException {
		try (Reader entityReader = new InputStreamReader(entityStream,
				Charsets.UTF_8)) {
			String entity = CharStreams.toString(entityReader);
			checkArgument(entity != null,
					"failed to parse JSON object: entity stream is null");
			checkArgument(!entity.isEmpty(),
					"failed to parse JSON object: entity stream is empty");
			return JsonUtils.parseJsonString(entity);
		} catch (IllegalArgumentException e) {
			// produce a 400 http response
			throw new WebApplicationException(e, Response
					.status(Status.BAD_REQUEST).entity(new ErrorType(e))
					.build());
		} catch (JsonParseException e) {
			// produce a 400 http response
			throw new WebApplicationException(e, Response
					.status(Status.BAD_REQUEST).entity(new ErrorType(e))
					.build());
		}
	}

}
