package com.elastisys.scale.commons.json.typeadapters;

import java.lang.reflect.Type;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

/**
 * A Gson serializer capable of serializing {@link DateTime} objects to their
 * UTC time representation rather than their internal object representation.
 * <p/>
 * Produces datetime strings formatted as {@code 2014-01-13T12:00:00.000Z}.
 * <p/>
 * Register via:
 * 
 * <pre>
 * GsonBuilder gson = new GsonBuilder();
 * gson.registerTypeAdapter(DateTime.class, new GsonDateTimeSerializer());
 * </pre>
 * 
 * 
 */
public class GsonDateTimeSerializer implements JsonSerializer<DateTime> {

	@Override
	public JsonElement serialize(DateTime src, Type typeOfSrc,
			JsonSerializationContext context) {
		return new JsonPrimitive(src.toDateTime(DateTimeZone.UTC).toString());
	}

}
