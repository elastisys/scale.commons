package com.elastisys.scale.commons.rest.converters;

import java.text.SimpleDateFormat;

import javax.ws.rs.core.Application;
import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.Provider;

import org.glassfish.jersey.jackson.JacksonFeature;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

/**
 * A JAX-RS {@link Provider} that, when added to an {@link Application} together
 * with the {@link JacksonFeature}, provides conversions between Java (POJO)
 * types and JSON.
 *
 *
 *
 */
@Provider
public class JsonObjectMapperProvider implements ContextResolver<ObjectMapper> {

	private final ObjectMapper objectMapper;

	public JsonObjectMapperProvider() {
		this.objectMapper = defaultMapper();
	}

	@Override
	public ObjectMapper getContext(Class<?> type) {
		return this.objectMapper;
	}

	/**
	 * The default {@link ObjectMapper} to use for JSON<->Java conversions.
	 *
	 * @return
	 */
	public static ObjectMapper defaultMapper() {
		ObjectMapper objectMapper = new ObjectMapper();
		objectMapper.configure(SerializationFeature.INDENT_OUTPUT, true);
		objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS,
				false);

		// Make sure (de)serialization of date time fields use the same ISO-8601
		// date format (that includes milliseconds and time zone).
		SimpleDateFormat dateFormat = new SimpleDateFormat(
				"yyyy-MM-dd'T'HH:mm:ss.SSSZ");

		objectMapper.setDateFormat(dateFormat);

		return objectMapper;
	}

}
