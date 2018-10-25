package com.elastisys.scale.commons.rest.converters;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;

import javax.inject.Singleton;
import javax.ws.rs.Consumes;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.client.Client;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.NoContentException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.Provider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.elastisys.scale.commons.json.JsonUtils;
import com.elastisys.scale.commons.json.types.ErrorType;
import com.elastisys.scale.commons.util.io.IoUtils;
import com.google.gson.JsonParseException;

/**
 * A {@link MessageBodyReader} that can de-serialize arbitrary Java class
 * instances from an entity stream through the use of the Gson library.
 * <p/>
 * On the server-side, it needs to be registered as a {@link Provider} class in
 * the {@link Application}.
 * <p/>
 * On the client-side, it needs to be registered as a {@link Provider} class on
 * the {@link Client} object.
 */
@Provider
@Consumes(MediaType.APPLICATION_JSON)
@Singleton
public class GsonMessageBodyReader<T> implements MessageBodyReader<T> {
    private static final Logger LOG = LoggerFactory.getLogger(GsonMessageBodyReader.class);

    @Override
    public boolean isReadable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
        // assume we can deserialize instances for any class
        return true;
    }

    @Override
    public T readFrom(Class<T> type, Type genericType, Annotation[] annotations, MediaType mediaType,
            MultivaluedMap<String, String> httpHeaders, InputStream entityStream)
            throws IOException, WebApplicationException {
        try (Reader entityReader = new InputStreamReader(entityStream, StandardCharsets.UTF_8)) {
            String entity = IoUtils.toString(entityReader);
            if (entity == null || entity.isEmpty()) {
                throw new NoContentException("failed to deserialize JSON entity: " + "empty/null entity stream");
            }
            return JsonUtils.toObject(JsonUtils.parseJsonString(entity), type);
        } catch (IllegalArgumentException | JsonParseException e) {
            // produce a 400 http response
            throw new WebApplicationException(e, Response.status(Status.BAD_REQUEST).entity(new ErrorType(e)).build());
        }
    }

}
